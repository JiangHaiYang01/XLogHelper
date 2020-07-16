package com.allens.xloghelper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.allens.xlog.XLogHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        add.setOnClickListener {

            for (i in 1..100000) {
                XLogHelper.i("======================> %s", i)
            }
        }

    }
}