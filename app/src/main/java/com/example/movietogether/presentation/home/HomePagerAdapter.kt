package com.example.movietogether.presentation.home

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class HomePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> MovieListFragment.newInstance(MovieListFragment.TYPE_POPULAR)
            1 -> MovieListFragment.newInstance(MovieListFragment.TYPE_TOP250)
            else -> throw IllegalArgumentException("Invalid position")
        }
    }

    override fun getItemCount(): Int = 2

}