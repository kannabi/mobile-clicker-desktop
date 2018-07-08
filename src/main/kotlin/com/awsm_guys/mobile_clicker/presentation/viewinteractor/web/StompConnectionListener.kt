package com.awsm_guys.mobile_clicker.presentation.viewinteractor.web

import org.springframework.web.socket.messaging.SessionDisconnectEvent

interface StompConnectionListener {
    fun onDisconnect(event: SessionDisconnectEvent)
}