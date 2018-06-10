package com.awsm_guys.mobile_clicker.mobile.udp

import com.awsm_guys.mobile_clicker.mobile.MobileClicker
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.net.InetAddress

class UdpMobileClicker(
        var inetAddress: String,
        var port: Int,
        var clickerName: String
): MobileClicker {

    private var clickerPort = 17710

    private var switchPageSubject = PublishSubject.create<Int>()

    override fun init() {

    }

    override fun getName() = clickerName

    override fun switchPageObservable(): Observable<Int> =
            switchPageSubject.hide()

    override fun switchToPage(pageNumber: Int) {}

    override fun disconnect() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}