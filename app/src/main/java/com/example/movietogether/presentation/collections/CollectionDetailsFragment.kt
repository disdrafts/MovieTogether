package com.example.movietogether.presentation.collections

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.movietogether.R
import com.example.movietogether.data.remote.repository.CollectionRepository
import com.example.movietogether.databinding.FragmentCollectionDetailsBinding
import kotlinx.coroutines.launch

class CollectionDetailsFragment : Fragment() {

    companion object {
        private const val ARG_ID = "collection_id"
        private const val ARG_NAME = "collection_name"

        fun newInstance(id: Long, name: String): CollectionDetailsFragment {
            return CollectionDetailsFragment().apply {
                arguments = bundleOf(
                    ARG_ID to id,
                    ARG_NAME to name
                )
            }
        }
    }

    private var _binding: FragmentCollectionDetailsBinding? = null
    private val binding get() = _binding!!

    private val repository = CollectionRepository()
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

        binding.tvTitle.text = name
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        adapter = LocalMovieAdapter { movie ->
            Toast.makeText(requireContext(), movie.title, Toast.LENGTH_SHORT).show()
        }

        binding.rvMovies.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvMovies.adapter = adapter

        loadCollection(collectionId)
    }

    private fun loadCollection(id: Long) {
        binding.progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val collection = repository.getCollection(id)
                adapter.submitList(collection.movies)
                binding.tvEmpty.visibility =
                    if (collection.movies.isEmpty()) View.VISIBLE else View.GONE
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}