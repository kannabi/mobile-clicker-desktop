package com.awsm_guys.mobile_clicker.presentation.viewinteractor.web.config

import com.awsm_guys.mobile_clicker.presentation.viewinteractor.web.handlers.SocketHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry



@Configuration
@EnableWebSocket
class WebSocketConfig : WebSocketConfigurer {

    @Autowired
    private lateinit var socketHandler: SocketHandler

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(socketHandler, "/listener").withSockJS()
    }
}
