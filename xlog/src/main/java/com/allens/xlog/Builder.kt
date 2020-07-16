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


    //是否每天一个日志文件
    private var oneFileEveryday = true

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
    // 最大 当文件不能超过 10M
    private var maxFileSize = 0L

    //日志级别
    //debug 版本下建议把控制台日志打开，日志级别设为 Verbose 或者 Debug, release 版本建议把控制台日志关闭，日志级别使用 Info.
    private var logLevel = LogLevel.LEVEL_INFO

    //通过 python gen_key.py 获取到的公钥
    private var pubKey = ""

    //单个文件最大保留时间 最小 1天 默认时间 10天
    private var maxAliveTime = 10

    //缓存的天数  一般情况下填0即可。非0表示会在 _cachedir 目录下存放几天的日志。
    //原来缓存日期的意思是几天后从缓存目录移到日志目录
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

    //原来缓存日期的意思是几天后从缓存目录移到日志目录 默认 0 即可
    //如果想让文件保留多少天 用 [setMaxAliveTime] 方法即可
    //大于 0  的时候 默认会放在缓存的位置上 [cachePath]
    fun setCacheDays(days: Int): Builder {
        if (days < 0) {
            this.cacheDays = 0
        } else {
            this.cacheDays = days
        }
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


    fun setTag(logTag: String): Builder {
        tag = logTag
        return this
    }


    /**
     * [isOpen]  true   设置每天一个日志文件
     *           false  那么  [setMaxFileSize] 生效
     */
    fun setOneFileEveryday(isOpen: Boolean): Builder {
        this.oneFileEveryday = isOpen
        return this
    }

    fun setMaxFileSize(maxFileSize: Float): Builder {
        when {
            maxFileSize < 0 -> {
                this.maxFileSize = 0L
            }
            maxFileSize > 10 -> {
                this.maxFileSize = (10 * 1024 * 1024).toLong()
            }
            else -> {
                this.maxFileSize = (maxFileSize * 1024 * 1024).toLong()
            }
        }
        return this
    }

    /**
     * [day] 设置单个文件的过期时间 默认10天 在程序启动30S 以后会检查过期文件
     *       过期时间依据 当前系统时间 - 文件最后修改时间计算
     *       默认 单个文件保存 10天
     */
    fun setMaxAliveTime(day: Int): Builder {
        when {
            day < 0 -> {
                this.maxAliveTime = 0
            }
            day > 10 -> {
                this.maxAliveTime = 10
            }
            else -> {
                this.maxAliveTime = day
            }
        }
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
                    + "level:" + logLevel.level + "\n"
                    + "model:" + model.model + "\n"
                    + "cachePath:" + cachePath + "\n"
                    + "logPath:" + logPath + "\n"
                    + "namePreFix:" + namePreFix + "\n"
                    + "cacheDays:" + cacheDays + "\n"
                    + "pubKey:" + pubKey + "\n"
                    + "consoleLogOpen:" + consoleLogOpen + "\n"
                    + "maxFileSize:" + maxFileSize + "\n"
        )

        android.util.Log.i(tag, "Xlog=========================================<")
        Xlog.setConsoleLogOpen(consoleLogOpen)
        //每天一个日志文件
        if (oneFileEveryday) {
            Xlog.setMaxFileSize(0)
        } else {
            Xlog.setMaxFileSize(maxFileSize)
        }

        Xlog.setMaxAliveTime((maxAliveTime * 24 * 60 * 60).toLong())

        Xlog.appenderOpen(
            logLevel.level,
            model.model,
            cachePath,
            logPath,
            namePreFix,
            cacheDays,
            pubKey
        )
        Log.setLogImp(Xlog())
    }


}