package com.beyzakutlu.yemektariflerikitabi.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.beyzakutlu.yemektariflerikitabi.model.Tarif
import com.beyzakutlu.yemektariflerikitabi.view.TarifFragmentArgs
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

@Dao
interface TarifDao {

    @Query("SELECT * FROM Tarif")
    fun getAll() : Flowable<List<Tarif>>


    //id ye göre bul (fonk içinde isteyebiliyoruz)
    @Query("SELECT * FROM Tarif WHERE id= :id")
    fun findById (id:Int) : Flowable<Tarif>

    @Insert
    fun insert (tarif: Tarif) : Completable

    @Delete
    fun delete (tarif: Tarif) : Completable
}