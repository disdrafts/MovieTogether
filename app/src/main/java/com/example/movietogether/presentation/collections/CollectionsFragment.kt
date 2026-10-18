package com.example.movietogether.presentation.collections

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movietogether.R
import com.example.movietogether.databinding.FragmentCollectionsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CollectionsFragment : Fragment() {

    private var _binding: FragmentCollectionsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CollectionsViewModel by viewModels()
    private lateinit var adapter: CollectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCollectionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CollectionAdapter { collection ->
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    CollectionDetailsFragment.newInstance(collection.id, collection.name)
                )
                .addToBackStack(null)
                .commit()
        }

        binding.rvCollections.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCollections.adapter = adapter

        binding.fabCreate.setOnClickListener { showCreateDialog() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        CollectionsUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is CollectionsUiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            adapter.submitList(state.collections)
                            binding.tvEmpty.visibility =
                                if (state.collections.isEmpty()) View.VISIBLE else View.GONE
                        }
                        is CollectionsUiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.load()
    }

    private fun showCreateDialog() {
        val input = EditText(requireContext()).apply {
            hint = getString(R.string.collections_name_hint)
            setPadding(48, 32, 48, 32)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.collections_create)
            .setView(input)
            .setPositiveButton(R.string.collections_create) { _, _ ->
                viewModel.createCollection(input.text.toString())
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}