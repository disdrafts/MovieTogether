package com.example.movietogether.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.movietogether.R
import com.example.movietogether.data.remote.dto.KinopoiskMovieDto
import com.example.movietogether.data.remote.repository.MovieRepository
import com.example.movietogether.databinding.FragmentHomeBinding
import com.example.movietogether.presentation.movie.MovieDetailsFragment
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val repository = MovieRepository()
    private lateinit var popularAdapter: MovieAdapter
    private lateinit var topAdapter: MovieAdapter

    private var featuredMovie: KinopoiskMovieDto? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        popularAdapter = MovieAdapter { openDetails(it) }
        topAdapter = MovieAdapter { openDetails(it) }

        binding.rvPopular.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvTop250.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        binding.rvPopular.adapter = popularAdapter
        binding.rvTop250.adapter = topAdapter

        binding.btnDetails.setOnClickListener {
            featuredMovie?.let { openDetails(it) }
        }

        binding.btnWatch.setOnClickListener {
            featuredMovie?.let { openDetails(it) }
        }

        loadData()
    }

    private fun loadData() {
        binding.progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val popular = repository.getPopularMovies()
                val top = repository.getTop250()

                popularAdapter.submitList(popular)
                topAdapter.submitList(top)

                featuredMovie = popular.firstOrNull()
                featuredMovie?.let { movie ->
                    binding.tvBannerTitle.text = movie.title
                    Glide.with(this@HomeFragment)
                        .load(movie.posterUrl)
                        .centerCrop()
                        .into(binding.ivBanner)
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun openDetails(movie: KinopoiskMovieDto) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, MovieDetailsFragment.newInstance(movie))
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}