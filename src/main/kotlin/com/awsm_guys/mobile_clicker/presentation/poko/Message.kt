package com.awsm_guys.mobile_clicker.presentation.poko

data class Message (
        var header: Header,
        var body: String,
        var features: MutableMap<String, String>
)

