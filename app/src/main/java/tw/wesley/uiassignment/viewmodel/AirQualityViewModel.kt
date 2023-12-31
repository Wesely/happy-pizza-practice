package tw.wesley.uiassignment.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber
import tw.wesley.uiassignment.data.local.AirData
import tw.wesley.uiassignment.data.local.AirData.Companion.INVALID_INT
import tw.wesley.uiassignment.extensions.median
import tw.wesley.uiassignment.repo.AirDataRepository
import javax.inject.Inject

@FlowPreview
@HiltViewModel
class AirQualityViewModel @Inject constructor(
    // Dependency injection to get the repository
    private val airDataRepository: AirDataRepository
) : ViewModel() {

    /**
     * Use a UiState to manage the behave of several data with the Searching mode.
     * On each update, we can copy this UiState and only modify the part we want.
     */
    data class UiState(
        var isSearching: Boolean = false,
        var searchingKeyword: String = "",
        var horizontalAirDataList: List<AirData> = emptyList(),
        var verticalAirDataList: List<AirData> = emptyList(),
        var searchResultAirDataList: List<AirData> = emptyList(),
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    // As SearchText.onChange might variate fast, use this flow to debounce
    private val _queryFlow = MutableStateFlow("")

    private var qualityThreshold = 30

    init {
        // Here we start collecting the Flow from our repository when viewModel is live
        viewModelScope.launch {
            // collectLatest would discard and we start processing the new one.
            airDataRepository.getListAirDataFlow().collectLatest { list ->
                Timber.d("getListAirDataFlow/dataSize=${list.size}}")

                // find the median from the list, original order unchanged
                qualityThreshold = list.map { it.pm25 }.filter { it != INVALID_INT }.median() ?: 0

                // Sort and split the list based on PM2.5 value
                list.sortedBy { it.pm25 }.apply {
                    // Determine which air data goes to which LiveData based on PM2.5 value
                    if (qualityThreshold == 0) {
                        // If all PM2.5 values are 0, the first few items go to horizontalAirLiveData, the rest to verticalAirLiveData
                        _uiState.value = _uiState.value.copy(
                            horizontalAirDataList = take(MIN_HORIZONTAL_ITEMS),
                            verticalAirDataList = drop(MIN_HORIZONTAL_ITEMS)
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            horizontalAirDataList = filter { it.pm25 <= qualityThreshold },
                            verticalAirDataList = filter { it.pm25 > qualityThreshold }
                        )
                    }
                }
            }
        }

        // wait for 0.5 sec before user finish their typing
        viewModelScope.launch {
            _queryFlow
                .debounce(500)  // debounce for 500 milliseconds
                .distinctUntilChanged()  // only proceed if the query is different from the last one
                .collect { query ->
                    if (query.isBlank()) {
                        // send an empty searching uiState
                        _uiState.value = _uiState.value.copy(
                            searchResultAirDataList = emptyList(),
                            searchingKeyword = query
                        )
                    } else {
                        // send search result uiState
                        _uiState.value = _uiState.value.copy(
                            searchResultAirDataList = airDataRepository.queryAirData(query),
                            searchingKeyword = query
                        )
                    }
                }
        }
    }

    /**
     * This function is called when we want to refresh the air data.
     * It should ask repo to fetch latest data and write to DB.
     * We collect the DB as a flow when it's change in the init{} already.
     */
    fun fetchAirData() {
        // It runs in a coroutine to avoid blocking the main thread
        viewModelScope.launch {
            airDataRepository.fetchAndStoreAirQualityData()
        }
    }

    /**
     * Set the isSearching mode
     */
    fun activateSearching(activate: Boolean) {
        _uiState.value = _uiState.value.copy(isSearching = activate)
    }

    fun queryAirData(keyword: String?) {
        // send the latest keyword to flow
        _queryFlow.value = keyword ?: ""
    }

    companion object {
        // A constant that represents the minimum number of items we want to display in the horizontal recycler view
        private const val MIN_HORIZONTAL_ITEMS = 5
    }
}
