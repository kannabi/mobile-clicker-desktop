package com.awsm_guys.mobile_clicker.presentation.viewinteractor.web

import com.awsm_guys.mobile_clicker.presentation.poko.Message
import org.springframework.web.socket.messaging.SessionConnectEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent

interface WebSocketListener {

    fun onConnected(event: SessionConnectEvent)

    fun onDisconnected(event: SessionDisconnectEvent)

    fun onMessageReceived(message: Message)
}