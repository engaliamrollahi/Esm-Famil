package com.example.network

import android.content.Context
import com.example.model.DiscoveredPeer
import com.example.model.LocalConnectionStatus
import com.example.model.LocalLobbyState
import com.example.model.LocalRole
import com.example.model.LocalTransportType
import com.example.model.PlayerAvatar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LocalSyncManager(
    context: Context,
    private val scope: CoroutineScope,
    private val localProfileProvider: () -> Pair<String, PlayerAvatar>,
) {
    private val _lobbyState = MutableStateFlow(LocalLobbyState())
    val lobbyState: StateFlow<LocalLobbyState> = _lobbyState.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<SyncMessage>(extraBufferCapacity = 32)
    val incomingMessages: SharedFlow<SyncMessage> = _incomingMessages.asSharedFlow()

    private val wifiTransport = WifiSyncTransport(
        context = context,
        scope = scope,
        onConnected = { peerLabel -> handleSocketConnected(peerLabel) },
        onMessage = { msg -> handleIncomingMessage(msg) },
        onPeersUpdated = { peers -> updateDiscoveredPeers(peers) },
        onError = { err -> handleTransportError(err) },
        onDisconnected = { reason -> handlePeerDisconnected(reason) },
    )

    private val bluetoothTransport = BluetoothSyncTransport(
        context = context,
        scope = scope,
        onConnected = { peerLabel -> handleSocketConnected(peerLabel) },
        onMessage = { msg -> handleIncomingMessage(msg) },
        onPeersUpdated = { peers -> updateDiscoveredPeers(peers) },
        onError = { err -> handleTransportError(err) },
        onDisconnected = { reason -> handlePeerDisconnected(reason) },
    )

    fun setTransport(transport: LocalTransportType) {
        if (_lobbyState.value.transport == transport) return
        disconnect()
        _lobbyState.value = LocalLobbyState(transport = transport)
    }

    fun isBluetoothSupported(): Boolean = bluetoothTransport.isSupported()
    fun isBluetoothEnabled(): Boolean = bluetoothTransport.isEnabled()
    fun missingBluetoothHostPermissions(): List<String> = bluetoothTransport.missingPermissionsForHost()
    fun missingBluetoothScanPermissions(): List<String> = bluetoothTransport.missingPermissionsForScan()

    fun startHosting() {
        val transport = _lobbyState.value.transport
        closeAllTransports()
        val (playerName, _) = localProfileProvider()
        _lobbyState.update {
            it.copy(
                role = LocalRole.HOST,
                status = LocalConnectionStatus.HOSTING,
                statusDetail = "در حال راه‌اندازی اتاق...",
                discoveredPeers = emptyList(),
                opponentName = null,
                opponentAvatar = null,
                waitingForOpponentAnswers = false,
                errorMessage = null,
            )
        }

        when (transport) {
            LocalTransportType.WIFI -> {
                wifiTransport.startHosting(playerName) { ip, port, registeredName ->
                    _lobbyState.update {
                        it.copy(
                            status = LocalConnectionStatus.HOSTING,
                            roomName = registeredName,
                            hostIpAddress = ip,
                            hostPort = port,
                            statusDetail = "اتاق آماده است • IP: $ip پورت: $port",
                        )
                    }
                }
            }
            LocalTransportType.BLUETOOTH -> {
                bluetoothTransport.startHosting { deviceName ->
                    _lobbyState.update {
                        it.copy(
                            status = LocalConnectionStatus.HOSTING,
                            roomName = deviceName,
                            statusDetail = "منتظر اتصال بلوتوث از دستگاه مقابل ($deviceName)...",
                        )
                    }
                }
            }
        }
    }

    fun startDiscovery() {
        val transport = _lobbyState.value.transport
        closeAllTransports()
        _lobbyState.update {
            it.copy(
                role = LocalRole.GUEST,
                status = LocalConnectionStatus.SCANNING,
                statusDetail = when (transport) {
                    LocalTransportType.WIFI -> "در حال جست‌وجوی بازی‌های وای‌فای هم‌شبکه..."
                    LocalTransportType.BLUETOOTH -> "در حال یافتن دستگاه‌های بلوتوث اطراف..."
                },
                discoveredPeers = emptyList(),
                opponentName = null,
                opponentAvatar = null,
                waitingForOpponentAnswers = false,
                errorMessage = null,
            )
        }

        when (transport) {
            LocalTransportType.WIFI -> wifiTransport.startDiscovery()
            LocalTransportType.BLUETOOTH -> bluetoothTransport.startDiscovery()
        }
    }

    fun connectToPeer(peer: DiscoveredPeer) {
        _lobbyState.update {
            it.copy(
                role = LocalRole.GUEST,
                status = LocalConnectionStatus.CONNECTING,
                statusDetail = "در حال اتصال به ${peer.name}...",
                errorMessage = null,
            )
        }
        when (peer.transport) {
            LocalTransportType.WIFI -> wifiTransport.connectToHost(peer.address, peer.port, peer.name)
            LocalTransportType.BLUETOOTH -> bluetoothTransport.connectToDevice(peer.address, peer.name)
        }
    }

    fun connectDirectWifi(ip: String, port: Int) {
        val cleanIp = ip.trim()
        if (cleanIp.isBlank() || port <= 0 || port > 65535) {
            handleTransportError("آدرس IP یا شماره پورت معتبر نیست.")
            return
        }
        closeAllTransports()
        _lobbyState.update {
            it.copy(
                transport = LocalTransportType.WIFI,
                role = LocalRole.GUEST,
                status = LocalConnectionStatus.CONNECTING,
                statusDetail = "در حال اتصال مستقیم به $cleanIp:$port...",
                errorMessage = null,
            )
        }
        wifiTransport.connectToHost(cleanIp, port, cleanIp)
    }

    fun setWaitingForOpponentAnswers(waiting: Boolean) {
        _lobbyState.update { it.copy(waitingForOpponentAnswers = waiting) }
    }

    suspend fun sendMessage(message: SyncMessage): Boolean {
        return when (_lobbyState.value.transport) {
            LocalTransportType.WIFI -> wifiTransport.send(message)
            LocalTransportType.BLUETOOTH -> bluetoothTransport.send(message)
        }
    }

    fun disconnect() {
        val wasConnected = _lobbyState.value.status == LocalConnectionStatus.CONNECTED
        if (wasConnected) {
            scope.launch {
                runCatching { sendMessage(SyncMessage.LeaveMatch("disconnect")) }
                closeAllTransports()
            }
        } else {
            closeAllTransports()
        }
        val currentTransport = _lobbyState.value.transport
        _lobbyState.value = LocalLobbyState(transport = currentTransport)
    }

    private fun closeAllTransports() {
        wifiTransport.close()
        bluetoothTransport.close()
    }

    private fun handleSocketConnected(peerLabel: String) {
        val (myName, myAvatar) = localProfileProvider()
        _lobbyState.update {
            it.copy(
                status = LocalConnectionStatus.CONNECTED,
                statusDetail = "اتصال برقرار شد",
                opponentName = it.opponentName ?: peerLabel,
                errorMessage = null,
            )
        }
        scope.launch {
            sendMessage(SyncMessage.Hello(playerName = myName, avatarId = myAvatar.name))
        }
    }

    private fun handleIncomingMessage(message: SyncMessage) {
        if (message is SyncMessage.Hello) {
            val avatar = PlayerAvatar.entries.find { it.name == message.avatarId } ?: PlayerAvatar.PEN_MASTER
            _lobbyState.update {
                it.copy(
                    opponentName = message.playerName,
                    opponentAvatar = avatar,
                )
            }
        }
        _incomingMessages.tryEmit(message)
    }

    private fun updateDiscoveredPeers(peers: List<DiscoveredPeer>) {
        _lobbyState.update { it.copy(discoveredPeers = peers) }
    }

    private fun handleTransportError(message: String) {
        _lobbyState.update {
            it.copy(
                status = LocalConnectionStatus.ERROR,
                statusDetail = message,
                waitingForOpponentAnswers = false,
                errorMessage = message,
            )
        }
    }

    private fun handlePeerDisconnected(reason: String) {
        _lobbyState.update {
            it.copy(
                status = LocalConnectionStatus.ERROR,
                statusDetail = reason,
                opponentName = null,
                opponentAvatar = null,
                waitingForOpponentAnswers = false,
                errorMessage = reason,
            )
        }
        _incomingMessages.tryEmit(SyncMessage.LeaveMatch(reason))
    }

    fun release() {
        closeAllTransports()
    }
}
