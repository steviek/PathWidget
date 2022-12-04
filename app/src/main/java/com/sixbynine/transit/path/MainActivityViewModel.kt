package com.sixbynine.transit.path

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sixbynine.transit.path.logging.LocalLogDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(localLogDao: LocalLogDao) : ViewModel() {

    val logs = localLogDao.flowAll().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
}
