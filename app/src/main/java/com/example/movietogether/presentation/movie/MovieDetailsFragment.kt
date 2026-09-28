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
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.movietogether.R
import com.example.movietogether.data.Session
import com.example.movietogether.data.remote.dto.KinopoiskMovieDto
import com.example.movietogether.data.remote.dto.MovieDto
import com.example.movietogether.data.remote.repository.CollectionRepository
import com.example.movietogether.data.remote.repository.MovieRepository
import com.example.movietogether.data.remote.repository.RoomRepository
import kotlinx.coroutines.launch
import com.example.movietogether.databinding.FragmentMovieDetailsBinding

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
    }

    private var _binding: FragmentMovieDetailsBinding? = null
    private val binding get() = _binding!!

    private val repository = MovieRepository()
    private var localMovie: MovieDto? = null

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
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = bars.top)
            insets
        }

        val kinopoiskId = arguments?.getLong(ARG_ID) ?: 0L
        val title = arguments?.getString(ARG_TITLE).orEmpty()
        val overview = arguments?.getString(ARG_OVERVIEW)
        val year = arguments?.getInt(ARG_YEAR)
        val rating = arguments?.getDouble(ARG_RATING)
        val poster = arguments?.getString(ARG_POSTER)

        binding.tvTitle.text = title
        binding.tvOverview.text = overview ?: "—"
        binding.tvYear.text = getString(R.string.movie_year, year?.toString() ?: "—")
        binding.tvRating.text = getString(
            R.string.movie_rating,
            rating?.toString() ?: "—"
        )

        Glide.with(this)
            .load(poster)
            .centerCrop()
            .into(binding.ivPoster)

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnWatch.setOnClickListener {
            val videoUrl = localMovie?.videoUrl
            if (videoUrl.isNullOrBlank()) {
                Toast.makeText(requireContext(), R.string.movie_no_trailer, Toast.LENGTH_SHORT).show()
            } else {
                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragmentContainer,
                        com.example.movietogether.presentation.player.PlayerFragment.newInstance(videoUrl)
                    )
                    .addToBackStack(null)
                    .commit()
            }
        }
        binding.btnAddToList.setOnClickListener {
            val localId = localMovie?.id
            if (localId == null) {
                Toast.makeText(requireContext(), "Movie is not on server", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showChooseCollectionDialog(localId)
        }

        binding.btnWatchTogether.setOnClickListener {
            val localId = localMovie?.id
            if (localId == null) {
                Toast.makeText(requireContext(), "Movie is not on server", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            createRoom(localId)
        }

        checkTrailer(kinopoiskId)
    }

    private fun checkTrailer(kinopoiskId: Long) {
        binding.progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                localMovie = repository.getByKinopoiskId(kinopoiskId)
                val hasVideo = !localMovie?.videoUrl.isNullOrBlank()
                binding.btnWatch.isEnabled = hasVideo
                if (!hasVideo) {
                    binding.btnWatch.text = getString(R.string.movie_no_trailer)
                }
            } catch (e: Exception) {
                binding.btnWatch.isEnabled = false
                binding.btnWatch.text = getString(R.string.movie_no_trailer)
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showChooseCollectionDialog(movieId: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val collections = CollectionRepository().getUserCollections(Session.userId)
                if (collections.isEmpty()) {
                    Toast.makeText(requireContext(), "Create a collection first", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val names = collections.map { it.name }.toTypedArray()

                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.movie_add_to_list)
                    .setItems(names) { _, which ->
                        val selected = collections[which]
                        addToCollection(selected.id, movieId)
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message ?: "Error", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun addToCollection(collectionId: Long, movieId: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                CollectionRepository().addMovieToCollection(collectionId, movieId)
                Toast.makeText(requireContext(), R.string.movie_added_to_list, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message ?: "Error", Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun createRoom(movieId: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val room = RoomRepository().createRoom(
                    movieId = movieId,
                    hostUserId = Session.userId,
                    hostUsername = Session.username
                )

                Toast.makeText(
                    requireContext(),
                    getString(R.string.room_created, room.roomId),
                    Toast.LENGTH_LONG
                ).show()

                val sendIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    putExtra(
                        android.content.Intent.EXTRA_TEXT,
                        getString(R.string.room_share, room.roomId)
                    )
                    type = "text/plain"
                }
                startActivity(android.content.Intent.createChooser(sendIntent, null))
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message ?: "Error", Toast.LENGTH_LONG).show()
            }
        }
    }
}