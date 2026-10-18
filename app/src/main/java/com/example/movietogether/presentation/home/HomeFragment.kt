package com.example.movietogether.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.movietogether.R
import com.example.movietogether.data.remote.dto.KinopoiskMovieDto
import com.example.movietogether.databinding.FragmentHomeBinding
import com.example.movietogether.presentation.movie.MovieDetailsFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
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

        popularAdapter = MovieAdapter(R.layout.item_movie) { openDetails(it) }
        topAdapter = MovieAdapter(R.layout.item_movie) { openDetails(it) }

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

        binding.tvSeeAllPopular.setOnClickListener {
            openFullList(MovieListFragment.TYPE_POPULAR)
        }
        binding.tvSeeAllTop.setOnClickListener {
            openFullList(MovieListFragment.TYPE_TOP250)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        HomeUiState.Loading -> {
                            binding.shimmerContainer.visibility = View.VISIBLE
                            binding.shimmerContainer.startShimmer()
                            binding.contentContainer.visibility = View.GONE
                        }

                        is HomeUiState.Success -> {
                            binding.shimmerContainer.stopShimmer()
                            binding.shimmerContainer.visibility = View.GONE
                            binding.contentContainer.visibility = View.VISIBLE

                            popularAdapter.submitList(state.popular)
                            topAdapter.submitList(state.top250)

                            featuredMovie = state.popular.firstOrNull()
                            featuredMovie?.let { movie ->
                                binding.tvBannerTitle.text = movie.title
                                Glide.with(this@HomeFragment)
                                    .load(movie.posterUrl)
                                    .centerCrop()
                                    .into(binding.ivBanner)
                            }
                        }

                        is HomeUiState.Error -> {
                            binding.shimmerContainer.stopShimmer()
                            binding.shimmerContainer.visibility = View.GONE
                            binding.contentContainer.visibility = View.VISIBLE
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun openFullList(type: String) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, MovieListFragment.newInstance(type))
            .addToBackStack(null)
            .commit()
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