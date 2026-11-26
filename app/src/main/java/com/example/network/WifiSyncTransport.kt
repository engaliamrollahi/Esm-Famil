package com.example.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
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
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket

class WifiSyncTransport(
    context: Context,
    private val scope: CoroutineScope,
    private val onConnected: (peerLabel: String) -> Unit,
    private val onMessage: (SyncMessage) -> Unit,
    private val onPeersUpdated: (List<DiscoveredPeer>) -> Unit,
    private val onError: (String) -> Unit,
    private val onDisconnected: (String) -> Unit,
) {
    private val appContext = context.applicationContext
    private val nsdManager = appContext.getSystemService(Context.NSD_SERVICE) as? NsdManager
    private val wifiManager = appContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    private var multicastLock: WifiManager.MulticastLock? = null
    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var writer: BufferedWriter? = null

    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null

    private var ioJob: Job? = null
    private val writeMutex = Mutex()
    private val resolveMutex = Mutex()
    private val discoveredMap = LinkedHashMap<String, DiscoveredPeer>()

    @Volatile
    private var isClosed = false

    fun startHosting(
        roomDisplayName: String,
        onReady: (ipAddress: String, port: Int, registeredName: String) -> Unit,
    ) {
        close()
        isClosed = false

        ioJob = scope.launch(Dispatchers.IO) {
            try {
                acquireMulticastLock()
                val server = ServerSocket(0)
                serverSocket = server
                val port = server.localPort
                val ip = resolveLocalIpv4Address() ?: "127.0.0.1"
                val serviceName = "EsmFamil-$roomDisplayName"

                registerNsdService(serviceName, port)
                withContext(Dispatchers.Main) {
                    onReady(ip, port, serviceName)
                }

                val accepted = server.accept()
                if (isClosed) {
                    runCatching { accepted.close() }
                    return@launch
                }
                unregisterNsdService()
                runCatching { server.close() }
                serverSocket = null

                attachSocket(accepted, accepted.inetAddress?.hostAddress ?: "بازیکن مهمان")
            } catch (e: Exception) {
                if (!isClosed) {
                    withContext(Dispatchers.Main) {
                        onError("راه‌اندازی میزبان وای‌فای ناموفق بود: ${e.localizedMessage ?: "خطای شبکه"}")
                    }
                }
            }
        }
    }

    fun startDiscovery() {
        stopDiscoveryOnly()
        isClosed = false
        discoveredMap.clear()
        onPeersUpdated(emptyList())

        val manager = nsdManager
        if (manager == null) {
            onError("سرویس کشف شبکه وای‌فای در این دستگاه در دسترس نیست.")
            return
        }

        acquireMulticastLock()
        val listener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) = Unit

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                if (serviceInfo.serviceType?.contains("_esmfamil") != true) return
                resolveServiceQueued(manager, serviceInfo)
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                val key = serviceInfo.serviceName ?: return
                scope.launch(Dispatchers.Main) {
                    discoveredMap.remove(key)
                    onPeersUpdated(discoveredMap.values.toList())
                }
            }

            override fun onDiscoveryStopped(serviceType: String) = Unit

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                scope.launch(Dispatchers.Main) {
                    onError("جست‌وجوی وای‌فای با خطا مواجه شد (کد $errorCode). می‌توانید با IP مستقیم متصل شوید.")
                }
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) = Unit
        }
        discoveryListener = listener
        runCatching {
            manager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, listener)
        }.onFailure {
            onError("امکان شروع جست‌وجوی وای‌فای وجود ندارد. از اتصال وای‌فای یا هات‌اسپات مطمئن شوید.")
        }
    }

    @Suppress("DEPRECATION")
    private fun resolveServiceQueued(manager: NsdManager, serviceInfo: NsdServiceInfo) {
        scope.launch(Dispatchers.IO) {
            resolveMutex.withLock {
                if (isClosed) return@withLock
                val latch = java.util.concurrent.CountDownLatch(1)
                manager.resolveService(
                    serviceInfo,
                    object : NsdManager.ResolveListener {
                        override fun onResolveFailed(info: NsdServiceInfo, errorCode: Int) {
                            latch.countDown()
                        }

                        override fun onServiceResolved(resolved: NsdServiceInfo) {
                            val hostAddress = resolved.host?.hostAddress
                            val port = resolved.port
                            val rawName = resolved.serviceName ?: "اتاق وای‌فای"
                            val displayName = rawName.removePrefix("EsmFamil-").ifBlank { rawName }
                            if (!hostAddress.isNullOrBlank() && port > 0) {
                                val peer = DiscoveredPeer(
                                    id = rawName,
                                    name = displayName,
                                    address = hostAddress,
                                    port = port,
                                    transport = LocalTransportType.WIFI,
                                    isPaired = false,
                                )
                                scope.launch(Dispatchers.Main) {
                                    discoveredMap[rawName] = peer
                                    onPeersUpdated(discoveredMap.values.toList())
                                }
                            }
                            latch.countDown()
                        }
                    },
                )
                latch.await(2500L, java.util.concurrent.TimeUnit.MILLISECONDS)
            }
        }
    }

    fun connectToHost(host: String, port: Int, label: String) {
        stopDiscoveryOnly()
        isClosed = false

        ioJob = scope.launch(Dispatchers.IO) {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(host.trim(), port), 8000)
                if (isClosed) {
                    runCatching { socket.close() }
                    return@launch
                }
                attachSocket(socket, label)
            } catch (e: Exception) {
                if (!isClosed) {
                    withContext(Dispatchers.Main) {
                        onError("اتصال به میزبان وای‌فای برقرار نشد: ${e.localizedMessage ?: "عدم دسترسی"}")
                    }
                }
            }
        }
    }

    private suspend fun attachSocket(socket: Socket, peerLabel: String) {
        clientSocket = socket
        socket.tcpNoDelay = true
        val reader = BufferedReader(InputStreamReader(socket.getInputStream(), Charsets.UTF_8))
        writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream(), Charsets.UTF_8))

        withContext(Dispatchers.Main) {
            onConnected(peerLabel)
        }

        try {
            while (scope.isActive && !isClosed && !socket.isClosed) {
                val line = reader.readLine() ?: break
                val decoded = SyncMessage.decode(line) ?: continue
                withContext(Dispatchers.Main) {
                    onMessage(decoded)
                }
            }
        } catch (_: Exception) {
            // Handled below on stream end
        } finally {
            if (!isClosed) {
                withContext(Dispatchers.Main) {
                    onDisconnected("ارتباط وای‌فای با بازیکن مقابل قطع شد.")
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

    private fun registerNsdService(serviceName: String, port: Int) {
        val manager = nsdManager ?: return
        val info = NsdServiceInfo().apply {
            this.serviceName = serviceName
            this.serviceType = SERVICE_TYPE
            this.port = port
        }
        val listener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(nsdServiceInfo: NsdServiceInfo) = Unit
            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) = Unit
            override fun onServiceUnregistered(arg0: NsdServiceInfo) = Unit
            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) = Unit
        }
        registrationListener = listener
        runCatching {
            manager.registerService(info, NsdManager.PROTOCOL_DNS_SD, listener)
        }
    }

    private fun unregisterNsdService() {
        val listener = registrationListener ?: return
        registrationListener = null
        runCatching { nsdManager?.unregisterService(listener) }
    }

    fun stopDiscoveryOnly() {
        val listener = discoveryListener ?: return
        discoveryListener = null
        runCatching { nsdManager?.stopServiceDiscovery(listener) }
        releaseMulticastLock()
    }

    private fun acquireMulticastLock() {
        runCatching {
            if (multicastLock == null) {
                multicastLock = wifiManager?.createMulticastLock("EsmFamilNsdLock")?.apply {
                    setReferenceCounted(false)
                }
            }
            if (multicastLock?.isHeld == false) {
                multicastLock?.acquire()
            }
        }
    }

    private fun releaseMulticastLock() {
        runCatching {
            if (multicastLock?.isHeld == true) {
                multicastLock?.release()
            }
        }
    }

    fun close() {
        isClosed = true
        ioJob?.cancel()
        ioJob = null
        unregisterNsdService()
        stopDiscoveryOnly()
        releaseMulticastLock()
        runCatching { writer?.close() }
        writer = null
        runCatching { clientSocket?.close() }
        clientSocket = null
        runCatching { serverSocket?.close() }
        serverSocket = null
    }

    private fun resolveLocalIpv4Address(): String? {
        return runCatching {
            val interfaces = NetworkInterface.getNetworkInterfaces()?.toList().orEmpty()
            val candidates = mutableListOf<String>()
            for (iface in interfaces) {
                if (!iface.isUp || iface.isLoopback) continue
                for (addr in iface.inetAddresses.toList()) {
                    if (addr is Inet4Address && !addr.isLoopbackAddress) {
                        val ip = addr.hostAddress ?: continue
                        if (iface.name.contains("wlan", ignoreCase = true) ||
                            iface.name.contains("ap", ignoreCase = true)
                        ) {
                            return@runCatching ip
                        }
                        candidates += ip
                    }
                }
            }
            candidates.firstOrNull()
        }.getOrNull()
    }

    companion object {
        private const val SERVICE_TYPE = "_esmfamil._tcp."
    }
}
