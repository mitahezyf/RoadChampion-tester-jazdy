package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RouteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)


        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RouteAdapter { route -> onRouteClicked(route) }
        recyclerView.adapter = adapter

        val backButton = findViewById<CardView>(R.id.cardBack)
        backButton.setOnClickListener {
            VibrationHelper.vibrate(this)
            finish()
        }


        loadRoutes()
    }

    private fun loadRoutes() {
        val database = AppDatabase.getInstance(this)
        CoroutineScope(Dispatchers.IO).launch {
            val routes = database.routeDao().getAllRoutes()
            withContext(Dispatchers.Main) {
                adapter.submitList(routes)
            }
        }
    }

    private fun onRouteClicked(route: Route) {
        Log.d("HistoryActivity", "Kliknięto trasę o ID: ${route.id}")
        val intent = Intent(this, RouteDetailsActivity::class.java)
        intent.putExtra("routeId", route.id)
        Log.d("HistoryActivity", "Przekazywane ID trasy: ${route.id}")
        VibrationHelper.vibrate(this)
        startActivity(intent)
    }


}
