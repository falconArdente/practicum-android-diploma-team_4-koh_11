package ru.practicum.android.diploma.favorites.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.favorites.presentation.state.FavoritesListState
import ru.practicum.android.diploma.favorites.presentation.viewmodel.FavoritesViewModel

class FavoritesJobsFragment : Fragment() {
    private val viewModel by viewModel<FavoritesViewModel>()
    private val clickerForItem = View.OnClickListener {
        // viewModel.openForDetails()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites_jobs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.stateToObserve.observe(viewLifecycleOwner) { render(it) }
    }

    private fun render(state: FavoritesListState) {

    }

}
