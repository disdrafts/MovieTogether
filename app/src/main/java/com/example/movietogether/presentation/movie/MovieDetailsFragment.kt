package com.example.movietogether.presentation.movie

import android.app.AlertDialog
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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.movietogether.R
import com.example.movietogether.data.remote.dto.KinopoiskMovieDto
import com.example.movietogether.data.remote.dto.MovieDto
import com.example.movietogether.databinding.FragmentMovieDetailsBinding
import com.example.movietogether.presentation.player.PlayerFragment
import com.example.movietogether.presentation.watch.WatchRoomFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MovieDetailsFragment : Fragment() {

    companion object {
        private const val ARG_ID = "id"
        private const val ARG_TITLE = "title"
        private const val ARG_OVERVIEW = "overview"
        private const val ARG_YEAR = "year"
        private const val ARG_RATING = "rating"
        private const val ARG_POSTER = "poster"

        fun newInstance(movie: KinopoiskMovieDto): MovieDetailsFragment {
            return MovieDetailsFragment().apply {
                arguments = bundleOf(
                    ARG_ID to movie.id,
                    ARG_TITLE to movie.title,
                    ARG_OVERVIEW to movie.overview,
                    ARG_YEAR to movie.year,
                    ARG_RATING to movie.rating,
                    ARG_POSTER to movie.posterUrl
                )
            }
        }

        fun newInstance(movie: MovieDto): MovieDetailsFragment {
            return MovieDetailsFragment().apply {
                arguments = bundleOf(
                    ARG_ID to (movie.kinopoiskId ?: movie.id),
                    ARG_TITLE to movie.title,
                    ARG_OVERVIEW to movie.description,
                    ARG_YEAR to movie.year,
                    ARG_RATING to movie.rating,
                    ARG_POSTER to movie.posterUrl
                )
            }
        }
    }

    private var _binding: FragmentMovieDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MovieDetailsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = bars.top)
            insets
        }

        viewModel.initFromArgs(
            kinopoiskId = arguments?.getLong(ARG_ID) ?: 0L,
            title = arguments?.getString(ARG_TITLE).orEmpty(),
            overview = arguments?.getString(ARG_OVERVIEW),
            year = arguments?.getInt(ARG_YEAR),
            rating = arguments?.getDouble(ARG_RATING),
            posterUrl = arguments?.getString(ARG_POSTER)
        )

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnWatch.setOnClickListener {
            val videoUrl = viewModel.uiState.value.localMovie?.videoUrl
            if (videoUrl.isNullOrBlank()) {
                Toast.makeText(requireContext(), R.string.movie_no_trailer, Toast.LENGTH_SHORT).show()
            } else {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, PlayerFragment.newInstance(videoUrl))
                    .addToBackStack(null)
                    .commit()
            }
        }

        binding.btnAddToList.setOnClickListener {
            viewModel.onAddToListClick()
        }

        binding.btnWatchTogether.setOnClickListener {
            viewModel.createRoom()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    bindArgs(state)
                    bindTrailer(state)
                    handleEvent(state.event)
                }
            }
        }
    }

    private fun bindArgs(state: MovieDetailsUiState) {
        val args = state.args ?: return
        binding.tvTitle.text = args.title
        binding.tvOverview.text = args.overview ?: "—"
        binding.tvYear.text = getString(R.string.movie_year, args.year?.toString() ?: "—")
        binding.tvRating.text = getString(R.string.movie_rating, args.rating?.toString() ?: "—")

        Glide.with(this)
            .load(args.posterUrl)
            .centerCrop()
            .into(binding.ivPoster)
    }

    private fun bindTrailer(state: MovieDetailsUiState) {
        binding.progressBar.visibility =
            if (state.isLoadingTrailer) View.VISIBLE else View.GONE

        binding.btnWatch.isEnabled = state.hasTrailer
        binding.btnWatch.text = if (state.hasTrailer) {
            getString(R.string.movie_watch)
        } else {
            getString(R.string.movie_no_trailer)
        }
    }

    private fun handleEvent(event: MovieDetailsEvent) {
        when (event) {
            MovieDetailsEvent.Idle -> Unit

            is MovieDetailsEvent.ShowCollections -> {
                val names = event.collections.map { it.name }.toTypedArray()
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.movie_add_to_list)
                    .setItems(names) { _, which ->
                        viewModel.addToCollection(event.collections[which].id, event.movieId)
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
                viewModel.consumeEvent()
            }

            is MovieDetailsEvent.RoomCreated -> {
                viewModel.consumeEvent()
                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragmentContainer,
                        WatchRoomFragment.newInstance(
                            event.room.roomId,
                            event.room.movieId,
                            event.room.hostUsername
                        )
                    )
                    .addToBackStack(null)
                    .commit()
            }

            is MovieDetailsEvent.Error -> {
                Toast.makeText(requireContext(), event.message, Toast.LENGTH_LONG).show()
                viewModel.consumeEvent()
            }

            is MovieDetailsEvent.Info -> {
                Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                viewModel.consumeEvent()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}