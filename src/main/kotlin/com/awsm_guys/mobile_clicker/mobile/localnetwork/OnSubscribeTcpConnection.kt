package com.awsm_guys.mobile_clicker.mobile.localnetwork

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import java.net.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

class OnSubscribeTcpConnection(
        localPort: Int,
        targetAddress: String,
        targetPort: Int,
        message: ByteArray
): ObservableOnSubscribe<Socket> {
    private val emitters by lazy { ConcurrentLinkedQueue<ObservableEmitter<Socket>>() }
    private val lock by lazy { Any() }
    private val serverSocket by lazy { ServerSocket(localPort) }
    private val datagramSocket by lazy { DatagramSocket() }
    private val compositeDisposable by lazy { CompositeDisposable() }

    private val datagramPacket = DatagramPacket(
            message,
            message.size,
            InetAddress.getByName(targetAddress),
            targetPort
    )

    override fun subscribe(emitter: ObservableEmitter<Socket>) {
        synchronized(lock, {
            if (!emitter.isDisposed) {
                if (emitters.isEmpty()){
                    startConnectionProcess()
                }
                emitters.add(emitter)
                emitter.setDisposable(Disposables.fromAction {
                    emitters.remove(emitter)
                    if (emitters.isEmpty()){
                        stopConnectionProcess()
                    }
                })
            }
        })
    }

    private fun startConnectionProcess() {
        compositeDisposable.add(
            Observable.interval(500, TimeUnit.MILLISECONDS)
                    .take(8)
                    .map { datagramPacket }
                    .subscribe(
                            datagramSocket::send,
                            this::onError,
                            this::stopConnectionProcess
                    )
        )

        compositeDisposable.add(
            Observable.fromCallable(serverSocket::accept)
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                        this::emit,
                        this::onError
                    )
        )

    }

    private fun stopConnectionProcess() {
        compositeDisposable.clear()
        serverSocket.close()
        datagramSocket.close()
    }

    private fun emit(socket: Socket){
        stopConnectionProcess()
        for (emitter in emitters) {
            emitter.onNext(socket)
            emitter.onComplete()
        }
    }

    private fun onError(throwable: Throwable){
        for (emitter in emitters) {
            emitter.onError(throwable)
        }
        stopConnectionProcess()
    }
}