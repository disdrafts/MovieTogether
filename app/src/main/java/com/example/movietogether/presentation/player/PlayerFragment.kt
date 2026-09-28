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
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import com.example.movietogether.R
import com.example.movietogether.databinding.FragmentPlayerBinding
import androidx.media3.exoplayer.mediacodec.MediaCodecUtil

class PlayerFragment : Fragment() {

    companion object {
        private const val ARG_URL = "video_url"
        private const val BASE_URL = "http://127.0.0.1:8080"

        fun newInstance(videoUrl: String): PlayerFragment {
            return PlayerFragment().apply {
                arguments = bundleOf(ARG_URL to videoUrl)
            }
        }
    }

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private var player: ExoPlayer? = null

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
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = bars.top)
            insets
        }

        val relativeUrl = arguments?.getString(ARG_URL)
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
        }
    }

    override fun onStop() {
        super.onStop()
        player?.pause()
    }

    override fun onDestroyView() {
        player?.release()
        player = null
        _binding = null
        super.onDestroyView()
    }
}