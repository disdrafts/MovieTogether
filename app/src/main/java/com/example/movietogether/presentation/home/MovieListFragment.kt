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
import androidx.recyclerview.widget.GridLayoutManager
import com.example.movietogether.R
import com.example.movietogether.data.remote.dto.KinopoiskMovieDto
import com.example.movietogether.databinding.FragmentMovieListBinding
import com.example.movietogether.presentation.movie.MovieDetailsFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MovieListFragment : Fragment() {

    companion object {
        private const val ARG_TYPE = "type"
        const val TYPE_POPULAR = "popular"
        const val TYPE_TOP250 = "top250"

        fun newInstance(type: String): MovieListFragment {
            return MovieListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TYPE, type)
                }
            }
        }
    }

    private var _binding: FragmentMovieListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MovieListViewModel by viewModels()
    private lateinit var adapter: MovieAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val type = arguments?.getString(ARG_TYPE) ?: TYPE_POPULAR

        adapter = MovieAdapter(R.layout.item_movie_grid) { openDetails(it) }
        binding.rvMovies.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvMovies.adapter = adapter

        viewModel.load(type)

        binding.tvTitle.text = when (type) {
            TYPE_TOP250 -> getString(R.string.home_top_250)
            else -> getString(R.string.home_popular)
        }
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        MovieListUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is MovieListUiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            adapter.submitList(state.movies)
                            if (state.movies.isEmpty()) {
                                Toast.makeText(
                                    requireContext(),
                                    R.string.search_empty,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        is MovieListUiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.error_loading, state.message),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
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