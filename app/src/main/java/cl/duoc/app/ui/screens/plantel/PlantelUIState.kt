package cl.duoc.app.ui.screens.plantel

import cl.duoc.app.data.model.PlantelPlant

data class PlantelUIState(
    val plants: List<PlantelPlant> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
