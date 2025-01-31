package com.example.myapplication

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
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
            .inflate(R.layout.item_route, parent, false)
        return RouteViewHolder(view, onRouteClick)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        val route = routes[position]
        holder.bind(route)
    }

    override fun getItemCount(): Int = routes.size

    class RouteViewHolder(view: View, private val onRouteClick: (Route) -> Unit) :
        RecyclerView.ViewHolder(view) {

        private val cardView: CardView = view.findViewById(R.id.cardRoute)
        private val title: TextView = view.findViewById(R.id.textRouteTitle)
        private val subtitle: TextView = view.findViewById(R.id.textRouteDetails)

        @SuppressLint("SetTextI18n", "DefaultLocale")
        fun bind(route: Route) {
            val formattedDistance = String.format("%.2f", route.distance)
            val durationInMinutes = route.duration / 60000
            val formattedSpeed = String.format("%.2f", route.averageSpeed)

            title.text = "Trasa #${route.id}"
            subtitle.text = "Dystans: $formattedDistance km | Czas: $durationInMinutes min | Śr. prędkość: $formattedSpeed km/h"

            cardView.setOnClickListener { onRouteClick(route) }
        }
    }
}
