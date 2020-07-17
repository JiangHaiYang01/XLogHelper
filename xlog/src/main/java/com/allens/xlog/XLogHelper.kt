package com.allens.xlog

import android.content.Context
import com.tencent.mars.xlog.Log
import com.tencent.mars.xlog.Xlog

object XLogHelper {


    init {
        System.loadLibrary("c++_shared")
        System.loadLibrary("marsxlog")
    }


    //创建
    fun create(context: Context): Builder {
        return Builder(context)
    }

    //=====================================================================================
    // v
    //=====================================================================================
    fun v(msg: String) {
        v(msg, null)
    }

    fun v(tag: String, msg: String) {
        v(tag, msg, null)
    }

    fun v(format: String, vararg obj: Any?) {
        v(Builder.tag, format, *obj)
    }

    fun v(tag: String, format: String, vararg obj: Any?) {
        Log.v(tag, format, *obj)
    }

    //=====================================================================================
    // f
    //=====================================================================================
    fun f(msg: String) {
        f(msg, null)
    }

    fun f(tag: String, msg: String) {
        f(tag, msg, null)
    }

    fun f(format: String, vararg obj: Any?) {
        f(Builder.tag, format, *obj)
    }

    fun f(tag: String, format: String, vararg obj: Any?) {
        Log.f(tag, format, *obj)
    }


    //=====================================================================================
    // e
    //=====================================================================================
    fun e(msg: String) {
        e(msg, null)
    }

    fun e(tag: String, msg: String) {
        e(tag, msg, null)
    }

    fun e(format: String, vararg obj: Any?) {
        e(Builder.tag, format, *obj)
    }

    fun e(tag: String, format: String, vararg obj: Any?) {
        Log.e(tag, format, *obj)
    }

    //=====================================================================================
    // w
    //=====================================================================================
    fun w(msg: String) {
        w(msg, null)
    }

    fun w(tag: String, msg: String) {
        w(tag, msg, null)
    }

    fun w(format: String, vararg obj: Any?) {
        w(Builder.tag, format, *obj)
    }

    fun w(tag: String, format: String, vararg obj: Any?) {
        Log.w(tag, format, *obj)
    }

    //=====================================================================================
    // w
    //=====================================================================================
    fun i(msg: String) {
        i(msg, null)
    }

    fun i(tag: String, msg: String) {
        i(tag, msg, null)
    }

    fun i(format: String, vararg obj: Any?) {
        i(Builder.tag, format, *obj)
    }

    fun i(tag: String, format: String, vararg obj: Any?) {
        Log.i(tag, format, *obj)
    }

    //=====================================================================================
    // d
    //=====================================================================================
    fun d(msg: String) {
        d(msg, null)
    }

    fun d(tag: String, msg: String) {
        d(tag, msg, null)
    }

    fun d(format: String, vararg obj: Any?) {
        d(Builder.tag, format, *obj)
    }

    fun d(tag: String, format: String, vararg obj: Any?) {
        Log.d(tag, format, *obj)
    }

    //关闭日志，不再写入
    fun close() {
        Log.appenderClose()
    }

    //当日志写入模式为异步时，调用该接口会把内存中的日志写入到文件。
    //isSync : true  为同步 flush，flush 结束后才会返回。
    //isSync : false 为异步 flush，不等待 flush 结束就返回。
    fun flush(isSync: Boolean) {
        Log.appenderFlush(isSync)
    }


}