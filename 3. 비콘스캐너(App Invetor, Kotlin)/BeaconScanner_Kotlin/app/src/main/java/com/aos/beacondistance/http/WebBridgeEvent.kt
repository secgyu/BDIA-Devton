package com.aos.beacondistance.http

import android.webkit.JavascriptInterface

object WebBridgeEvent {

    var listener: BridgeListener? = null

    @JavascriptInterface
    fun setMemberInfo(mdata: String, name: String, type: String) {
        listener?.login(mdata, name, type)
    }

    interface BridgeListener {
        fun login(data: String, name: String, type: String)
    }
}
