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

    fun v(msg: String) {
        v(msg, null)
    }


    fun v(format: String, vararg obj: Any?) {
        Log.v(Builder.tag, format, *obj)
    }


    fun f(msg: String) {
        f(msg, null)
    }


    fun f(format: String, vararg obj: Any?) {
        Log.f(Builder.tag, format, *obj)
    }


    fun e(msg: String) {
        e(msg, null)
    }


    fun e(format: String, vararg obj: Any?) {
        Log.e(Builder.tag, format, *obj)
    }


    fun w(msg: String) {
        w(msg, null)
    }


    fun w(format: String, vararg obj: Any?) {
        Log.w(Builder.tag, format, *obj)
    }


    fun i(msg: String) {
        i(msg, null)
    }


    fun i(format: String, vararg obj: Any?) {
        Log.i(Builder.tag, format, *obj)
    }


    fun d(msg: String) {
        d(msg, null)
    }


    fun d(format: String, vararg obj: Any?) {
        Log.d(Builder.tag, format, *obj)
    }


}