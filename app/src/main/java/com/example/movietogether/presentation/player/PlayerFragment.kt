package com.example.movietogether.presentation.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.mediacodec.MediaCodecUtil
import com.example.movietogether.R
import com.example.movietogether.data.Session
import com.example.movietogether.data.remote.dto.PlaybackStateDto
import com.example.movietogether.data.remote.ws.StompRoomClient
import com.example.movietogether.databinding.FragmentPlayerBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@AndroidEntryPoint
class PlayerFragment : Fragment() {

    companion object {
        private const val ARG_URL = "video_url"
        private const val ARG_ROOM_ID = "room_id"
        private const val BASE_URL = "http://127.0.0.1:8080"
        private const val WS_URL = "ws://127.0.0.1:8080/ws"

        fun newInstance(videoUrl: String, roomId: String? = null): PlayerFragment {
            return PlayerFragment().apply {
                arguments = bundleOf(
                    ARG_URL to videoUrl,
                    ARG_ROOM_ID to roomId
                )
            }
        }
    }

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private var player: ExoPlayer? = null
    private var roomClient: StompRoomClient? = null
    private var roomId: String? = null
    private var applyingRemote = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = bars.top)
            insets
        }

        val relativeUrl = arguments?.getString(ARG_URL)
        roomId = arguments?.getString(ARG_ROOM_ID)

        if (relativeUrl.isNullOrBlank()) {
            Toast.makeText(requireContext(), R.string.player_error, Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        val fullUrl = if (relativeUrl.startsWith("http")) {
            relativeUrl
        } else {
            BASE_URL + relativeUrl
        }

        binding.btnClose.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        setupPlayer(fullUrl)
        setupSyncIfNeeded()
    }

    private fun setupPlayer(url: String) {
        val softwareSelector = MediaCodecSelector { mimeType, requiresSecure, requiresTunneling ->
            MediaCodecUtil.getDecoderInfos(mimeType, requiresSecure, requiresTunneling)
                .filter { codec -> codec.softwareOnly }
        }

        val renderersFactory = DefaultRenderersFactory(requireContext())
            .setEnableDecoderFallback(true)
            .setMediaCodecSelector(softwareSelector)

        player = ExoPlayer.Builder(requireContext(), renderersFactory).build().also { exoPlayer ->
            binding.playerView.player = exoPlayer
            exoPlayer.setMediaItem(MediaItem.fromUri(url))
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true

            exoPlayer.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (!applyingRemote) sendSync()
                }
            })
        }
    }

    private fun setupSyncIfNeeded() {
        val id = roomId
        if (id.isNullOrBlank()) return

        roomClient = StompRoomClient(
            onPlaybackState = { state -> applyRemoteState(state) },
            onError = { msg ->
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
            }
        )
        roomClient?.connect(WS_URL, id)
    }

    private fun sendSync() {
        val exo = player ?: return
        val id = roomId ?: return

        roomClient?.sendPlayback(
            PlaybackStateDto(
                roomId = id,
                currentTime = exo.currentPosition / 1000.0,
                isPlaying = exo.isPlaying,
                username = Session.username
            )
        )
    }

    private fun applyRemoteState(state: PlaybackStateDto) {
        if (state.username == Session.username) return

        requireActivity().runOnUiThread {
            val exo = player ?: return@runOnUiThread
            applyingRemote = true
            try {
                val targetMs = (state.currentTime * 1000).toLong()
                if (abs(exo.currentPosition - targetMs) > 1000) {
                    exo.seekTo(targetMs)
                }
                exo.playWhenReady = state.isPlaying
            } finally {
                applyingRemote = false
            }
        }
    }

    override fun onStop() {
        super.onStop()
        player?.pause()
        if (roomId != null) sendSync()
    }

    override fun onDestroyView() {
        roomClient?.disconnect()
        roomClient = null
        player?.release()
        player = null
        _binding = null
        super.onDestroyView()
    }
}