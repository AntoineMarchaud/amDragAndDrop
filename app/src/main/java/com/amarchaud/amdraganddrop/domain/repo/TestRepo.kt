package com.amarchaud.amdraganddrop.domain.repo

import arrow.core.Either
import com.amarchaud.amdraganddrop.data.api.TestApi
import com.amarchaud.amdraganddrop.data.database.PeopleDao
import com.amarchaud.amdraganddrop.domain.ApiOnePerson
import com.amarchaud.amdraganddrop.domain.entity.EntityOnePerson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class TestRepo @Inject constructor(
    private val testDao: PeopleDao,
    private val testApi: TestApi
) : ITestRepo {

    override fun listen() = testDao.getAllPeopleFlow()

    override suspend fun getPeopleFromDb(): List<EntityOnePerson> = testDao.getAllPeople()

    override suspend fun getAllPeople(): Either<String?, List<EntityOnePerson>> {
        if (testDao.getSize() == 0) {
            val res = getPeopleFromApi()
            if (res is Either.Right) {
                res.value?.map { EntityOnePerson(it) }?.let {

                    var pos = 0
                    for (i in it.indices) {
                        it[i].position_db = pos++
                    }

                    testDao.insertAll(it)
                }

                return Either.Right(testDao.getAllPeople())

            } else if (res is Either.Left) {
                return res
            }
        }

        return Either.Right(testDao.getAllPeople())
    }

    override suspend fun order(id: Int, newPosition: Int) {

        val currentList = testDao.getAllPeople()

        val elementToMove = currentList.first { it.id == id }
        val elementToMoveCurrentPos = currentList.indexOf(elementToMove)

        if (newPosition < elementToMoveCurrentPos) {
            testDao.incrementPosition(newPosition, elementToMoveCurrentPos - 1)
        } else {
            testDao.decrementPosition(elementToMoveCurrentPos + 1, newPosition)
        }

        testDao.update(elementToMove.apply {
            position_db = newPosition
        })
    }

    override suspend fun getPeopleFromApi(): Either<String?, List<ApiOnePerson>?> =
        suspendCoroutine { continuation ->

            testApi.getProfiles().enqueue(object : Callback<List<ApiOnePerson>> {
                override fun onResponse(
                    call: Call<List<ApiOnePerson>>,
                    response: Response<List<ApiOnePerson>>
                ) {
                    val responseBody = (response.body() as List<ApiOnePerson>)
                    continuation.resume(Either.Right(responseBody))
                }

                override fun onFailure(call: Call<List<ApiOnePerson>>, t: Throwable) {
                    continuation.resume(Either.Left(t.message))
                }
            })
        }

    override suspend fun addOnePerson(onePerson: EntityOnePerson): EntityOnePerson {
        testDao.insert(onePerson.apply {
            position_db = testDao.getSize()
        })

        return testDao.getAllPeople().last()
    }

    override suspend fun deleteOnePerson(onePerson: EntityOnePerson) {
        val pos = onePerson.position_db
        testDao.delete(onePerson)
        testDao.decrementPosition(pos, testDao.getSize())
    }
}