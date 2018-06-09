package com.awsm_guys.mobile_clicker.mobile.udp

import com.awsm_guys.mobile_clicker.mobile.MobileClicker
import io.reactivex.Observable
import java.net.InetAddress

class UdpMobileClicker(
        var inetAddress: InetAddress,
        var port: Int,
        var clickerName: String
): MobileClicker {
    override fun init() {}

    override fun getName() = clickerName

    override fun switchPageObservable(): Observable<Int> {
        TODO()
    }

    override fun switchToPage(pageNumber: Int) {}
}