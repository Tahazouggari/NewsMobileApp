package com.example.newsprojectpractice.db

import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.newsprojectpractice.models.Source

interface Converters {
    @TypeConverter
    fun fromSource(source: Source):String{
        return source.name
    }
    @TypeConverter
    fun toSource(name : String ):Source{
        return Source(name,name)
    }
}