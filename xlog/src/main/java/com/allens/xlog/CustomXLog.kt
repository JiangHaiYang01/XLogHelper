package com.allens.xlog

import android.util.Log
import com.tencent.mars.xlog.Xlog

class CustomXLog : Xlog() {

    override fun logI(
        tag: String?,
        filename: String?,
        funcname: String?,
        line: Int,
        pid: Int,
        tid: Long,
        maintid: Long,
        log: String?
    ) {
        super.logI(tag, filename, funcname, line, pid, tid, maintid, log)
        Log.i(
            "allens",
            "tag:$tag \n" +
                    "filename:$filename \n" +
                    "funcname:$funcname \n" +
                    "line:$line \n" +
                    "pid:$pid \n" +
                    "tid:$tid \n" +
                    "maintid:$maintid \n" +
                    "log:$log \n" +
                    ""
        )
    }

}