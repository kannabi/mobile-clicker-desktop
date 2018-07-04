package com.awsm_guys.mobile_clicker.mobile.localnetwork

import com.awsm_guys.mobile_clicker.utils.LoggingMixin
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

class RxSocketWrapper(private val socket: Socket): LoggingMixin {

    private val compositeDisposable = CompositeDisposable()

    private val outputStream = socket.getOutputStream()
    private val inputReader = BufferedReader(InputStreamReader(socket.getInputStream()))

    val inputObservable: Observable<String> by lazy {
        Observable.create(object : ObservableOnSubscribe<String> {
            private val isListening = AtomicBoolean(false)

            private lateinit var emitter: ObservableEmitter<String>
            override fun subscribe(emitter: ObservableEmitter<String>) {
                this.emitter = emitter
                isListening.set(true)
                startListening()
            }

            private fun startListening() {
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
                    trace(e)
                    emitter.onError(e)
                }
            }
        })
    }

    fun sendData(data: String) {
        compositeDisposable.add(
                Single.fromCallable {
                    outputStream.write(data.toByteArray())
                    outputStream.flush()
                }.subscribeOn(Schedulers.io())
                        .subscribe({log("send $data")}, ::trace)
        )
    }

    fun close(){
        socket.close()
        inputReader.close()
        outputStream.close()
        compositeDisposable.clear()
    }
}