package com.example.movietogether.presentation.watch

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movietogether.R
import com.example.movietogether.data.Session
import com.example.movietogether.data.remote.repository.MovieRepository
import com.example.movietogether.data.remote.ws.StompRoomClient
import com.example.movietogether.databinding.FragmentWatchRoomBinding
import com.example.movietogether.presentation.player.PlayerFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WatchRoomFragment : Fragment() {

    companion object {
        private const val ARG_ROOM_ID = "room_id"
        private const val ARG_MOVIE_ID = "movie_id"
        private const val ARG_HOST = "host"
        private const val WS_URL = "ws://127.0.0.1:8080/ws"

        fun newInstance(
            roomId: String,
            movieId: Long,
            hostUsername: String
        ): WatchRoomFragment {
            return WatchRoomFragment().apply {
                arguments = bundleOf(
                    ARG_ROOM_ID to roomId,
                    ARG_MOVIE_ID to movieId,
                    ARG_HOST to hostUsername
                )
            }
        }
    }

    private var _binding: FragmentWatchRoomBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var movieRepository: MovieRepository

    private var roomClient: StompRoomClient? = null
    private lateinit var chatAdapter: ChatAdapter
    private var roomId: String = ""
    private var movieId: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWatchRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        roomId = arguments?.getString(ARG_ROOM_ID).orEmpty()
        movieId = arguments?.getLong(ARG_MOVIE_ID) ?: 0L
        val host = arguments?.getString(ARG_HOST).orEmpty()

        binding.tvRoomId.text = getString(R.string.room_code_label, roomId)
        binding.tvHost.text = getString(R.string.room_host, host)

        binding.btnShareCode.setOnClickListener {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_TEXT, getString(R.string.room_share, roomId))
                type = "text/plain"
            }
            startActivity(Intent.createChooser(sendIntent, null))
        }

        chatAdapter = ChatAdapter()
        binding.rvChat.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChat.adapter = chatAdapter

        roomClient = StompRoomClient(
            onChatMessage = { msg ->
                requireActivity().runOnUiThread {
                    chatAdapter.addMessage(msg)
                    binding.rvChat.scrollToPosition(chatAdapter.itemCount - 1)
                }
            },
            onConnected = {
                requireActivity().runOnUiThread {
                    binding.tvWsStatus.text = "Online"
                }
            },
            onError = { error ->
                requireActivity().runOnUiThread {
                    binding.tvWsStatus.text = "Error: $error"
                }
            }
        )
        roomClient?.connect(WS_URL, roomId)

        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text?.toString()?.trim().orEmpty()
            if (text.isEmpty()) return@setOnClickListener
            roomClient?.sendChat(Session.username, text)
            binding.etMessage.setText("")
        }

        binding.btnOpenPlayer.setOnClickListener {
            openPlayer()
        }
    }

    private fun openPlayer() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val movie = movieRepository.getMovieById(movieId)
                val videoUrl = movie.videoUrl
                if (videoUrl.isNullOrBlank()) {
                    Toast.makeText(requireContext(), R.string.movie_no_trailer, Toast.LENGTH_SHORT).show()
                    return@launch
                }
                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragmentContainer,
                        PlayerFragment.newInstance(videoUrl, roomId)
                    )
                    .addToBackStack(null)
                    .commit()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message ?: "Error", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        roomClient?.disconnect()
        roomClient = null
        _binding = null
        super.onDestroyView()
    }
}