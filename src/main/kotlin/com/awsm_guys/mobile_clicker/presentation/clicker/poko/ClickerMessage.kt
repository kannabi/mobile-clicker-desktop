package com.awsm_guys.mobile_clicker.presentation.clicker.poko

data class ClickerMessage (
        var header: Header,
        var body: String,
        var features: MutableMap<String, String>
)

