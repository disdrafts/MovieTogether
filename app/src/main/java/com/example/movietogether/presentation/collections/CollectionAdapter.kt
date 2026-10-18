package com.example.movietogether.presentation.collections

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movietogether.R
import com.example.movietogether.data.remote.dto.CollectionDto
import com.example.movietogether.databinding.ItemCollectionBinding

class CollectionAdapter(
    private val onClick: (CollectionDto) -> Unit
) : RecyclerView.Adapter<CollectionAdapter.CollectionViewHolder>() {

    private val items = mutableListOf<CollectionDto>()

    fun submitList(list: List<CollectionDto>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionViewHolder {
        val binding = ItemCollectionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CollectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CollectionViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class CollectionViewHolder(
        private val binding: ItemCollectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CollectionDto) {
            binding.tvName.text = item.name
            binding.tvCount.text = binding.root.context.getString(
                R.string.collections_movies_count,
                item.movies.size
            )

            val poster = item.movies.firstOrNull()?.posterUrl
            Glide.with(binding.root)
                .load(poster)
                .placeholder(R.color.poster_placeholder)
                .centerCrop()
                .into(binding.ivCover)

            binding.root.setOnClickListener { onClick(item) }
        }
    }
}