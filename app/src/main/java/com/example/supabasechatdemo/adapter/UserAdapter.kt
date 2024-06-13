package com.example.supabasechatdemo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.supabasechatdemo.R
import com.example.supabasechatdemo.data.model.UserModel
import java.util.Locale

class UserAdapter(private val items: ArrayList<UserModel>, private val onItemClick: OnItemClick) :
    RecyclerView.Adapter<UserAdapter.ItemViewHolder>() {

    // Inflate the item layout and create the ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return ItemViewHolder(view)
    }

    // Bind data to the views in each item
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val user = items[position]

        holder.tvEmail.text = user.user_email
        holder.tvTitle.text = user.user_email[0].toString().uppercase(Locale.ROOT)

        holder.llMain.setOnClickListener {
            onItemClick.onClick(user)
        }
    }

    // Interface to manage click of item in activity
    fun interface OnItemClick {
        fun onClick(user: UserModel)
    }

    // Return the total count of items
    override fun getItemCount() = items.size

    // Define a ViewHolder that holds the views for each item
    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEmail: TextView = itemView.findViewById(R.id.tv_user_email)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_icon)
        val llMain: LinearLayout = itemView.findViewById(R.id.ll_main)
    }
}