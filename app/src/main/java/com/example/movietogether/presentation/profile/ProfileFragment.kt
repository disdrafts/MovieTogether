package com.example.movietogether.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.movietogether.R
import com.example.movietogether.data.local.AvatarStorage
import com.example.movietogether.databinding.FragmentProfileBinding
import com.example.movietogether.presentation.auth.LoginFragment
import com.example.movietogether.presentation.watch.WatchRoomFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var avatarStorage: AvatarStorage

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@registerForActivityResult
        viewModel.saveAvatar(avatarStorage, uri)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        avatarStorage = AvatarStorage(requireContext())

        binding.tvUsername.text = viewModel.username
        binding.tvEmail.text = viewModel.email
        viewModel.loadAvatar(avatarStorage)

        binding.ivAvatar.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnJoinRoom.setOnClickListener {
            viewModel.joinRoom(binding.etRoomCode.text?.toString().orEmpty())
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.GONE
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, LoginFragment())
                .commit()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        ProfileUiState.Idle -> Unit
                        ProfileUiState.Loading -> {
                            Toast.makeText(requireContext(), "Loading...", Toast.LENGTH_SHORT).show()
                        }
                        is ProfileUiState.RoomFound -> {
                            viewModel.resetState()
                            parentFragmentManager.beginTransaction()
                                .replace(
                                    R.id.fragmentContainer,
                                    WatchRoomFragment.newInstance(
                                        state.room.roomId,
                                        state.room.movieId,
                                        state.room.hostUsername
                                    )
                                )
                                .addToBackStack(null)
                                .commit()
                        }
                        is ProfileUiState.AvatarUpdated -> bindAvatar(state.path)
                        is ProfileUiState.Error -> {
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            viewModel.resetState()
                        }
                    }
                }
            }
        }
    }

    private fun bindAvatar(path: String?) {
        if (path.isNullOrBlank()) {
            binding.ivAvatar.setImageDrawable(null)
            binding.ivAvatar.setBackgroundColor(resources.getColor(R.color.accent, null))
            return
        }
        Glide.with(this)
            .load(File(path))
            .circleCrop()
            .into(binding.ivAvatar)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}