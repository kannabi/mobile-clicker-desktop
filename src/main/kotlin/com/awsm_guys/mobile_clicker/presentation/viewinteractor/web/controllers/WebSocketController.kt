package com.awsm_guys.mobile_clicker.presentation.viewinteractor.web.controllers

import com.awsm_guys.mobile_clicker.presentation.poko.Message
import com.awsm_guys.mobile_clicker.presentation.viewinteractor.web.WebSocketListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller


@Controller
class WebSocketController {
    @Autowired
    private lateinit var webSocketListener: WebSocketListener

    @MessageMapping("/send")
    fun receiveMessage(message: Message) {
        webSocketListener.onMessageReceived(message = message)
    }
}