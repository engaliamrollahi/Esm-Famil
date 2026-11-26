package com.example.network

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.model.DiscoveredPeer
import com.example.model.LocalTransportType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.UUID

class BluetoothSyncTransport(
    context: Context,
    private val scope: CoroutineScope,
    private val onConnected: (peerLabel: String) -> Unit,
    private val onMessage: (SyncMessage) -> Unit,
    private val onPeersUpdated: (List<DiscoveredPeer>) -> Unit,
    private val onError: (String) -> Unit,
    private val onDisconnected: (String) -> Unit,
) {
    private val appContext = context.applicationContext
    private val bluetoothManager = appContext.getSystemService(BluetoothManager::class.java)
    private val adapter: BluetoothAdapter? get() = bluetoothManager?.adapter

    private var serverSocket: BluetoothServerSocket? = null
    private var clientSocket: BluetoothSocket? = null
    private var writer: BufferedWriter? = null
    private var discoveryReceiver: BroadcastReceiver? = null
    private var ioJob: Job? = null

    private val writeMutex = Mutex()
    private val discoveredMap = LinkedHashMap<String, DiscoveredPeer>()

    @Volatile
    private var isClosed = false

    fun isSupported(): Boolean = adapter != null

    fun isEnabled(): Boolean = adapter?.isEnabled == true

    fun missingPermissionsForHost(): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
            ).filter { !hasPermission(it) }
        } else {
            emptyList()
        }
    }

    fun missingPermissionsForScan(): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
            ).filter { !hasPermission(it) }
        } else {
            listOf(Manifest.permission.ACCESS_FINE_LOCATION).filter { !hasPermission(it) }
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(appContext, permission) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun startHosting(onReady: (deviceName: String) -> Unit) {
        close()
        isClosed = false

        val btAdapter = adapter
        if (btAdapter == null) {
            onError("این دستگاه از بلوتوث پشتیبانی نمی‌کند.")
            return
        }
        if (!btAdapter.isEnabled) {
            onError("لطفاً ابتدا بلوتوث دستگاه را روشن کنید.")
            return
        }
        if (missingPermissionsForHost().isNotEmpty()) {
            onError("مجوز اتصال بلوتوث داده نشده است.")
            return
        }

        val deviceLabel = runCatching { btAdapter.name }.getOrNull().orEmpty().ifBlank { "گوشی میزبان" }

        ioJob = scope.launch(Dispatchers.IO) {
            try {
                val server = btAdapter.listenUsingRfcommWithServiceRecord(SERVICE_NAME, APP_UUID)
                serverSocket = server
                withContext(Dispatchers.Main) {
                    onReady(deviceLabel)
                }

                val socket = server.accept()
                if (isClosed) {
                    runCatching { socket.close() }
                    return@launch
                }
                runCatching { server.close() }
                serverSocket = null

                val remoteName = runCatching { socket.remoteDevice?.name }.getOrNull()
                    .orEmpty()
                    .ifBlank { socket.remoteDevice?.address ?: "بازیکن بلوتوث" }
                attachSocket(socket, remoteName)
            } catch (sec: SecurityException) {
                if (!isClosed) {
                    withContext(Dispatchers.Main) {
                        onError("عدم دسترسی به مجوز بلوتوث: ${sec.localizedMessage ?: ""}")
                    }
                }
            } catch (e: Exception) {
                if (!isClosed) {
                    withContext(Dispatchers.Main) {
                        onError("راه‌اندازی میزبان بلوتوث ناموفق بود: ${e.localizedMessage ?: "خطا"}")
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startDiscovery() {
        stopDiscoveryOnly()
        isClosed = false
        discoveredMap.clear()

        val btAdapter = adapter
        if (btAdapter == null) {
            onError("این دستگاه از بلوتوث پشتیبانی نمی‌کند.")
            return
        }
        if (!btAdapter.isEnabled) {
            onError("لطفاً ابتدا بلوتوث دستگاه را روشن کنید.")
            return
        }
        if (missingPermissionsForScan().isNotEmpty()) {
            onError("مجوز جست‌وجو و اتصال بلوتوث داده نشده است.")
            return
        }

        try {
            // Load paired/bonded devices first for fast selection
            btAdapter.bondedDevices.orEmpty().forEach { device ->
                val addr = device.address ?: return@forEach
                val name = runCatching { device.name }.getOrNull().orEmpty().ifBlank { addr }
                discoveredMap[addr] = DiscoveredPeer(
                    id = addr,
                    name = name,
                    address = addr,
                    port = 0,
                    transport = LocalTransportType.BLUETOOTH,
                    isPaired = true,
                )
            }
            onPeersUpdated(discoveredMap.values.toList())

            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (intent?.action != BluetoothDevice.ACTION_FOUND) return
                    @Suppress("DEPRECATION")
                    val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    val addr = device?.address ?: return
                    val name = runCatching { device.name }.getOrNull().orEmpty().ifBlank { addr }
                    val existing = discoveredMap[addr]
                    discoveredMap[addr] = DiscoveredPeer(
                        id = addr,
                        name = name,
                        address = addr,
                        port = 0,
                        transport = LocalTransportType.BLUETOOTH,
                        isPaired = existing?.isPaired == true,
                    )
                    onPeersUpdated(discoveredMap.values.toList())
                }
            }
            discoveryReceiver = receiver
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            ContextCompat.registerReceiver(appContext, receiver, filter, ContextCompat.RECEIVER_EXPORTED)

            if (btAdapter.isDiscovering) {
                btAdapter.cancelDiscovery()
            }
            btAdapter.startDiscovery()
        } catch (sec: SecurityException) {
            onError("عدم دسترسی به مجوز بلوتوث: ${sec.localizedMessage ?: ""}")
        } catch (e: Exception) {
            onError("خطا در جست‌وجوی بلوتوث: ${e.localizedMessage ?: "خطای دستگاه"}")
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(address: String, label: String) {
        stopDiscoveryOnly()
        isClosed = false

        val btAdapter = adapter
        if (btAdapter == null || !btAdapter.isEnabled) {
            onError("بلوتوث روشن نیست.")
            return
        }

        ioJob = scope.launch(Dispatchers.IO) {
            try {
                if (btAdapter.isDiscovering) {
                    btAdapter.cancelDiscovery()
                }
                val device = btAdapter.getRemoteDevice(address)
                val socket = device.createRfcommSocketToServiceRecord(APP_UUID)
                socket.connect()
                if (isClosed) {
                    runCatching { socket.close() }
                    return@launch
                }
                attachSocket(socket, label)
            } catch (sec: SecurityException) {
                if (!isClosed) {
                    withContext(Dispatchers.Main) {
                        onError("عدم دسترسی به مجوز اتصال بلوتوث.")
                    }
                }
            } catch (e: Exception) {
                if (!isClosed) {
                    withContext(Dispatchers.Main) {
                        onError("اتصال بلوتوث برقرار نشد. مطمئن شوید طرف مقابل روی حالت میزبان است.")
                    }
                }
            }
        }
    }

    private suspend fun attachSocket(socket: BluetoothSocket, peerLabel: String) {
        clientSocket = socket
        val reader = BufferedReader(InputStreamReader(socket.inputStream, Charsets.UTF_8))
        writer = BufferedWriter(OutputStreamWriter(socket.outputStream, Charsets.UTF_8))

        withContext(Dispatchers.Main) {
            onConnected(peerLabel)
        }

        try {
            while (scope.isActive && !isClosed && socket.isConnected) {
                val line = reader.readLine() ?: break
                val decoded = SyncMessage.decode(line) ?: continue
                withContext(Dispatchers.Main) {
                    onMessage(decoded)
                }
            }
        } catch (_: Exception) {
            // Stream closed
        } finally {
            if (!isClosed) {
                withContext(Dispatchers.Main) {
                    onDisconnected("ارتباط بلوتوث با بازیکن مقابل قطع شد.")
                }
            }
        }
    }

    suspend fun send(message: SyncMessage): Boolean = withContext(Dispatchers.IO) {
        writeMutex.withLock {
            val activeWriter = writer ?: return@withContext false
            return@withContext try {
                activeWriter.write(message.encode())
                activeWriter.newLine()
                activeWriter.flush()
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun stopDiscoveryOnly() {
        discoveryReceiver?.let { receiver ->
            runCatching { appContext.unregisterReceiver(receiver) }
        }
        discoveryReceiver = null
        runCatching {
            if (missingPermissionsForScan().isEmpty() && adapter?.isDiscovering == true) {
                adapter?.cancelDiscovery()
            }
        }
    }

    fun close() {
        isClosed = true
        ioJob?.cancel()
        ioJob = null
        stopDiscoveryOnly()
        runCatching { writer?.close() }
        writer = null
        runCatching { clientSocket?.close() }
        clientSocket = null
        runCatching { serverSocket?.close() }
        serverSocket = null
    }

    companion object {
        private const val SERVICE_NAME = "EsmFamilSync"
        val APP_UUID: UUID = UUID.fromString("8d7a4c20-5e1b-4c9a-9f12-3e4b5c6d7a01")
    }
}
