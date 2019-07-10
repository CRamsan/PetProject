package com.cramsan.petproject.appcore.storage.implementation

import com.cramsan.petproject.appcore.model.AnimalType
import com.cramsan.petproject.appcore.storage.ModelStorageDAO
import com.cramsan.petproject.db.*

class SQLDelightDAO(initializer: ModelStorageInitializer) : ModelStorageDAO {

    private var database: PetProjectDB

    init {
        val sqlDriver = initializer.platformInitializer.getSqlDriver()
        database = PetProjectDB(sqlDriver,
            DescriptionAdapter = Description.Adapter(AnimalTypeAdapter()),
            ToxicityAdapter = Toxicity.Adapter(AnimalTypeAdapter())
        )
    }

    override fun insertPlantEntry(scientificName: String, mainCommonName: String, family: String, imageUrl: String) {
        database.plantQueries.insert(scientificName, imageUrl)
    }

    override fun insertPlantCommonNameEntry(commonName: String, plantId: Long, locale: String) {
        database.plantCommonNameQueries.insert(commonName, plantId, locale)
    }

    override fun insertPlantMainNameEntry(mainName: String, plantId: Long, locale: String) {
        database.plantMainNameQueries.insert(plantId, mainName, locale)
    }

    override fun insertPlantFamilyNameEntry(family: String, plantId: Long, locale: String) {
        database.plantFamilyQueries.insert(plantId, family, locale)
    }

    override fun insertToxicityEntry(isToxic: Boolean, plantId: Long, animalType: AnimalType, source:String) {
        database.toxicityQueries.insert(plantId, animalType, isToxic, source)
    }

    override fun insertDescriptionEntry(plantId: Long, animalType: AnimalType, description: String, locale: String) {
        return database.descriptionQueries.insert(plantId, animalType, description, locale)
    }

    override fun getPlantEntry(scientificName: String): Plant {
        return database.plantQueries.getPlant(scientificName).executeAsOne()
    }

    override fun getAllPlantEntries(): List<Plant> {
        return database.plantQueries.getAll().executeAsList()
    }

    override fun getPlantCommonNameEntries(plantId: Long, locale: String): List<PlantCommonName> {
        return database.plantCommonNameQueries.getPlantCommonNames(plantId, locale).executeAsList()
    }

    override fun getPlantMainNameEntry(plantId: Long, locale: String): PlantMainName {
        return database.plantMainNameQueries.getPlantMainName(plantId, locale).executeAsOne()
    }

    override fun getPlantFamilyEntry(plantId: Long, locale: String): PlantFamily {
        return database.plantFamilyQueries.getPlantFamily(plantId, locale).executeAsOne()
    }

    override fun getToxicityEntry(plantId: Long, animalType: AnimalType): Toxicity {
        return database.toxicityQueries.getToxicity(plantId, animalType).executeAsOne()
    }

    override fun getDescriptionEntry(plantId: Long, animalType: AnimalType, locale: String): Description {
        return database.descriptionQueries.getDescription(plantId, animalType, locale).executeAsOne()
    }

    override fun getCustomPlantEntries(animalType: AnimalType, locale: String): List<GetAllPlantsWithAnimalId> {
        return database.customProjectionsQueries.getAllPlantsWithAnimalId(animalType, locale).executeAsList()
    }

    override fun getCustomPlantEntry(plantId: Long, animalType: AnimalType, locale: String): GetPlantWithPlantIdAndAnimalId {
        return database.customProjectionsQueries.getPlantWithPlantIdAndAnimalId(animalType, plantId, locale).executeAsOne()
    }

    override fun deleteAll() {
        database.toxicityQueries.deleteAll()
        database.plantCommonNameQueries.deleteAll()
        database.plantQueries.deleteAll()
    }
}