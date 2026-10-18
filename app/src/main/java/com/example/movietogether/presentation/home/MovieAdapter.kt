package com.example.movietogether.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movietogether.R
import com.example.movietogether.data.remote.dto.KinopoiskMovieDto
import com.example.movietogether.databinding.ItemMovieBinding
import com.example.movietogether.databinding.ItemMovieGridBinding

class MovieAdapter(
    @LayoutRes private val layoutRes: Int = R.layout.item_movie,
    private val onClick: (KinopoiskMovieDto) -> Unit
) : ListAdapter<KinopoiskMovieDto, RecyclerView.ViewHolder>(Diff) {

    object Diff : DiffUtil.ItemCallback<KinopoiskMovieDto>() {
        override fun areItemsTheSame(a: KinopoiskMovieDto, b: KinopoiskMovieDto) = a.id == b.id
        override fun areContentsTheSame(a: KinopoiskMovieDto, b: KinopoiskMovieDto) = a == b
    }

    override fun getItemViewType(position: Int): Int = layoutRes

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == R.layout.item_movie_grid) {
            GridVH(ItemMovieGridBinding.inflate(inflater, parent, false))
        } else {
            DefaultVH(ItemMovieBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val movie = getItem(position)
        when (holder) {
            is GridVH -> holder.bind(movie, onClick)
            is DefaultVH -> holder.bind(movie, onClick)
        }
    }

    class DefaultVH(private val binding: ItemMovieBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: KinopoiskMovieDto, onClick: (KinopoiskMovieDto) -> Unit) {
            binding.tvTitle.text = movie.title
            binding.tvRating.text = movie.rating?.let { "★ $it" } ?: ""
            Glide.with(binding.root.context)
                .load(movie.posterUrl)
                .centerCrop()
                .into(binding.ivPoster)
            binding.root.setOnClickListener { onClick(movie) }
        }
    }

    class GridVH(private val binding: ItemMovieGridBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: KinopoiskMovieDto, onClick: (KinopoiskMovieDto) -> Unit) {
            binding.tvTitle.text = movie.title
            binding.tvRating.text = movie.rating?.let { "★ $it" } ?: ""
            Glide.with(binding.root.context)
                .load(movie.posterUrl)
                .centerCrop()
                .into(binding.ivPoster)
            binding.root.setOnClickListener { onClick(movie) }
        }
    }
}