package com.example.supabasechatdemo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.supabasechatdemo.utils.Utils
import com.example.supabasechatdemo.R
import com.example.supabasechatdemo.data.model.ChatModel

class ChatAdapter(private val messagesList: ArrayList<ChatModel>, private val userId: String) :
    RecyclerView.Adapter<ChatAdapter.ItemViewHolder>() {

    // Inflate the item layout and create the ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ItemViewHolder(view)
    }

    // Bind data to the views in each item
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = messagesList[position]
        if (item.date.isNotEmpty()) {
            holder.llHeader.visibility = View.VISIBLE
            holder.llSend.visibility = View.GONE
            holder.llReceive.visibility = View.GONE
            holder.tvHeader.text = Utils.checkDate(item.date)
        } else {
            holder.llHeader.visibility = View.GONE
            if (item.sender_id == userId) {
                holder.llSend.visibility = View.VISIBLE
                holder.llReceive.visibility = View.GONE

                holder.tvSend.text = item.message
                holder.tvSendTime.text = Utils.formatChange(item.created_at)
            } else {
                holder.llReceive.visibility = View.VISIBLE
                holder.llSend.visibility = View.GONE

                holder.tvReceive.text = item.message
                holder.tvReceiveTime.text = Utils.formatChange(item.created_at)
            }
        }
    }

    // Return the total count of items
    override fun getItemCount() = messagesList.size

    // Add messages
    fun addData(list: ChatModel) {
        messagesList.add(list)
        notifyItemInserted(messagesList.size)
    }

    // Define a ViewHolder that holds the views for each item
    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val llSend: LinearLayout = itemView.findViewById(R.id.ll_send)
        val llReceive: LinearLayout = itemView.findViewById(R.id.ll_receive)
        val tvSend: TextView = itemView.findViewById(R.id.tv_send)
        val tvReceive: TextView = itemView.findViewById(R.id.tv_receive)
        val tvSendTime: TextView = itemView.findViewById(R.id.tv_send_message_time)
        val tvReceiveTime: TextView = itemView.findViewById(R.id.tv_receive_message_time)
        val tvHeader: TextView = itemView.findViewById(R.id.tv_header)
        val llHeader: LinearLayout = itemView.findViewById(R.id.ll_header)
    }
}