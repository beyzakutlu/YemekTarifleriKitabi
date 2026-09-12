package com.beyzakutlu.yemektariflerikitabi.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.beyzakutlu.yemektariflerikitabi.model.Tarif

// Kopyalayıp yapıştırdık değişiklik yaptık

@Database(entities = [Tarif::class], version = 1)
abstract class TarifDatabase : RoomDatabase() {
    abstract fun tarifDao(): TarifDao
}
