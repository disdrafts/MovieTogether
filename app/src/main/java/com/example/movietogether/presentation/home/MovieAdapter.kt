package com.example.movietogether.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movietogether.data.remote.dto.KinopoiskMovieDto
import com.example.movietogether.databinding.ItemMovieBinding

class MovieAdapter(
    private val onClick: (KinopoiskMovieDto) -> Unit
) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    private val items = mutableListOf<KinopoiskMovieDto>()

    fun submitList(list: List<KinopoiskMovieDto>) {
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

        fun bind(movie: KinopoiskMovieDto) {
            binding.tvTitle.text = movie.title
            binding.tvYear.text = movie.year?.toString() ?: "—"
            binding.tvRating.text = "★ ${movie.rating ?: "—"}"

            Glide.with(binding.root)
                .load(movie.posterUrl)
                .centerCrop()
                .into(binding.ivPoster)

            binding.root.setOnClickListener {
                onClick(movie)
            }
        }
    }
}