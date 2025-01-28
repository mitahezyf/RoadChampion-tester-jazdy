package com.example.myapplication

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RouteAdapter(private val onRouteClick: (Route) -> Unit) :
    RecyclerView.Adapter<RouteAdapter.RouteViewHolder>() {

    private val routes = mutableListOf<Route>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newRoutes: List<Route>) {
        routes.clear()
        routes.addAll(newRoutes)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return RouteViewHolder(view, onRouteClick)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        val route = routes[position]
        holder.bind(route)
    }

    override fun getItemCount(): Int = routes.size

    class RouteViewHolder(view: View, private val onRouteClick: (Route) -> Unit) :
        RecyclerView.ViewHolder(view) {

        private val title = view.findViewById<TextView>(android.R.id.text1)
        private val subtitle = view.findViewById<TextView>(android.R.id.text2)

        @SuppressLint("SetTextI18n")
        fun bind(route: Route) {
            title.text = "Trasa ID: ${route.id}"
            subtitle.text = "Średnia prędkość: ${route.averageSpeed} km/h"
            itemView.setOnClickListener { onRouteClick(route) }
        }
    }
}
