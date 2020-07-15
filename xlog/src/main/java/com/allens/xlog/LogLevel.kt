package com.allens.xlog

enum class LogLevel(var level: Int) {
    LEVEL_ALL(0),
    LEVEL_VERBOSE(0),
    LEVEL_DEBUG(1),
    LEVEL_INFO(2),
    LEVEL_WARNING(3),
    LEVEL_ERROR(4),
    LEVEL_FATAL(5),
    LEVEL_NONE(6)
}