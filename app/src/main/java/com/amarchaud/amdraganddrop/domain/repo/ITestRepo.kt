package com.amarchaud.amdraganddrop.domain.repo

import arrow.core.Either
import com.amarchaud.amdraganddrop.domain.ApiOnePerson
import com.amarchaud.amdraganddrop.domain.entity.EntityOnePerson
import kotlinx.coroutines.flow.Flow

interface ITestRepo {

    /**
     * Each time db is modified, emit items
     */
    fun listen(): Flow<List<EntityOnePerson>>

    /**
     * Just return db
     */
    suspend fun getPeopleFromDb(): List<EntityOnePerson>

    /**
     * call api if DB is empty
     */
    suspend fun getAllPeople(): Either<String?, List<EntityOnePerson>>

    /**
     * Call the webservice
     */
    suspend fun getPeopleFromApi(): Either<String?, List<ApiOnePerson>?>

    /**
     * Reorder the DB
     * @param id : id of the element to move
     * @param newPosition : new position of the item
     */
    suspend fun order(id: Int, newPosition: Int)

    /**
     * Add one person to DB
     */
    suspend fun addOnePerson(onePerson: EntityOnePerson): EntityOnePerson

    /**
     * Remove one person from DB
     */
    suspend fun deleteOnePerson(onePerson: EntityOnePerson)
}