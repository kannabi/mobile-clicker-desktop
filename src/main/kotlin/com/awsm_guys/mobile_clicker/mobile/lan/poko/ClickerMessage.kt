package com.awsm_guys.mobile_clicker.mobile.lan.poko

data class ClickerMessage (
        var header: Header,
        var body: String,
        var features: MutableMap<String, String>
)

