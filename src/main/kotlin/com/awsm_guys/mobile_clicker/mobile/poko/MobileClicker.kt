package com.awsm_guys.mobile_clicker.mobile.poko

import java.net.InetAddress

data class MobileClicker (
        var inetAddress: InetAddress,
        var port: Int,
        var name: String
)