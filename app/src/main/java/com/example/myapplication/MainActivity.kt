package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import org.osmdroid.config.Configuration
import android.content.Context


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Configuration.getInstance().load(applicationContext, getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = packageName

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, MapFragment())
            .commit()
    }
}
