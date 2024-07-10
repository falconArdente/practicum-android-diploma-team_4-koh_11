package ru.practicum.android.diploma.network.data.dto.responses

import com.google.gson.annotations.SerializedName
import ru.practicum.android.diploma.network.data.dto.linked.VacancyFunctTitle

class VacancySuggestionsResponse(
    @SerializedName("items")
    val vacancyPositionsList: List<ru.practicum.android.diploma.network.data.dto.linked.VacancyFunctTitle>
) : ru.practicum.android.diploma.network.data.dto.responses.Response()
