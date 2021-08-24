package com.amarchaud.amdraganddrop.data.database

import androidx.room.*
import com.amarchaud.amdraganddrop.domain.entity.EntityOnePerson
import kotlinx.coroutines.flow.Flow

@Dao
interface PeopleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(onePerson: EntityOnePerson): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(l: List<EntityOnePerson>)

    @Query("UPDATE people SET position_db=(position_db+1) WHERE position_db >= :fromPos AND position_db <= :toPos")
    suspend fun incrementPosition(fromPos: Int, toPos: Int)

    @Query("UPDATE people SET position_db=(position_db-1) WHERE position_db >= :fromPos AND position_db <= :toPos")
    suspend fun decrementPosition(fromPos: Int, toPos: Int)

    @Update
    suspend fun update(onePerson: EntityOnePerson)

    @Transaction
    @Query("SELECT Count(*) from people")
    suspend fun getSize(): Int

    @Delete
    suspend fun delete(onePerson: EntityOnePerson)

    @Transaction
    @Query("SELECT * from people ORDER BY position_db ASC")
    suspend fun getAllPeople(): List<EntityOnePerson>

    @Transaction
    @Query("SELECT * from people ORDER BY position_db ASC")
    fun getAllPeopleFlow(): Flow<List<EntityOnePerson>>
}