package ru.practicum.android.diploma.search.presentation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentSearchJobBinding
import ru.practicum.android.diploma.details.presentation.JobDetailsFragment
import ru.practicum.android.diploma.filter.presentation.FilterSettingsFragment
import ru.practicum.android.diploma.search.domain.model.Vacancy
import ru.practicum.android.diploma.search.presentation.state.SearchFragmentState
import ru.practicum.android.diploma.search.presentation.viewmodel.SearchViewModel
import ru.practicum.android.diploma.search.ui.SearchRecyclerViewEvent
import ru.practicum.android.diploma.search.ui.SearchRepeatHandler
import ru.practicum.android.diploma.search.ui.VacancyAdapter
import ru.practicum.android.diploma.search.ui.VacancyPositionSuggestsAdapter

class SearchJobFragment : Fragment() {
    private var _binding: FragmentSearchJobBinding? = null
    private val binding get() = _binding!!
    private var suggestionsAdapter: VacancyPositionSuggestsAdapter? = null
    private val viewModel by viewModel<SearchViewModel>()
    private val adapter = VacancyAdapter(emptyList(), clickListenerFun())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchJobBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewHolderInit()

        viewModel.fragmentStateLiveData().observe(viewLifecycleOwner) {
            allViewGone()
            renderSearchState(it)
        }

        searchInputClick()
        onScrollListener()
        viewModel.filterStateToObserve.observe(viewLifecycleOwner) { setFilterIcon(it) }
        binding.searchJobsCountButton.setOnClickListener {
            viewModel.searchImmidiently(binding.searchInput.text.toString())
        }
        binding.searchFilterButton.setOnClickListener {
            val args = Bundle()
            args.putBoolean(FilterSettingsFragment.PATH_FROM_SEARCH, true)
            findNavController().navigate(R.id.action_searchJobFragment_to_filterSettingsFragment, args)
        }

        binding.searchInput.doOnTextChanged { text, _, _, _ ->
            viewModel.searchWithDebounce(text.toString())
            if (text.isNullOrEmpty()) {
                binding.searchInputIcon.background = requireActivity().getDrawable(R.drawable.icon_search)
            } else {
                binding.searchInputIcon.background = requireActivity().getDrawable(R.drawable.icon_cross)
                viewModel.getSuggestionsForSearch(text.toString())
            }
        }
        binding.searchInputIcon.setOnClickListener {
            binding.searchInput.setText(String())
            viewModel.updateState(SearchFragmentState.NoTextInInputEditText)

        }
        binding.searchInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.searchInput.showKeyboard(requireContext())
            }
        }
        binding.searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.searchInput.hideKeyboard(requireContext())
                viewModel.currentPage = 0
                viewModel.searchImmidiently(binding.searchInput.text.toString())
            }
            false
        }

        suggestionsAdapter = VacancyPositionSuggestsAdapter(requireActivity(), binding.searchInput)
        binding.searchInput.setAdapter(suggestionsAdapter)
        viewModel.suggestionsLivaData.observe(viewLifecycleOwner) { renderSuggestions(it) }
    }

    private fun renderSearchState(searchState: SearchFragmentState) {
        allViewGone()
        when (searchState) {
            is SearchFragmentState.SearchVacancy -> {
                adapter.updateList(searchState.searchVacancy)
                setVisible(
                    placeholderText = false,
                    list = true,
                    blueButton = true,
                    progress = false,
                    progressMini = false
                )
                setBlueButtonText(searchState)
            }

            is SearchFragmentState.Loading -> {
                binding.searchMiniProgressBar.isVisible = true
                setVisible(
                    placeholderText = false,
                    list = false,
                    blueButton = false,
                    progress = true,
                    progressMini = false
                )
            }

            is SearchFragmentState.NoResult -> {
                if (viewModel.currentPage != 0) {
                    showToast(requireActivity().getString(R.string.toast_server_error))
                    setVisible(
                        placeholderText = false,
                        list = true,
                        blueButton = true,
                        progress = false,
                        progressMini = false
                    )

                } else {
                    binding.searchPlaceholderImage.background =
                        requireActivity().getDrawable(R.drawable.picture_angry_cat)
                    binding.searchJobsCountButton.text = requireActivity().getString(R.string.no_such_vacancies)
                    binding.searchPlaceholderText.text =
                        requireActivity().getString(R.string.failed_list_vacancy)
                    setVisible(
                        placeholderText = true,
                        list = false,
                        blueButton = true,
                        progress = false,
                        progressMini = false
                    )
                }
            }

            is SearchFragmentState.ServerError -> {
                if (viewModel.currentPage != 0) {
                    showToast(requireActivity().getString(R.string.toast_no_internet))
                    setVisible(
                        placeholderText = false,
                        list = true,
                        blueButton = true,
                        progress = false,
                        progressMini = false
                    )

                } else if (searchState.searchVacancy.isEmpty()) {
                    binding.searchPlaceholderImage.background =
                        requireActivity().getDrawable(R.drawable.picture_funny_head)
                    binding.searchPlaceholderText.text =
                        requireActivity().getString(R.string.no_internet)
                    setVisible(
                        placeholderText = true,
                        list = false,
                        blueButton = false,
                        progress = false,
                        progressMini = false
                    )
                }
            }

            is SearchFragmentState.NoTextInInputEditText -> {
                binding.searchPlaceholderImage.background =
                    requireActivity().getDrawable(R.drawable.picture_looking_man)
                setVisible(
                    placeholderText = false,
                    list = false,
                    blueButton = false,
                    progress = false,
                    image = true,
                    progressMini = false
                )
            }

            is SearchFragmentState.LoadingNewPage -> {
                setVisible(
                    placeholderText = false,
                    list = true,
                    blueButton = true,
                    progress = false,
                    image = false,
                    progressMini = true
                )
            }

            else -> Unit
        }

    }

    private fun renderSuggestions(incomeSuggestions: List<String>) {
        suggestionsAdapter?.applyDataSet(incomeSuggestions)
    }

    private fun setVisible(
        placeholderText: Boolean,
        list: Boolean,
        blueButton: Boolean,
        progress: Boolean,
        progressMini: Boolean,
        image: Boolean = placeholderText,
    ) {
        with(binding) {
            searchPlaceholderText.isVisible = placeholderText
            searchPlaceholderImage.isVisible = image
            recyclerViewSearch.isVisible = list
            searchJobsCountButton.isVisible = blueButton
            searchProgressBar.isVisible = progress
            searchMiniProgressBar.isVisible = progressMini
        }
    }

    private fun setBlueButtonText(state: SearchFragmentState.SearchVacancy) {
        val pluralVacancy = resources.getQuantityString(
            R.plurals.plurals_vacancy,
            state.totalFoundVacancy
        )
        val foundVac =
            requireActivity().getString(
                R.string.found_x_vacancies,
                state.totalFoundVacancy.toString()
            )
        val text = " $foundVac $pluralVacancy"
        binding.searchJobsCountButton.text = text

    }

    private fun setFilterIcon(filterIsActive: Boolean) {
        binding.searchFilterButton.setImageDrawable(
            if (filterIsActive) {
                requireActivity().getDrawable(R.drawable.icon_filter_active)
            } else {
                requireActivity().getDrawable(R.drawable.icon_filter)
            }
        )
    }

    private fun allViewGone() {
        binding.searchProgressBar.visibility = View.GONE
        binding.recyclerViewSearch.visibility = View.GONE
        binding.searchPlaceholderImage.visibility = View.GONE
        binding.searchJobsCountButton.visibility = View.GONE
    }

    private fun clickListenerFun() = object : SearchRecyclerViewEvent {
        override fun onItemClick(vacancy: Vacancy) {
            if (viewModel.clickDebounce()) {
                findNavController().navigate(
                    R.id.action_searchJobFragment_to_jobDetailsFragment,
                    JobDetailsFragment.createArgs(vacancy.id)
                )
            }
        }
    }

    private fun viewHolderInit() {
        binding.recyclerViewSearch.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewSearch.adapter = adapter
    }

    private fun searchInputClick() {
        binding.searchInput.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrEmpty()) {
                binding.searchInputIcon.background = requireActivity().getDrawable(R.drawable.icon_search)
            } else {
                binding.searchInputIcon.background = requireActivity().getDrawable(R.drawable.icon_cross)
                viewModel.getSuggestionsForSearch(text.toString())
                viewModel.currentPage = 0
            }
        }
        binding.searchInputIcon.setOnClickListener {
            binding.searchInput.setText(String())
        }

        binding.searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.searchInput.hideKeyboard(requireContext())
            }
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun View.showKeyboard(context: Context) {
        val imm =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(this, 0)
    }

    private fun View.hideKeyboard(context: Context) {
        val inputManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(windowToken, 0)

    }

    override fun onResume() {
        super.onResume()
        viewModel.checkFilterStatus()
        doFilteredRepeatSequence()
    }

    private fun doFilteredRepeatSequence() {
        val repeatHandler = requireActivity()
        if (repeatHandler is SearchRepeatHandler) {
            if (repeatHandler.getRepeatBool()) viewModel.repeatSearch()
            repeatHandler.setRepeat(false)
        }
    }

    private fun onScrollListener() {
        binding.recyclerViewSearch.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    val pos =
                        (binding.recyclerViewSearch.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

                    val itemsCount = adapter.itemCount
                    if (pos >= itemsCount - 1) {
                        viewModel.updateState(SearchFragmentState.LoadingNewPage)
                        viewModel.onLastItemReached()
                    }
                }
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
