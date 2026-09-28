package com.example.movietogether.presentation.collections

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movietogether.data.remote.dto.MovieDto
import com.example.movietogether.databinding.ItemMovieBinding

class LocalMovieAdapter(
    private val onClick: (MovieDto) -> Unit
) : RecyclerView.Adapter<LocalMovieAdapter.MovieViewHolder>() {

    private val items = mutableListOf<MovieDto>()

    fun submitList(list: List<MovieDto>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class MovieViewHolder(
        private val binding: ItemMovieBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: MovieDto) {
            binding.tvTitle.text = movie.title
            binding.tvYear.text = movie.year?.toString() ?: "—"
            binding.tvRating.text = "★ ${movie.rating ?: "—"}"

            Glide.with(binding.root)
                .load(movie.posterUrl)
                .centerCrop()
                .into(binding.ivPoster)

            binding.root.setOnClickListener { onClick(movie) }
        }
    }
}