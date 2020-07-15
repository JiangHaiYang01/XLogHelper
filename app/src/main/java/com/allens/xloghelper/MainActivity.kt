package com.allens.xloghelper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.allens.xlog.XLogHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        close.setOnClickListener {
            XLogHelper.close()
        }

        add.setOnClickListener {
            XLogHelper.i("tag", "add log %s  你还啊  %s", "12q12", 123)

            for (i in 1..10000) {
                XLogHelper.i("我他妈心态崩了啊 %s", i)
            }
        }

    }
}