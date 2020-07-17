package com.allens.xloghelper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.allens.xlog.XLogHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //添加日志
        add.setOnClickListener {

            for (i in 1..10) {
                XLogHelper.i("======================>i %s", i)
            }
            for (i in 1..10) {
                XLogHelper.e("======================>e %s", i)
            }
            for (i in 1..10) {
                XLogHelper.v("======================>v %s", i)
            }
        }


        //close 之后不能写入
        close.setOnClickListener {
            XLogHelper.close()
        }


        //flush 之后 还能够再次写入
        flush.setOnClickListener {
            XLogHelper.flush(true)
        }

    }
}