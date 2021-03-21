package com.example.peopleprotector

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CustomAdapter(private val userSet: MutableList<String>) :
        RecyclerView.Adapter<CustomAdapter.ViewHolder>() {


    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var userView: TextView

        init {
            // Define click listener for the ViewHolder's View.
            userView = view.findViewById(R.id.username)
            view.setOnClickListener {

                val pos: Int = adapterPosition
                Log.i("TAG", pos.toString())
            }
        }

    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.recyclerview_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.userView.text = userSet[position]
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = userSet.size


    fun addItem(username: String) {
        userSet.add(0,username)
        notifyDataSetChanged()
    }
    fun removeItem(username: String) {
        userSet.remove(username)
        notifyDataSetChanged()
    }

}



