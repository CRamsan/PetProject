package com.cramsan.petproject.appcore.storage.implementation

import android.content.Context
import com.cramsan.petproject.appcore.storage.ModelStorageDAO
import com.cramsan.petproject.appcore.storage.ModelStoragePlatformProvider
import com.cramsan.petproject.appcore.storage.implementation.sqldelight.SQLDelightDAO
import com.cramsan.petproject.db.PetProjectDB
import com.squareup.sqldelight.android.AndroidSqliteDriver

/**
 * Android implementation of [ModelStoragePlatformProvider]. Internally
 * it uses SQLite throught the [AndroidSqliteDriver] of SQLDelight.
 */
class ModelStorageAndroidProvider(private val context: Context) :
    ModelStoragePlatformProvider {
    override fun provide(): ModelStorageDAO {
        val sqlDriver = AndroidSqliteDriver(PetProjectDB.Schema, context, "petproject.db")
        return SQLDelightDAO(sqlDriver)
    }
}
