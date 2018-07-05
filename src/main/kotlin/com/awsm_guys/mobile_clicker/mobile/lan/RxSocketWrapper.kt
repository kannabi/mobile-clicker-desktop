package com.awsm_guys.mobile_clicker.mobile.lan

import com.awsm_guys.mobile_clicker.utils.LoggingMixin
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

class RxSocketWrapper(private val socket: Socket): LoggingMixin {

    private val compositeDisposable = CompositeDisposable()

    private val outputStream = socket.getOutputStream()
    private val inputStream = socket.getInputStream()

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
                    val data = ByteArray(2048)
                    var readedBytes = 0
                    while (isListening.get() && readedBytes != -1){
                        readedBytes = inputStream.read(data)
                        if (readedBytes != -1) {
                            println(String(data))
                            emitter.onNext(String(data))
                        } else {
                            emitter.onError(CloseWithoutMessageException())
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

    fun close() {
        inputStream.close()
        outputStream.close()
        socket.close()
        compositeDisposable.clear()
    }
}