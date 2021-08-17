package com.example.sos.core.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contact")
data class Contact(
    @PrimaryKey
    @ColumnInfo(name="id")
    val id: Int,
    @ColumnInfo(name="name")
    var name: String,
    @ColumnInfo(name = "number")
    var number:String,
    @ColumnInfo(name = "isSelected")
    var isSelected:Boolean
    )
