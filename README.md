
基于微信XLog的日志框架&&对于XLog的分析


# 前言

之前写过一个 日志框架[LogHelper](https://github.com/JiangHaiYang01/LogHelper) ,是基于 Logger 开源库封装的，当时的因为项目本身的日志不是很多，完全可以使用，最近和其他公司合作，在一个新的项目上反馈，说在 大量log 的情况下会影响到手机主体功能的使用。从而让我对之前的日志行为做了一个深刻的反省


随后在开发群中咨询了其他开发的小伙伴，如果追求性能，可以研究一下 微信的  xlog   ,也是本篇博客的重点


# xlog 是什么

xlog 是什么 这个问题 我这也是在[【腾讯Bugly干货分享】微信mars 的高性能日志模块 xlog](https://zhuanlan.zhihu.com/p/23879436)得到了答案

简单来说 ，就是腾讯团队分享的基于 c/c++ 高可靠性高性能的运行期日志组件



# 官网的 sample

知道了他是什么，就要只要他是怎么用的，打开github 找到官网[Tencent/mars](https://github.com/Tencent/mars)

使用非常简单

## 下载库

```
dependencies {
    compile 'com.tencent.mars:mars-xlog:1.2.3'
}
```

## 使用

```
System.loadLibrary("c++_shared");
System.loadLibrary("marsxlog");

final String SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();
final String logPath = SDCARD + "/marssample/log";

// this is necessary, or may crash for SIGBUS
final String cachePath = this.getFilesDir() + "/xlog"

//init xlog
if (BuildConfig.DEBUG) {
    Xlog.appenderOpen(Xlog.LEVEL_DEBUG, Xlog.AppenderModeAsync, cachePath, logPath, "MarsSample", 0, "");
    Xlog.setConsoleLogOpen(true);

} else {
    Xlog.appenderOpen(Xlog.LEVEL_INFO, Xlog.AppenderModeAsync, cachePath, logPath, "MarsSample", 0, "");
    Xlog.setConsoleLogOpen(false);
}

Log.setLogImp(new Xlog());
```

OK 实现了他的功能

> 不要高兴的太早，后续的问题都头大


# 分析各个方法的作用


知道了最简单的用法，就想看看他支持哪些功能

按照官网的demo 首先分析一下``appenderOpen``

## appenderOpen(int level, int mode, String cacheDir, String logDir, String nameprefix, int cacheDays, String pubkey)

### level

日志级别 没啥好说的 XLog 中已经写得很清楚了


```
public static final int LEVEL_ALL = 0;
public static final int LEVEL_VERBOSE = 0;
public static final int LEVEL_DEBUG = 1;
public static final int LEVEL_INFO = 2;
public static final int LEVEL_WARNING = 3;
public static final int LEVEL_ERROR = 4;
public static final int LEVEL_FATAL = 5;
public static final int LEVEL_NONE = 6;
```


>  值得注意的地方   debug 版本下建议把控制台日志打开，日志级别设为 Verbose 或者 Debug, release 版本建议把控制台日志关闭，日志级别使用 Info.

这个在官网的 [接入指南](https://github.com/Tencent/mars/wiki/Mars-Android-%E6%8E%A5%E5%85%A5%E6%8C%87%E5%8D%97)


这里也可以使用

```java
public static native void setLogLevel(int logLevel);
```
方法设置

### mode

 写入的模式

- public static final int AppednerModeAsync = 0;

异步写入

- public static final int AppednerModeSync = 1;

同步写入


同步写入，可以理解为实时的日志，异步则不是

> Release版本一定要用 AppednerModeAsync， Debug 版本两个都可以，但是使用 AppednerModeSync 可能会有卡顿

这里也可以使用

```java
public static native void setAppenderMode(int mode);
```
方法设置

### cacheDir 设置缓存目录



缓存目录，当 logDir 不可写时候会写进这个目录，可选项，不选用请给 ""， 如若要给，建议给应用的 /data/data/packname/files/log 目录。

会在目录下生成后缀为 .mmap3 的缓存文件，


### logDir 设置写入的文件目录

真正的日志，后缀为 .xlog


> 日志写入目录，请给单独的目录，除了日志文件不要把其他文件放入该目录，不然可能会被日志的自动清理功能清理掉。

### nameprefix 设置日志文件名的前缀

日志文件名的前缀，例如该值为TEST，生成的文件名为：TEST_20170102.xlog。

### cacheDays

 一般情况下填0即可。非0表示会在 _cachedir 目录下存放几天的日志。

 这里的描述比较晦涩难懂，当我设置这个参数非0 的时候 会发现 原本设置在   logDir 目录下的文件 出现在了 cacheDir

 例如 正常应该是


文件结构

 ```java
- cacheDir
    - log.mmap3
- logDir
    - log_20200710.xlog
    - log_20200711.xlog
 ```

 变成这样

 ```java
- cacheDir
    - log.mmap3
    - log_20200710.xlog
    - log_20200711.xlog
- logDir

 ```

 全部到了 cacheDir 下面

 > cacheDays 的意思是 在多少天以后  从缓存目录移到日志目录


### pubkey 设置加密的 pubkey

这里涉及到了日志的加密与解密，下面会专门介绍


## setMaxFileSize 设置文件大小

在 Xlog 下有一个 native 方法

```
	public static native void setMaxFileSize(long size);
```

他表示 最大文件大小，这里需要说一下，原本的默认设置 是一天一个日志文件在 [appender.h](https://github.com/Tencent/mars/blob/master/mars/log/appender.h) 描述的很清楚

```java
/*
 * By default, all logs will write to one file everyday. You can split logs to multi-file by changing max_file_size.
 *
 * @param _max_byte_size    Max byte size of single log file, default is 0, meaning do not split.
 */
void appender_set_max_file_size(uint64_t _max_byte_size);
```

默认情况下，所有日志每天都写入一个文件。可以通过更改max_file_size将日志分割为多个文件。单个日志文件的最大字节大小，默认为0，表示不分割



当超过设置的文件大小以后。文件会变成如下目录结构


 ```java
- cacheDir
    - log.mmap3
- logDir
    - log_20200710.xlog
    - log_20200710_1.xlog
    - log_20200710_2.xlog
 ```


在 [appender.cc](https://github.com/Tencent/mars/blob/master/mars/log/src/appender.cc) 对应的有如下逻辑，

```
static long __get_next_fileindex(const std::string& _fileprefix, const std::string& _fileext) {
    ...
    return (filesize > sg_max_file_size) ? index + 1 : index;

```


## setConsoleLogOpen  设置是否在控制台答应日志

···java
public static native void setConsoleLogOpen(boolean isOpen);
···

设置是否在控制台答应日志

## setErrLogOpen

这个方法是没用的，一开始以为哪里继承的有问题，在查看源码的时候发现 他是一个空方法，没有应用

![](http://allens-blog.oss-cn-beijing.aliyuncs.com/allens-blog/y9lop.png)

使用的话会导致程序异常,在自己编译的so 中我就把它给去掉了

## setMaxAliveTime 设置单个文件最大保留时间

```java
public static native void setMaxAliveTime(long duration);
```


置单个文件最大保留时间  单位是秒，这个方法有3个需要注意的地方，

- 必须在 appenderOpen 方法之前才有效
- 最小的时间是 一天
- 默认的时间是10天

在 [appender.cc](https://github.com/Tencent/mars/blob/master/mars/log/src/appender.cc) 中可以看到

```
static const long kMaxLogAliveTime = 10 * 24 * 60 * 60;    // 10 days in second
static const long kMinLogAliveTime = 24 * 60 * 60;    // 1 days in second
static long sg_max_alive_time = kMaxLogAliveTime;

....


void appender_set_max_alive_duration(long _max_time) {
	if (_max_time >= kMinLogAliveTime) {
		sg_max_alive_time = _max_time;
	}
}
```

默认的时间是10天

## appenderClose

在 [文档](https://github.com/Tencent/mars#mars_cn)中介绍说是在 程序退出时关闭日志 调用appenderClose的方法

然而在实际情况中 Application 类的 onTerminate() 只有在模拟器中才会生效，在真机中无效的，

如果在程序退出的时候没有触发 appenderClose 那么在下一次启动的时候，xlog 也会把日志写入到文件中


所以如何触发呢？

建议尽可能的去触发他 例如用户双击back 退出的情况下 你肯定是知道的
如果放在后台被杀死了，这个时候也真的没办法刷新，也没关系，上面也说了，再次启动的时候会刷新到日志中，

## appenderFlush


当日志写入模式为异步时，调用该接口会把内存中的日志写入到文件。

isSync : true  为同步 flush，flush 结束后才会返回。
isSync : false 为异步 flush，不等待 flush 结束就返回。


# 日志文件的加密

这一块单独拿出来说明，是因为之前使用上遇到了坑

首先是这个 入参 PUB_KEY,一脸懵，是个啥，

在 [mars/blob/master/mars/log/crypt/gen_key.py](https://github.com/Tencent/mars/blob/master/mars/log/crypt/gen_key.py) 这个就是能够获取到 PUB_KEY 的方法

运行如下

```python
$ python gen_key.py
WARNING: Executing a script that is loading libcrypto in an unsafe way. This will fail in a future version of macOS. Set the LIBRESSL_REDIRECT_STUB_ABORT=1 in the environment to force this into an error.
save private key
471e607b1bb3760205f74a5e53d2764f795601e241ebc780c849e7fde1b4ce40

appender_open's parameter:
300330b09d9e771d6163bc53a4e23b188ac9b2f5c7150366835bce3a12b0c8d9c5ecb0b15274f12b2dffae7f4b11c3b3d340e0521e8690578f51813c93190e1e
```


上面的 private key 自己保存好

appender_open's parameter: 就是需要的 PUB_KEY


# 日志文件的解密


上面已经知道如何加密了，现在了解一下如何解密

## 下载pyelliptic1

在[Xlog 加密使用指引](https://github.com/Tencent/mars/wiki/Xlog-%E5%8A%A0%E5%AF%86%E4%BD%BF%E7%94%A8%E6%8C%87%E5%BC%95)中能够看到

需要下载 [pyelliptic1.5.7](https://github.com/yann2192/pyelliptic/releases/tag/1.5.7)
然后编译 否则下面的命令会失败



## 直接解密脚本



xlog 很贴心的给我们提供了两个脚本

使用 [decode_mars_nocrypt_log_file.py](https://github.com/Tencent/mars/blob/master/mars/log/crypt/decode_nomars_crypt_log_file.py) 解压没有加密的

```python
python decode_mars_nocrypt_log_file [path]
```

使用 [decode_mars_crypt_log_file.py](https://github.com/Tencent/mars/blob/master/mars/log/crypt/decode_mars_crypt_log_file.py) 加密的文件

在使用之前需要将 脚本中的

```
PRIV_KEY = "145aa7717bf9745b91e9569b80bbf1eedaa6cc6cd0e26317d810e35710f44cf8"
PUB_KEY = "572d1e2710ae5fbca54c76a382fdd44050b3a675cb2bf39feebe85ef63d947aff0fa4943f1112e8b6af34bebebbaefa1a0aae055d9259b89a1858f7cc9af9df1"
```

改成上面自己获取到的 key 否则是解压不出来的

```python
python decode_mars_crypt_log_file.py ~/Desktop/log/log_20200710.xlog
```

直接生成一个

```java
- cacheDir
    - log.mmap3
- logDir
    - log_20200710.xlog
    - log_20200710.xlog.log
```

也可以自定义名字

```python
python decode_mars_crypt_log_file.py ~/Desktop/log/log_20200710.xlog ~/Desktop/log/1.log
```

```java
- cacheDir
    - log.mmap3
- logDir
    - log_20200710.xlog
    - 1.log
```

# 修改日志的格式

打开我们解压好的日志查看

```
^^^^^^^^^^Oct 14 2019^^^20:27:59^^^^^^^^^^[17223,17223][2020-07-24 +0800 09:49:19]
get mmap time: 3
MARS_URL:
MARS_PATH: master
MARS_REVISION: 85b19f92
MARS_BUILD_TIME: 2019-10-14 20:27:57
MARS_BUILD_JOB:
log appender mode:0, use mmap:1
cache dir space info, capacity:57926635520 free:52452691968 available:52452691968
log dir space info, capacity:57926635520 free:52452691968 available:52452691968
[I][2020-07-24 +8.0 09:49:21.179][17223, 17223][TAG][, , 0][======================> 1
[I][2020-07-24 +8.0 09:49:21.180][17223, 17223][TAG][, , 0][======================> 2
[I][2020-07-24 +8.0 09:49:21.180][17223, 17223][TAG][, , 0][======================> 3
[I][2020-07-24 +8.0 09:49:21.180][17223, 17223][TAG][, , 0][======================> 4
[I][2020-07-24 +8.0 09:49:21.181][17223, 17223][TAG][, , 0][======================> 5
[I][2020-07-24 +8.0 09:49:21.181][17223, 17223][TAG][, , 0][======================> 6
[I][2020-07-24 +8.0 09:49:21.182][17223, 17223][TAG][, , 0][======================> 7
[I][2020-07-24 +8.0 09:49:21.182][17223, 17223][TAG][, , 0][======================> 8
[I][2020-07-24 +8.0 09:49:21.182][17223, 17223][TAG][, , 0][======================> 9
[I][2020-07-24 +8.0 09:49:21.183][17223, 17223][TAG][, , 0][======================> 10
[I][2020-07-24 +8.0 09:49:21.183][17223, 17223][TAG][, , 0][======================> 11
[I][2020-07-24 +8.0 09:49:21.183][17223, 17223][TAG][, , 0][======================> 12
[I][2020-07-24 +8.0 09:49:21.184][17223, 17223][TAG][, , 0][======================> 13
[I][2020-07-24 +8.0 09:49:21.184][17223, 17223][TAG][, , 0][======================> 14
[I][2020-07-24 +8.0 09:49:21.185][17223, 17223][TAG][, , 0][======================> 15
[I][2020-07-24 +8.0 09:49:21.185][17223, 17223][TAG][, , 0][======================> 16
[I][2020-07-24 +8.0 09:49:21.185][17223, 17223][TAG][, , 0][======================> 17
```

我擦泪 除了我们需要的信息以外，还有这么多杂七杂八的信息，如何去掉，并且自己定义一下格式


这里就需要自己去编译 so 了，好在 xlog 已经给我们提供了很好的编译代码

对应的文档 [本地编译](https://github.com/Tencent/mars/wiki/Mars-Android-%E6%8E%A5%E5%85%A5%E6%8C%87%E5%8D%97#local_compile)


> 对于编译这块按照文档来就好了 需要注意的是

- 一定要用  ndk-r20 不要用最新版本的 21
- 一定用 Python2.7 mac 自带  不用要 Python3


## 去掉头文件

首先我们去到这个头文件，对于一个日志框架来着，这个没啥用

```
^^^^^^^^^^Oct 14 2019^^^20:27:59^^^^^^^^^^[17223,17223][2020-07-24 +0800 09:49:19]
get mmap time: 3
MARS_URL:
MARS_PATH: master
MARS_REVISION: 85b19f92
MARS_BUILD_TIME: 2019-10-14 20:27:57
MARS_BUILD_JOB:
log appender mode:0, use mmap:1
cache dir space info, capacity:57926635520 free:52452691968 available:52452691968
log dir space info, capacity:57926635520 free:52452691968 available:52452691968
```

在本机下载好的 mars 下，找到 [appender.cc](https://github.com/Tencent/mars/blob/master/mars/log/src/appender.cc) 将头文件去掉

![](http://allens-blog.oss-cn-beijing.aliyuncs.com/allens-blog/x381l.png)


## 修改日志格式

默认的格式很长

```
[I][2020-07-24 +8.0 09:49:21.179][17223, 17223][TAG][, , 0][======================> 1
```
[日志级别][时间][pid,tid][tag][filename,strFuncName,line][日志内容

是一个这样结构

比较乱，我们想要的日志 就时间，级别，日志内容 就行了

找到 [formater.cc](https://github.com/Tencent/mars/blob/master/mars/log/src/formater.cc)

将原本的

```java
 int ret = snprintf((char*)_log.PosPtr(), 1024, "[%s][%s][%" PRIdMAX ", %" PRIdMAX "%s][%s][%s, %s, %d][",  // **CPPLINT SKIP**
                           _logbody ? levelStrings[_info->level] : levelStrings[kLevelFatal], temp_time,
                           _info->pid, _info->tid, _info->tid == _info->maintid ? "*" : "", _info->tag ? _info->tag : "",
                           filename, strFuncName, _info->line);
```

改成

```java
int ret = snprintf((char*)_log.PosPtr(), 1024,     "[%s][%s]",  // **CPPLINT SKIP**
                        temp_time,   _logbody ? levelStrings[_info->level] : levelStrings[kLevelFatal] );
```

就行了

然后从新编译，将so 翻入项目 在看一下现在的效果

```java
[2020-07-24 +8.0 11:47:42.597][I]======================>9
```

ok 打完收工


# 简单的封装一下

基本上分析和实现了我们需要的功能，那么把这部分简单的封装一下

放上核心的 Builder 源码可在下面自行查看

```java
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
```

# 下载
Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:
```
allprojects {
		repositories {
			...
			maven { url 'https://www.jitpack.io' }
		}
	}

```
Step 2. Add the dependency
```
	dependencies {
	        implementation 'com.github.JiangHaiYang01:XLogHelper:Tag'
	}
```

添加 abiFilter

```
android {
    compileSdkVersion 30
    buildToolsVersion "30.0.1"

    defaultConfig {
        ...
        ndk {
            abiFilter "armeabi-v7a"
        }
    }

    ...
}
```



> 当前最新版本

[![](https://www.jitpack.io/v/JiangHaiYang01/XLogHelper.svg)](https://www.jitpack.io/#JiangHaiYang01/XLogHelper)


# 使用

## 初始化，建议放在 Application 中

```
        XLogHelper.create(this)
            .setModel(LogModel.Async)
            .setTag("TAG")
            .setConsoleLogOpen(true)
            .setLogLevel(LogLevel.LEVEL_INFO)
            .setNamePreFix("log")
            .setPubKey("572d1e2710ae5fbca54c76a382fdd44050b3a675cb2bf39feebe85ef63d947aff0fa4943f1112e8b6af34bebebbaefa1a0aae055d9259b89a1858f7cc9af9df1")
            .setMaxFileSize(1f)
            .setOneFileEveryday(true)
            .setCacheDays(0)
            .setMaxAliveTime(2)
            .init()
```

使用
```
XLogHelper.i("======================> %s", i)
XLogHelper.e("======================> %s", i)
```

# 源码

[GitHub](https://github.com/JiangHaiYang01/XLogHelper)
[博客](https://allens.icu/posts/60625924/#more)
[掘金](https://juejin.im/post/5f110c786fb9a07e802052cd)




#  参考


[Tencent/mars](https://github.com/Tencent/mars)
[Mars Android 接口详细说明](https://github.com/Tencent/mars/wiki/Mars-Android-%E6%8E%A5%E5%8F%A3%E8%AF%A6%E7%BB%86%E8%AF%B4%E6%98%8E)
[【腾讯Bugly干货分享】微信mars 的高性能日志模块 xlog](https://zhuanlan.zhihu.com/p/23879436)
[本地编译](https://github.com/Tencent/mars/wiki/Mars-Android-%E6%8E%A5%E5%85%A5%E6%8C%87%E5%8D%97#local_compile)