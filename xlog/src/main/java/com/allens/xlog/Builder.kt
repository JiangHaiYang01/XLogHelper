package com.allens.xlog

import android.content.Context
import com.tencent.mars.xlog.Log
import com.tencent.mars.xlog.Xlog

class Builder(context: Context) {

    companion object {
        //日志的tag
        var tag = "log_tag"
    }

    //是否是debug 模式
    private var debug = true


    //是否打印控制台日志
    private var consoleLogOpen = true

    //设置是否将打印错误日志记录到单独的文件中
    private var errorLogOpen = true

    //默认的位置
    private val defCachePath = context.getExternalFilesDir(null)?.path + "/mmap"

    // mmap 位置 默认缓存的位置
    private var cachePath = defCachePath

    //实际保存的log 位置
    private var logPath = context.getExternalFilesDir(null)?.path + "/logDir"

    //文件名称前缀 例如该值为TEST，生成的文件名为：TEST_20170102.xlog
    private var namePreFix = "log"

    //写入文件的模式
    private var model = LogModel.Async

    //最大文件大小
    //默认情况下，所有日志每天都写入一个文件。可以通过更改max_file_size将日志分割为多个文件。
    //单个日志文件的最大字节大小，默认为0，表示不分割
    private var maxFileSize = 0L

    //日志级别
    //debug 版本下建议把控制台日志打开，日志级别设为 Verbose 或者 Debug, release 版本建议把控制台日志关闭，日志级别使用 Info.
    private var logLevel = LogLevel.LEVEL_INFO

    //通过 python gen_key.py 获取到的公钥
    private var pubKey = ""

    //缓存的天数  一般情况下填0即可。非0表示会在 _cachedir 目录下存放几天的日志。
    private var cacheDays = 0

    fun setCachePath(cachePath: String): Builder {
        this.cachePath = cachePath
        return this
    }

    fun setLogPath(logPath: String): Builder {
        this.logPath = logPath
        return this
    }


    fun setNamePreFix(namePreFix: String): Builder {
        this.namePreFix = namePreFix
        return this
    }

    fun setModel(model: LogModel): Builder {
        this.model = model
        return this
    }

    fun setPubKey(key: String): Builder {
        this.pubKey = key
        return this
    }

    fun setCacheDays(days: Int): Builder {
        this.cacheDays = days
        return this
    }

    fun setDebug(debug: Boolean): Builder {
        this.debug = debug
        return this
    }

    fun setLogLevel(level: LogLevel): Builder {
        this.logLevel = level
        return this
    }

    fun setConsoleLogOpen(consoleLogOpen: Boolean): Builder {
        this.consoleLogOpen = consoleLogOpen
        return this
    }

    fun setErrLogOpen(errorLogOpen: Boolean): Builder {
        this.errorLogOpen = errorLogOpen
        return this
    }

    fun setTag(logTag: String): Builder {
        tag = logTag
        return this
    }

    fun setMaxFileSize(maxFileSize: Long): Builder {
        this.maxFileSize = maxFileSize
        return this
    }

    fun init() {

        if (!debug) {
            //判断如果是release 就强制使用 异步
            model = LogModel.Async
            //日志级别使用 Info
            logLevel = LogLevel.LEVEL_INFO
        }

        if (cachePath.isEmpty()) {
            //cachePath这个参数必传，而且要data下的私有文件目录，例如 /data/data/packagename/files/xlog， mmap文件会放在这个目录，如果传空串，可能会发生 SIGBUS 的crash。
            cachePath = defCachePath
        }


        android.util.Log.i(tag, "Xlog=========================================>")
        android.util.Log.i(
            tag,
            "info" + "\n"
                    +"level:" + logLevel.level + "\n"
                    + "model:" + model.model + "\n"
                    + "cachePath:" + cachePath + "\n"
                    + "logPath:" + logPath + "\n"
                    + "namePreFix:" + namePreFix + "\n"
                    + "cacheDays:" + cacheDays + "\n"
                    + "pubKey:" + pubKey + "\n"
                    + "consoleLogOpen:" + consoleLogOpen + "\n"
                    + "errorLogOpen:" + errorLogOpen + "\n"
                    + "maxFileSize:" + maxFileSize + "\n"
        )

        android.util.Log.i(tag, "Xlog=========================================<")
        Xlog.appenderOpen(
            logLevel.level,
            model.model,
            cachePath,
            logPath,
            namePreFix,
            cacheDays,
            pubKey
        )
        Xlog.setConsoleLogOpen(consoleLogOpen)
//        Xlog.setErrLogOpen(errorLogOpen)
        Xlog.setMaxFileSize(maxFileSize)
        Log.setLogImp(Xlog())
    }


}