package com.cramsan.petproject.plantslist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cramsan.framework.logging.Severity
import com.cramsan.framework.logging.classTag
import com.cramsan.petproject.appcore.framework.CoreFrameworkAPI
import com.cramsan.petproject.appcore.model.AnimalType
import com.cramsan.petproject.appcore.model.Plant
import com.cramsan.petproject.appcore.model.PresentablePlant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlantListViewModel : ViewModel() {

    private val modelStore = CoreFrameworkAPI.modelProvider

    private val observablePlants = MutableLiveData<List<PresentablePlant>>()

    fun reloadPlants() {
        CoreFrameworkAPI.eventLogger.log(Severity.INFO, classTag(), "reloadPlants")
        viewModelScope.launch {
            loadPlants()
        }
    }

    fun searchPlants(query: String) {
        CoreFrameworkAPI.eventLogger.log(Severity.INFO, classTag(), "searchPlants")
        viewModelScope.launch {
            if (query.isEmpty()) {
                loadPlants()
            } else {
                filterPlants(query)
            }
        }
    }

    fun observablePlants(): LiveData<List<PresentablePlant>> {
        return observablePlants
    }

    private suspend fun loadPlants() = withContext(Dispatchers.IO)  {
        val plants = modelStore.getPlantsWithToxicity(AnimalType.CAT, "en")
        viewModelScope.launch {
            CoreFrameworkAPI.threadUtil.assertIsUIThread()
            observablePlants.value = plants
        }
    }

    private suspend fun filterPlants(query: String) = withContext(Dispatchers.IO)  {
        val plants = modelStore.getPlantsWithToxicityFiltered(AnimalType.DOG, query, "en")
        if (plants == null) {
            return@withContext
        }
        viewModelScope.launch {
            CoreFrameworkAPI.threadUtil.assertIsUIThread()
            observablePlants.value = plants
        }
    }
}
