package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)


        findViewById<Button>(R.id.buttonOpenMap).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


        findViewById<Button>(R.id.buttonHistory).setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }


        findViewById<Button>(R.id.buttonSettings).setOnClickListener {

            Toast.makeText(this, "Ustawienia w przygotowaniu!", Toast.LENGTH_SHORT).show()
        }
    }
}
