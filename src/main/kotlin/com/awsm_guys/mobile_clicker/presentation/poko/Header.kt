package com.awsm_guys.mobile_clicker.presentation.poko

import com.fasterxml.jackson.annotation.JsonValue

enum class Header(@get:JsonValue val header: String) {
    CONNECT("CONNECT"),
    DISCONNECT("DISCONNECT"),
    OK("OK"),
    SWITCH_PAGE("SWITCH_PAGE")
}