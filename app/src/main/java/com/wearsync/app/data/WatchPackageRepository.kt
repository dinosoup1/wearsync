package com.wearsync.app.data

import android.content.Context
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class WatchPackageRepository(private val context: Context) {

    companion object {
        private const val REQUEST_PATH = "/wearsync/request"
        private const val RESPONSE_PATH = "/wearsync/response"
        private const val TIMEOUT_MS = 10_000L
        private const val MAX_RETRIES = 2
    }

    suspend fun getWatchPackages(): Set<String> = withContext(Dispatchers.IO) {
        var lastException: Exception? = null

        repeat(MAX_RETRIES + 1) { attempt ->
            try {
                return@withContext queryWatch()
            } catch (e: Exception) {
                lastException = e
                if (attempt < MAX_RETRIES) {
                    kotlinx.coroutines.delay(1000L * (attempt + 1))
                }
            }
        }

        throw lastException ?: WatchConnectionException("Failed to connect to watch")
    }

    private suspend fun queryWatch(): Set<String> {
        val messageClient = Wearable.getMessageClient(context)
        val nodeClient = Wearable.getNodeClient(context)

        val nodes = nodeClient.connectedNodes.await()
        if (nodes.isEmpty()) {
            throw WatchConnectionException("No connected watch found")
        }

        val targetNode = nodes.first()

        return withTimeout(TIMEOUT_MS) {
            suspendCancellableCoroutine { continuation ->
                val listener = object : MessageClient.OnMessageReceivedListener {
                    override fun onMessageReceived(event: MessageEvent) {
                        if (event.path == RESPONSE_PATH) {
                            messageClient.removeListener(this)
                            val packages = String(event.data)
                                .split("\n")
                                .filter { it.isNotBlank() }
                                .toSet()
                            continuation.resume(packages)
                        }
                    }
                }

                messageClient.addListener(listener)

                continuation.invokeOnCancellation {
                    messageClient.removeListener(listener)
                }

                messageClient.sendMessage(targetNode.id, REQUEST_PATH, byteArrayOf())
                    .addOnFailureListener { e ->
                        messageClient.removeListener(listener)
                        continuation.resumeWithException(
                            WatchConnectionException("Failed to send message: ${e.message}")
                        )
                    }
            }
        }
    }
}

class WatchConnectionException(message: String, cause: Throwable? = null) :
    Exception(message, cause)
