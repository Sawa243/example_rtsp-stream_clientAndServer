package com.sawacorp.displaysharepro.feature.connectToBroadcast.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.sawacorp.displaysharepro.feature.connectToBroadcast.database.Client

@Dao
interface ClientDao {
    @Query("SELECT * FROM client")
    fun getAll(): LiveData<List<Client>>

    @Query("SELECT * FROM client WHERE token = :token")
    fun getByToken(token: String): Client?

    @Query("SELECT EXISTS(SELECT * FROM client WHERE token = :token)")
    suspend fun isTokenIsExist(token : String) : Boolean

    @Insert
    suspend fun insert(client: Client)

    @Delete
    suspend fun delete(client: Client)
}