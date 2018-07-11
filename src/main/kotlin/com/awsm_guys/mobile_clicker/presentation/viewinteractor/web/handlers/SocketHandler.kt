package com.awsm_guys.mobile_clicker.presentation.viewinteractor.web.handlers

import com.awsm_guys.mobile_clicker.presentation.viewinteractor.web.WebSocketListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler


@Component
class SocketHandler: TextWebSocketHandler() {

    @Autowired
    private lateinit var webSocketListener: WebSocketListener

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        extractData(message.payload)?.let { webSocketListener.onMessageReceived(it, session) }
    }

    override fun afterConnectionEstablished(session: WebSocketSession) =
            webSocketListener.onConnected(session)

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) =
            webSocketListener.onDisconnected(session)

    private fun extractData(payload: String): String? {
        val parts = payload.split("\n")
        return if (parts[0] == "SEND") parts[parts.lastIndex] else null
    }
}