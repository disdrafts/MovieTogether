package com.example.movietogether.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movietogether.data.remote.repository.MovieRepository
import com.example.movietogether.databinding.FragmentMovieListBinding
import kotlinx.coroutines.launch

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

    private val repository = MovieRepository()
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

        adapter = MovieAdapter { movie ->
            Toast.makeText(requireContext(), movie.title, Toast.LENGTH_SHORT).show()
            // позже откроем детали
        }

        binding.rvMovies.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMovies.adapter = adapter

        loadMovies()
    }

    private fun loadMovies() {
        val type = arguments?.getString(ARG_TYPE) ?: TYPE_POPULAR

        binding.progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                val movies = when (type) {
                    TYPE_TOP250 -> repository.getTop250()
                    else -> repository.getPopularMovies()
                }
                adapter.submitList(movies)
                if (movies.isEmpty()) {
                    Toast.makeText(requireContext(), "The list is empty", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}