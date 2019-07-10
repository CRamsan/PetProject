package com.cramsan.petproject.appcore.storage.implementation

import com.cramsan.petproject.appcore.framework.CoreFrameworkAPI
import com.cramsan.petproject.appcore.model.AnimalType
import com.cramsan.petproject.appcore.model.Plant
import com.cramsan.petproject.appcore.model.PlantMetadata
import com.cramsan.petproject.appcore.model.Toxicity
import com.cramsan.petproject.appcore.storage.ModelStorageInterface

class ModelStorage(initializer: ModelStorageInitializer) : ModelStorageInterface {

    var sqlDelightDAO: SQLDelightDAO = SQLDelightDAO(initializer)

    override fun getPlants(animalType: AnimalType, locale: String): List<Plant> {
        CoreFrameworkAPI.threadUtil.assertIsBackgroundThread()

        val list = sqlDelightDAO.getCustomPlantEntries(animalType, locale)
        val mutableList = mutableListOf<Plant>()

        list.forEach {
            mutableList.add(
                Plant(
                    it.id.toInt(),
                    it.scientific_name,
                    it.main_name,
                    it.common_names,
                    it.image_url,
                    it.family,
                    it.is_toxic
                ))
        }
        return mutableList
    }

    override fun getPlant(animalType: AnimalType, plantId: Int, locale: String): Plant {
        CoreFrameworkAPI.threadUtil.assertIsBackgroundThread()

        val plantEntry = sqlDelightDAO.getCustomPlantEntry(plantId.toLong(), animalType,locale)

        return Plant(
            plantEntry.id.toInt(),
            plantEntry.scientific_name,
            plantEntry.main_name,
            plantEntry.common_names,
            plantEntry.image_url,
            plantEntry.family,
            plantEntry.is_toxic
        )
    }

    override fun getPlantMetadata(animalType: AnimalType, plantId: Int, locale: String) : PlantMetadata {
        return PlantMetadata(0, plantId, animalType, true, "This asd" +
                "a sdas dasdasdasd wq da s da  eqwew dwad " +
                "a ddasdasdasd wq da s da  eqwew dwad " +
                "as asd sa dasdasdasd wq da s da  eqwew dwad " +
                " dasdasdasd wq da s da  eqwew dwad as  sa" +
                " dasdasdasd wq da s da  eqwew dwad ", "https://www.google.com")
    }

    fun insertPlant(plant: Plant, plantMetadata: PlantMetadata, locale: String) {
        sqlDelightDAO.insertPlantEntry(plant.exactName, plant.mainCommonName, plant.family, plant.imageUrl)
        val plantEntry = sqlDelightDAO.getPlantEntry(plant.exactName)
        plant.commonNames.split(", ").forEach {
            sqlDelightDAO.insertPlantCommonNameEntry(it, plantEntry.id, locale)
        }
        sqlDelightDAO.insertPlantFamilyNameEntry(plant.family, plantEntry.id, locale)
        sqlDelightDAO.insertPlantMainNameEntry(plant.mainCommonName, plantEntry.id, locale)
        sqlDelightDAO.insertDescriptionEntry(plantEntry.id, plantMetadata.animalType, plantMetadata.description, locale)
        sqlDelightDAO.insertToxicityEntry(plantMetadata.isToxic, plantEntry.id, plantMetadata.animalType, plantMetadata.source)
    }

    fun deleteAll() {
        CoreFrameworkAPI.threadUtil.assertIsBackgroundThread()
        sqlDelightDAO.deleteAll()
    }
}