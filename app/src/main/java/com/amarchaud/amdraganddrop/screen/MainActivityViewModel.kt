package com.amarchaud.amdraganddrop.screen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.amarchaud.amdraganddrop.domain.entity.EntityOnePerson
import com.amarchaud.amdraganddrop.domain.repo.ITestRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class LoadingStatus {
    object StateLoadingStart : LoadingStatus()
    object StateLoadingEnd : LoadingStatus()
}

sealed class Status {
    data class StatusOk(val profiles: List<EntityOnePerson>?) : Status()
    data class StatusError(val message: String?) : Status()
}

@HiltViewModel
class MainActivityViewModel @Inject constructor(val app: Application, private val repo: ITestRepo) :
    AndroidViewModel(app) {

    private var _loadingStatus = MutableLiveData<LoadingStatus>()
    val loadingStatus: LiveData<LoadingStatus>
        get() = _loadingStatus

    private var _people = MutableLiveData<Status>()
    val People: LiveData<Status>
        get() = _people

    /**
     * Each time there is a modification in DB, the collect will be called
     */
    /*
    init {
        viewModelScope.launch {
            repo.listen().distinctUntilChanged().collect {
                _loadingStatus.postValue(LoadingStatus.StateLoadingStart)
                _apiPeople.postValue(Status.StatusOk(profiles = it))
            }
        }
    }*/

    fun fetchPeople() {
        viewModelScope.launch {
            _loadingStatus.postValue(LoadingStatus.StateLoadingStart)
            val res = repo.getAllPeople()
            if (res is Either.Right) {
                _people.postValue(Status.StatusOk(res.value))
            } else if (res is Either.Left) {
                _people.postValue(Status.StatusError(res.value))
            }
            _loadingStatus.postValue(LoadingStatus.StateLoadingEnd)
        }
    }


    fun order(id: Int, positionInserted: Int) {
        viewModelScope.launch {
            repo.order(id, positionInserted)
        }
    }

    fun deletePerson(person: EntityOnePerson) {
        viewModelScope.launch {
            repo.deleteOnePerson(person)
        }
    }
}