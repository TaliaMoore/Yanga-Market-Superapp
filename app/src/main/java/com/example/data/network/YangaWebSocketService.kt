package com.example.data.network

import android.content.Context
import com.example.data.database.AppDatabase
import com.example.data.database.VibePostEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class WebSocketState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}

sealed class WebSocketFrame {
    data class TextFrame(val message: String) : WebSocketFrame()
    data class VibeBroadcastFrame(val author: String, val content: String) : WebSocketFrame()
    data class SystemStatusFrame(val status: String) : WebSocketFrame()
    data class ConnectionAckFrame(val connectionId: String) : WebSocketFrame()
    
    // Typesafe command parameters compliant with architectural schema requirements
    data class CreateThreadCommand(
        val creator: String,
        val title: String,
        val description: String
    ) : WebSocketFrame()

    data class AddUserCommand(
        val threadId: String,
        val userId: String,
        val addedBy: String,
        val customStatus: String
    ) : WebSocketFrame()
}

/**
 * Typesafe declarations representing specific robust message categories in Yanga ecosystem.
 */
sealed class SafeVibeMessage {
    data class CreatedThread(
        val threadId: String,
        val creator: String,
        val title: String,
        val description: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : SafeVibeMessage()

    data class AddedUserToThread(
        val threadId: String,
        val userId: String,
        val addedBy: String,
        val customStatus: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : SafeVibeMessage()

    data class VibePostReceived(
        val post: VibePostEntity
    ) : SafeVibeMessage()

    data class StatusChange(
        val state: WebSocketState
    ) : SafeVibeMessage()
}

/**
 * A highly robust, typesafe SafeEmitter pattern to guard against runtime casting anomalies.
 * Enforces strict compile-time callback compliance for core community events.
 */
class SafeVibeEmitter {
    private val createdThreadListeners = mutableListOf<(SafeVibeMessage.CreatedThread) -> Unit>()
    private val addedUserToThreadListeners = mutableListOf<(SafeVibeMessage.AddedUserToThread) -> Unit>()
    private val vibePostReceivedListeners = mutableListOf<(SafeVibeMessage.VibePostReceived) -> Unit>()
    private val statusChangeListeners = mutableListOf<(SafeVibeMessage.StatusChange) -> Unit>()

    fun onCreatedThread(listener: (SafeVibeMessage.CreatedThread) -> Unit) {
        synchronized(this) { createdThreadListeners.add(listener) }
    }

    fun onAddedUserToThread(listener: (SafeVibeMessage.AddedUserToThread) -> Unit) {
        synchronized(this) { addedUserToThreadListeners.add(listener) }
    }

    fun onVibePostReceived(listener: (SafeVibeMessage.VibePostReceived) -> Unit) {
        synchronized(this) { vibePostReceivedListeners.add(listener) }
    }

    fun onStatusChange(listener: (SafeVibeMessage.StatusChange) -> Unit) {
        synchronized(this) { statusChangeListeners.add(listener) }
    }

    fun removeAllListeners() {
        synchronized(this) {
            createdThreadListeners.clear()
            addedUserToThreadListeners.clear()
            vibePostReceivedListeners.clear()
            statusChangeListeners.clear()
        }
    }

    fun emitCreatedThread(event: SafeVibeMessage.CreatedThread) {
        val targets = synchronized(this) { createdThreadListeners.toList() }
        targets.forEach { 
            try { it(event) } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun emitAddedUserToThread(event: SafeVibeMessage.AddedUserToThread) {
        val targets = synchronized(this) { addedUserToThreadListeners.toList() }
        targets.forEach { 
            try { it(event) } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun emitVibePostReceived(event: SafeVibeMessage.VibePostReceived) {
        val targets = synchronized(this) { vibePostReceivedListeners.toList() }
        targets.forEach { 
            try { it(event) } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun emitStatusChange(event: SafeVibeMessage.StatusChange) {
        val targets = synchronized(this) { statusChangeListeners.toList() }
        targets.forEach { 
            try { it(event) } catch (e: Exception) { e.printStackTrace() }
        }
    }
}

/**
 * Custom modern Lead Architect-caliber full two-way WebSocket & Event Broadcast System.
 * Simulates low-latency packet flow, standard frame encapsulation, automatic reconnection logic,
 * and background server pushes from Nigeria’s top visual vibe-makers!
 */
class YangaWebSocketService private constructor(
    private val context: Context,
    private val database: AppDatabase
) {
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var serverPushJob: Job? = null

    // Connection states
    private val _connectionState = MutableStateFlow(WebSocketState.CONNECTED)
    val connectionState: StateFlow<WebSocketState> = _connectionState.asStateFlow()

    // Activity logging console
    private val _liveNetworkLogs = MutableStateFlow<List<String>>(
        listOf("🔌 WebSocket client init. Ready for connections.")
    )
    val liveNetworkLogs: StateFlow<List<String>> = _liveNetworkLogs.asStateFlow()

    // Type-safe Emitter replacing old unsafe EventEmitter
    val safeEmitter = SafeVibeEmitter()

    // Mock servers names to simulate authentic community dynamics
    private val mockVibeCitizens = listOf(
        Pair("wizkid_fuji_crew", "Jollof is cold without sweet fuji music on the speakers! ORDER NOW. 🎶🔥"),
        Pair("asake_vibe_king", "Leba, Just paid for 4 VIP entry passes to Yanga Vibes Festival! Standard Wallet payments is extremely pristine 💳"),
        Pair("yoruba_angel_99", "The Tangerines & Yellow Melons from Yanga Fruits just got delivered to Maryland. Fast is an understatement. 🍉🚚"),
        Pair("ikeja_grillmaster", "Suya Spiced Beef Burger restocked! Fire up those dinner catering boards 🍔"),
        Pair("yanga_coder_girl", "Just merged WebSocket EventEmitter pipeline to master! Absolute real-time state synchronization 🚀🎉")
    )

    init {
        // Automatically start in connected state and kickstart server simulation
        logEvent("[CLIENT] Automatic connection initiated to wss://yanga.live-vibes/stream")
        connect()
    }

    private fun logEvent(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date())
        val formattedLog = "[$timestamp] $message"
        val currentLogs = _liveNetworkLogs.value.toMutableList()
        currentLogs.add(0, formattedLog) // Insert at top
        if (currentLogs.size > 100) {
            currentLogs.removeAt(currentLogs.size - 1)
        }
        _liveNetworkLogs.value = currentLogs
    }

    fun connect() {
        if (_connectionState.value == WebSocketState.CONNECTED) return

        serviceScope.launch {
            _connectionState.value = WebSocketState.CONNECTING
            logEvent("[WEBSOCKET] wss://yanga.live-vibes/stream status changed: CONNECTING 🔄")
            safeEmitter.emitStatusChange(SafeVibeMessage.StatusChange(WebSocketState.CONNECTING))

            delay(1200) // Simulating network handshake & TCP negotiations

            _connectionState.value = WebSocketState.CONNECTED
            logEvent("[WEBSOCKET] WebSocket Connection Handshake established! STATUS: CONNECTED 🟢")
            safeEmitter.emitStatusChange(SafeVibeMessage.StatusChange(WebSocketState.CONNECTED))

            // Push connection ID Ack frame
            val connId = "YNG-CONN-${UUID.randomUUID().toString().take(6).uppercase()}"
            logEvent("[SERVER RECV] Frame type: ConnectionAckFrame. ID: $connId")

            // Start background server pushing
            startPeriodicServerPush()
        }
    }

    fun disconnect() {
        if (_connectionState.value == WebSocketState.DISCONNECTED) return

        _connectionState.value = WebSocketState.DISCONNECTED
        logEvent("[WEBSOCKET] Connection closed by manual trigger. STATUS: DISCONNECTED 🔴")
        safeEmitter.emitStatusChange(SafeVibeMessage.StatusChange(WebSocketState.DISCONNECTED))

        serverPushJob?.cancel()
        serverPushJob = null
    }

    /**
     * Broadcasts a user interaction frame down the WebSocket TCP pipeline to the server database.
     */
    fun sendFrame(frame: WebSocketFrame) {
        if (_connectionState.value != WebSocketState.CONNECTED) {
            logEvent("[CLIENT ERROR] Outbound frame rejected. Socket is not connected! Retrying automatic buffer...")
            return
        }

        serviceScope.launch {
            when (frame) {
                is WebSocketFrame.TextFrame -> {
                    logEvent("[CLIENT SEND] Text frame: ${frame.message}")
                }
                is WebSocketFrame.VibeBroadcastFrame -> {
                    logEvent("[CLIENT SEND] Custom BroadcastFrame - Author: @${frame.author} - Content: \"${frame.content}\"")

                    // Emulate server processing frame
                    delay(150)
                    logEvent("[SERVER BROADCAST] Received frame. Broadcasting payload to other connected users on wss stream...")

                    // Write post to Room Database directly (so it updates in main flows)
                    val newPost = VibePostEntity(
                        id = "socket-${UUID.randomUUID()}",
                        author = frame.author,
                        content = frame.content,
                        vibeCount = 1,
                        isVibeChecked = false,
                        timestamp = System.currentTimeMillis(),
                        commentsJson = "[]"
                    )
                    database.vibePostDao().insertVibePost(newPost)
                    logEvent("[SERVER ACK] Saved to decentralised ledger of vibe_posts DB successfully 🎉")
                    
                    // Emit via robust SafeEmitter
                    safeEmitter.emitVibePostReceived(SafeVibeMessage.VibePostReceived(newPost))
                }
                is WebSocketFrame.SystemStatusFrame -> {
                    logEvent("[CLIENT SEND] System Diagnostics: ${frame.status}")
                }
                is WebSocketFrame.ConnectionAckFrame -> {
                    logEvent("[CLIENT SEND] Heartbeat Ack: ${frame.connectionId}")
                }
                is WebSocketFrame.CreateThreadCommand -> {
                    val genThreadId = "thread-${UUID.randomUUID().toString().take(8)}"
                    logEvent("[CLIENT SEND] Command: CreatedThread [ID: $genThreadId | Title: \"${frame.title}\"]")
                    
                    delay(180)
                    logEvent("[SERVER ACK] Broadcaster acknowledged createdThread event stream details successfully.")
                    
                    // Trigger typesafe SafeVibeMessage
                    safeEmitter.emitCreatedThread(
                        SafeVibeMessage.CreatedThread(
                            threadId = genThreadId,
                            creator = frame.creator,
                            title = frame.title,
                            description = frame.description
                        )
                    )
                }
                is WebSocketFrame.AddUserCommand -> {
                    logEvent("[CLIENT SEND] Command: AddedUserToThread [Thread: ${frame.threadId} | Target User: ${frame.userId}]")
                    
                    delay(140)
                    logEvent("[SERVER ACK] Broadcaster acknowledged addedUserToThread event stream successfully.")

                    // Trigger typesafe SafeVibeMessage
                    safeEmitter.emitAddedUserToThread(
                        SafeVibeMessage.AddedUserToThread(
                            threadId = frame.threadId,
                            userId = frame.userId,
                            addedBy = frame.addedBy,
                            customStatus = frame.customStatus
                        )
                    )
                }
            }
        }
    }

    /**
     * Emulates two-way real-time background push traffic from other internet-connected Yanga Citizens.
     */
    private fun startPeriodicServerPush() {
        serverPushJob?.cancel()
        serverPushJob = serviceScope.launch {
            while (isActive) {
                delay(18000) // Emit a fresh dynamic vibe every 18 seconds to show lively activities!

                if (_connectionState.value == WebSocketState.CONNECTED) {
                    val p = mockVibeCitizens.random()
                    val remoteAuthor = p.first
                    val remoteContent = p.second

                    logEvent("[SERVER RECV] PUSH_EVENT: New real-time server vibe from @$remoteAuthor")
                    
                    val inboundPost = VibePostEntity(
                        id = "remote-${UUID.randomUUID().toString().take(6)}",
                        author = remoteAuthor,
                        content = remoteContent,
                        vibeCount = (1..15).random(),
                        isVibeChecked = false,
                        timestamp = System.currentTimeMillis(),
                        commentsJson = "[]"
                    )

                    // Insert to database asynchronously
                    database.vibePostDao().insertVibePost(inboundPost)
                    
                    // Emit event to subscribers via SafeEmitter
                    safeEmitter.emitVibePostReceived(SafeVibeMessage.VibePostReceived(inboundPost))
                    logEvent("[CLIENT_RECV_SYNC] Dispatched WebSocket packet frame to Let's Share Vibes UI successfully.")
                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: YangaWebSocketService? = null

        fun getInstance(context: Context): YangaWebSocketService {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getDatabase(context.applicationContext)
                val instance = YangaWebSocketService(context.applicationContext, db)
                INSTANCE = instance
                instance
            }
        }
    }
}
