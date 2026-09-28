package com.example.movietogether.presentation.collections

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movietogether.R
import com.example.movietogether.data.Session
import com.example.movietogether.data.remote.repository.CollectionRepository
import com.example.movietogether.databinding.FragmentCollectionsBinding
import kotlinx.coroutines.launch

class CollectionsFragment : Fragment() {

    private var _binding: FragmentCollectionsBinding? = null
    private val binding get() = _binding!!

    private val repository = CollectionRepository()
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

        binding.fabCreate.setOnClickListener {
            showCreateDialog()
        }

        loadCollections()
    }
    private fun loadCollections() {
        binding.progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val list = repository.getUserCollections(Session.userId)
                adapter.submitList(list)
                binding.tvEmpty.visibility =
                    if (list.isEmpty()) View.VISIBLE else View.GONE
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
    private fun showCreateDialog() {
        val input = EditText(requireContext()).apply {
            hint = getString(R.string.collections_name_hint)
            setTextColor(resources.getColor(R.color.text_primary, null))
            setHintTextColor(resources.getColor(R.color.text_secondary, null))
            setPadding(48, 32, 48, 32)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.collections_create)
            .setView(input)
            .setPositiveButton(R.string.collections_create) { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    createCollection(name)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun createCollection(name: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                repository.createCollection(name, Session.userId)
                loadCollections()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_loading, e.message ?: ""),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}