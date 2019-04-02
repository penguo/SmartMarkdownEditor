package com.penguodev.smartmd.ui

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.penguodev.smartmd.R
import com.penguodev.smartmd.ui.list.ListActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startActivity(Intent(this@MainActivity, ListActivity::class.java))
        finish()
    }
}
