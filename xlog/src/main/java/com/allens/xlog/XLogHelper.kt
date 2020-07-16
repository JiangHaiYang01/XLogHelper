package com.allens.xlog

import android.content.Context
import com.tencent.mars.xlog.Log

object XLogHelper {


    init {
        System.loadLibrary("c++_shared")
        System.loadLibrary("marsxlog")
    }


    fun create(context: Context): Builder {
        return Builder(context)
    }


    fun i(msg: String) {
        i(msg, null)
    }


    fun i(format: String, vararg obj: Any?) {
        Log.i(Builder.tag, format, *obj)
    }


}