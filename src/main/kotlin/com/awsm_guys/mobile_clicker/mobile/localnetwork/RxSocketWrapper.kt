package com.awsm_guys.mobile_clicker.mobile.localnetwork

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

class RxSocketWrapper(private val socket: Socket) {
    val inputObservable: Observable<String> by lazy {
        Observable.create(object : ObservableOnSubscribe<String> {
            private val isListening = AtomicBoolean(false)

            private lateinit var emitter: ObservableEmitter<String>
            override fun subscribe(emitter: ObservableEmitter<String>) {
                this.emitter = emitter
                isListening.set(true)
                startListening()
            }

            private fun startListening(){
                try {
                    BufferedReader(InputStreamReader(socket.getInputStream())).use {
                        var data: String?
                        while (isListening.get()){
                            data = it.readLine()
                            if (data == null) {
                                emitter.onNext(data)
                            } else {
                                emitter.onComplete()
                            }
                        }
                    }
                } catch (e: Throwable){
                    emitter.onError(e)
                }
            }
        })
    }
    private val outputPrintWriter by lazy { PrintWriter(socket.getOutputStream(), true) }

    fun sendData(data: String) =
            outputPrintWriter.write(data)

    fun close() = socket.close()
}