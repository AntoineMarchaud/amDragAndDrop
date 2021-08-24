package com.amarchaud.amdraganddrop.screen.new_user

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.amarchaud.amdraganddrop.domain.entity.EntityOnePerson
import com.amarchaud.amdraganddrop.domain.repo.ITestRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class Status {
    data class StatusOk(val onePerson: EntityOnePerson) : Status()
}

@HiltViewModel
class NewUserDialogViewModel @Inject constructor(
    val app: Application,
    private val repo: ITestRepo
) : AndroidViewModel(app) {

    //Save
    var imageUri: Uri? = null

    private var _status = MutableLiveData<Status>()
    val status: LiveData<Status>
        get() = _status

    fun addNewUser(onePerson: EntityOnePerson) {
        viewModelScope.launch {
            _status.postValue(Status.StatusOk(onePerson = repo.addOnePerson(onePerson)))
        }
    }
}