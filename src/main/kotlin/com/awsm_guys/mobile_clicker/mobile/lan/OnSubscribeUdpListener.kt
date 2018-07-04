package com.awsm_guys.mobile_clicker.mobile.lan

import com.awsm_guys.mobile_clicker.utils.LoggingMixin
import io.reactivex.FlowableEmitter
import io.reactivex.FlowableOnSubscribe
import io.reactivex.disposables.Disposables
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean


class OnSubscribeUdpListener(
        private val port: Int
): FlowableOnSubscribe<DatagramPacket>, LoggingMixin {
    private val emitters = ConcurrentLinkedQueue<FlowableEmitter<DatagramPacket>>()

    private var socket: DatagramSocket? = null
    private val isListening = AtomicBoolean(false)
    private val lock = Any()

    override fun subscribe(emitter: FlowableEmitter<DatagramPacket>) {
        synchronized(lock) {
            if (!emitter.isCancelled) {
                if (emitters.isEmpty()){
                    socket = getSocket()
                }
                emitters.add(emitter)

                emitter.setDisposable(Disposables.fromAction {
                    emitters.remove(emitter)
                    if (emitters.isEmpty()) {
                        isListening.set(false)
                    }
                })
            }
        }

        if (!isListening.get()){
            startListening()
        }
    }

    private fun startListening() {
        isListening.set(true)
        val receiveData = ByteArray(2048)
        log("start listening")
        var datagramPacket = DatagramPacket(receiveData, receiveData.size)
        while (isListening.get()) {
            socket?.receive(datagramPacket)
            for(emitter in emitters) {
                log(String(datagramPacket.data))
                emitter.onNext(datagramPacket)
            }
            datagramPacket = DatagramPacket(receiveData, receiveData.size)
        }
    }

    fun stop() {
        isListening.set(false)
        socket?.close()
        socket = null
    }

    private fun getSocket() =
            DatagramSocket(port).apply {
                broadcast = true
                reuseAddress = true
            }
}