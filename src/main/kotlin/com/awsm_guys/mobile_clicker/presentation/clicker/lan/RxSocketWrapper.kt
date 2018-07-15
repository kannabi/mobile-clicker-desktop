package com.awsm_guys.mobile_clicker.presentation.clicker.lan

import com.awsm_guys.mobile_clicker.utils.LoggingMixin
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.net.Socket
import java.net.SocketException

class RxSocketWrapper(private val socket: Socket): LoggingMixin {

    private val compositeDisposable = CompositeDisposable()

    private val outputStream = socket.getOutputStream()
    private val inputStream = socket.getInputStream()

    private val lock = Any()
    private val dataSubject = PublishSubject.create<String>()
        get() {
            synchronized(lock) {
                return field
            }
        }

    val inputObservable: Observable<String> by lazy {

        compositeDisposable.add(
                Completable.fromCallable {
                    try {
                        val data = ByteArray(512)
                        while (inputStream.read(data) != -1){
                            dataSubject.onNext(String(data))
                        }
                    } catch (e: SocketException) {
                        dataSubject.onComplete()
                    } catch (e: Throwable){
                        trace(e)
                        dataSubject.onError(e)
                    }
                }.subscribeOn(Schedulers.io())
                        .subscribe(dataSubject::onComplete, dataSubject::onError)
        )

        return@lazy dataSubject.hide()
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