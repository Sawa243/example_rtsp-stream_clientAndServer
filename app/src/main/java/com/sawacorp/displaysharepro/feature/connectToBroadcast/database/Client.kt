package com.sawacorp.displaysharepro.feature.connectToBroadcast.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Client(
    @ColumnInfo(name = "device") val device: String?,
    @ColumnInfo(name = "token") val token: String?,
){
    @PrimaryKey(autoGenerate = true) var id: Long = 0
}