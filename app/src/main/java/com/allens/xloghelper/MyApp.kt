package com.allens.xloghelper

import android.app.Application
import com.allens.xlog.LogLevel
import com.allens.xlog.LogModel
import com.allens.xlog.XLogHelper

class MyApp :Application(){

    override fun onCreate() {
        super.onCreate()
        XLogHelper.create(this)
            .setModel(LogModel.Async)
            .setTag("TAG")
            .setConsoleLogOpen(false)
            .setLogLevel(LogLevel.LEVEL_INFO)
            .setNamePreFix("log")
            .setPubKey("013eb2d1dc2a03c3fdaffa2d55425abd292fc3d0b3fdea822113f95ad1f770921682017a5ba407037900eaa519a52187df57a8e06d0d3a760361d18a82193268")
            .setMaxFileSize(1f)
            .setOneFileEveryday(true)
            .setCacheDays(0)
            .setMaxAliveTime(2)
            .init()
    }
}