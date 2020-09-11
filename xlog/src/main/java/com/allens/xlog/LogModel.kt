package com.allens.xlog

enum class LogModel(var model: Int) {
    //异步写入 release 下建议 非实时写入
    Async(0),

    //同步写入 debug 下建议，实时写入 不会加密
    Sync(1)
}
