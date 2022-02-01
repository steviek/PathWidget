package com.sixbynine.transit.path

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sixbynine.transit.path.logging.IsLocalLoggingEnabled
import com.sixbynine.transit.path.logging.LocalLogDao
import com.sixbynine.transit.path.logging.LocalLogEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
  private val localLogDao: LocalLogDao
): ViewModel() {

  private val logsLiveData = MutableLiveData<List<LocalLogEntry>>()

  fun getLogs(): LiveData<List<LocalLogEntry>> {
    refreshDisplayedLogs()
    return logsLiveData
  }

  fun refreshDisplayedLogs() {
    if (IsLocalLoggingEnabled) {
      viewModelScope.launch {
        logsLiveData.value = localLogDao.getAll().sortedByDescending { it.timestamp }
      }
    }
  }
}