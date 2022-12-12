package com.sawacorp.displaysharepro.feature.connectToBroadcast.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Query("SELECT * FROM client")
    fun getAll(): Flow<List<Client>>

    @Query("SELECT * FROM client WHERE token = :token")
    fun getByToken(token: String): Client?

    @Query("SELECT EXISTS(SELECT * FROM client WHERE token = :token)")
    suspend fun isTokenIsExist(token: String): Boolean

    @Insert
    suspend fun insert(client: Client)

    @Delete
    suspend fun delete(client: Client)
}