package com.example.movietogether.presentation.watch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.movietogether.data.remote.dto.ChatMessageDto
import com.example.movietogether.databinding.ItemChatMessageBinding

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val items = mutableListOf<ChatMessageDto>()

    fun addMessage(message: ChatMessageDto) {
        items.add(message)
        notifyItemInserted(items.lastIndex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ChatViewHolder(
        private val binding: ItemChatMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatMessageDto) {
            binding.tvUser.text = item.username
            binding.tvText.text = item.message
        }
    }
}