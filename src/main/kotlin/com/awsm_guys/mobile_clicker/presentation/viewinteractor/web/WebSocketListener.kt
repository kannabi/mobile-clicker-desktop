package com.awsm_guys.mobile_clicker.presentation.viewinteractor.web

import org.springframework.web.socket.WebSocketSession

interface WebSocketListener {

    fun onConnected(session: WebSocketSession)

    fun onDisconnected(session: WebSocketSession)

    fun onMessageReceived(message: String, session: WebSocketSession)
}