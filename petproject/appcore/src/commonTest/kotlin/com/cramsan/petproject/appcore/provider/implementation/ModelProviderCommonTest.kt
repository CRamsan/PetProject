package com.cramsan.petproject.appcore.provider.implementation

import com.cramsan.framework.logging.EventLoggerInterface
import com.cramsan.framework.logging.implementation.EventLogger
import com.cramsan.framework.preferences.PreferencesInterface
import com.cramsan.framework.preferences.implementation.Preferences
import com.cramsan.framework.thread.ThreadUtilInterface
import com.cramsan.framework.thread.implementation.ThreadUtil
import com.cramsan.petproject.appcore.model.AnimalType
import com.cramsan.petproject.appcore.model.ToxicityValue
import com.cramsan.petproject.appcore.provider.ModelProviderInterface
import com.cramsan.petproject.appcore.provider.ProviderConfig
import com.cramsan.petproject.appcore.storage.ModelStoragePlatformProvider
import com.cramsan.petproject.appcore.storage.implementation.DescriptionImpl
import com.cramsan.petproject.appcore.storage.implementation.ModelStorage
import com.cramsan.petproject.appcore.storage.implementation.PlantCommonNameImpl
import com.cramsan.petproject.appcore.storage.implementation.PlantFamilyImpl
import com.cramsan.petproject.appcore.storage.implementation.PlantImp
import com.cramsan.petproject.appcore.storage.implementation.PlantMainNameImpl
import com.cramsan.petproject.appcore.storage.implementation.ToxicityImpl
import io.mockk.mockk
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.newInstance
import org.kodein.di.provider
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class ModelProviderCommonTest {

    private val kodein = DI {
        bind<EventLoggerInterface>() with provider { mockk<EventLogger>(relaxUnitFun = true) }
        bind<ThreadUtilInterface>() with provider { mockk<ThreadUtil>(relaxUnitFun = true) }
        bind<ProviderConfig>() with provider { mockk<ProviderConfig>(relaxUnitFun = true) }
    }

    private lateinit var modelProvider: ModelProviderInterface
    private lateinit var modelProviderImpl: ModelProvider
    private lateinit var modelStorage: ModelStorage

    fun setUp(storagePlatformProvider: ModelStoragePlatformProvider, platformDelegate: PreferencesInterface) {
        val modelStorageImpl by kodein.newInstance { ModelStorage(storagePlatformProvider.provide(), instance(), instance()) }
        modelStorage = modelStorageImpl
        val preferences by kodein.newInstance { Preferences(platformDelegate) }
        val newModelProvider by kodein.newInstance { ModelProvider(instance(), instance(), modelStorage, preferences, instance()) }
        modelProviderImpl = newModelProvider
        modelProvider = modelProviderImpl
    }

    suspend fun testFiltering() = coroutineScope {
        insertBaseEntries()
        modelProviderImpl.isCatalogReady = true
        modelProvider.getPlantsWithToxicity(AnimalType.CAT, "en")
        var result = modelProvider.getPlantsWithToxicityFiltered(AnimalType.CAT, "100", "en")
        assertNotNull(result)
        assertEquals(result.size, 1)
        val job = launch {
            for (i in 0..99) {
                modelProvider.getPlantsWithToxicityFiltered(AnimalType.CAT, "100", "en")
            }
            result = modelProvider.getPlantsWithToxicityFiltered(AnimalType.CAT, "65", "en")
        }
        job.join()
        assertNotNull(result)
        assertEquals(result!!.size, 1)
    }

    private fun insertBaseEntries() {
        var descriptionId = 0L
        var familyId = 0L
        var commonNameId = 0L
        var mainNameId = 0L
        var toxicityId = 0L
        for (i in 1..100) {
            val plantId: Long = i.toLong()
            modelStorage.insertPlant(
                PlantImp(
                    plantId,
                    "$i",
                    "https://www.aspca.org"
                )
            )
            modelStorage.insertDescription(DescriptionImpl(descriptionId++, plantId, AnimalType.CAT, "en", "desc"))
            modelStorage.insertPlantFamily(PlantFamilyImpl(familyId++, "Some Family $i", plantId, "en"))
            modelStorage.insertPlantCommonName(PlantCommonNameImpl(commonNameId++, "Name $i", plantId, "en"))
            modelStorage.insertPlantMainName(PlantMainNameImpl(mainNameId++, "$i $i", plantId, "en"))
            modelStorage.insertToxicity(ToxicityImpl(toxicityId++, plantId, AnimalType.CAT, ToxicityValue.NON_TOXIC, ""))
            modelStorage.insertToxicity(ToxicityImpl(toxicityId++, plantId, AnimalType.DOG, ToxicityValue.NON_TOXIC, ""))
        }
    }
}
