package com.amarchaud.amdraganddrop.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.amarchaud.amdraganddrop.domain.entity.EntityOnePerson


@Database(entities = [EntityOnePerson::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun peopleDao(): PeopleDao
}