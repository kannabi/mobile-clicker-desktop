package com.awsm_guys.mobile_clicker.presentation.clicker.lan

import com.awsm_guys.mobile_clicker.utils.LoggingMixin
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.io.Closeable
import java.net.Socket
import java.net.SocketException

class RxSocketWrapper(
        private val socket: Socket,
        private var bufferSize: Int = 2048
): LoggingMixin, Closeable {

    private val MESSAGE_END: ByteArray = byteArrayOf(-1)

    private val compositeDisposable = CompositeDisposable()

    private val outputStream = socket.getOutputStream()
        get() = synchronized(lock) { return field }
    private val inputStream = socket.getInputStream()

    private val lock = Any()
    private val dataSubject = PublishSubject.create<String>()
        get() = synchronized(lock) { return field }

    val inputObservable: Observable<String> by lazy {

        compositeDisposable.add(
                Completable.fromCallable {
                    try {
                        val data = ByteArray(bufferSize)
                        while (inputStream.read(data) != -1) {
                            processData(data)
                            data.fill(0)
                        }
                    } catch (e: SocketException) {
                        dataSubject.onComplete()
                    } catch (e: Exception) {
                        trace(e)
                        dataSubject.onError(e)
                    }
                }.subscribeOn(Schedulers.io())
                        .subscribe(dataSubject::onComplete, dataSubject::onError)
        )

        return@lazy dataSubject.hide()
    }

    private val stringBuilder = StringBuilder()

    private fun processData(data: ByteArray) {
        val messageEnd = data.indexOf(MESSAGE_END[0])
        val isMessageEnd = messageEnd != -1

        stringBuilder.append(
                String(data, 0, if (isMessageEnd) messageEnd else bufferSize)
        )
        if (isMessageEnd) {
            dataSubject.onNext(stringBuilder.toString())
            stringBuilder.setLength(0)
        }
    }

    fun sendData(data: String) {
        compositeDisposable.add(
                Completable.fromCallable {
                    outputStream.write(data.toByteArray())
                    outputStream.write(MESSAGE_END)
                    outputStream.flush()
                }.subscribeOn(Schedulers.io())
                        .subscribe({log("send $data")}, ::trace)
        )
    }

    fun sendData(data: String, completeCallback: () -> Unit) {
        compositeDisposable.add(
                Completable.fromCallable {
                    outputStream.write(data.toByteArray())
                    outputStream.flush()
                }.subscribeOn(Schedulers.io())
                        .subscribe(completeCallback, ::trace)
        )
    }

    override fun close() {
        inputStream.close()
        outputStream.close()
        socket.close()
        compositeDisposable.clear()
    }
}