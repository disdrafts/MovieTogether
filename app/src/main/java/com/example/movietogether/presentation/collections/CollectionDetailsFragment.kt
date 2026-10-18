package com.example.movietogether.presentation.collections

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.movietogether.R
import com.example.movietogether.databinding.FragmentCollectionDetailsBinding
import com.example.movietogether.presentation.movie.MovieDetailsFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CollectionDetailsFragment : Fragment() {

    companion object {
        private const val ARG_ID = "collection_id"
        private const val ARG_NAME = "collection_name"

        fun newInstance(id: Long, name: String): CollectionDetailsFragment {
            return CollectionDetailsFragment().apply {
                arguments = bundleOf(ARG_ID to id, ARG_NAME to name)
            }
        }
    }

    private var _binding: FragmentCollectionDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CollectionDetailsViewModel by viewModels()
    private lateinit var adapter: LocalMovieAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCollectionDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val collectionId = arguments?.getLong(ARG_ID) ?: 0L
        val name = arguments?.getString(ARG_NAME).orEmpty()

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        adapter = LocalMovieAdapter { movie ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MovieDetailsFragment.newInstance(movie))
                .addToBackStack(null)
                .commit()
        }

        binding.rvMovies.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvMovies.adapter = adapter

        viewModel.load(collectionId, name)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        CollectionDetailsUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is CollectionDetailsUiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            binding.tvTitle.text = state.name
                            adapter.submitList(state.movies)
                            binding.tvEmpty.visibility =
                                if (state.movies.isEmpty()) View.VISIBLE else View.GONE
                        }
                        is CollectionDetailsUiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}