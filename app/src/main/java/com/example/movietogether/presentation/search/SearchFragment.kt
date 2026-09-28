package com.example.movietogether.presentation.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.movietogether.R
import com.example.movietogether.data.remote.dto.KinopoiskMovieDto
import com.example.movietogether.data.remote.repository.MovieRepository
import com.example.movietogether.databinding.FragmentSearchBinding
import com.example.movietogether.presentation.home.MovieAdapter
import com.example.movietogether.presentation.movie.MovieDetailsFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val repository = MovieRepository()
    private lateinit var adapter: MovieAdapter
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = bars.top)
            insets
        }

        adapter = MovieAdapter { movie -> openDetails(movie) }
        binding.rvResults.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvResults.adapter = adapter

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                search(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchJob?.cancel()
                searchJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(400)
                    search(newText.orEmpty())
                }
                return true
            }
        })
    }

    private fun search(query: String) {
        val q = query.trim()
        if (q.length < 2) {
            adapter.submitList(emptyList())
            binding.tvEmpty.visibility = View.GONE
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = repository.searchMovies(q)
                adapter.submitList(result)
                binding.tvEmpty.visibility =
                    if (result.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_loading, e.message ?: ""),
                    Toast.LENGTH_LONG
                ).show()
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
        searchJob?.cancel()
        _binding = null
        super.onDestroyView()
    }
}