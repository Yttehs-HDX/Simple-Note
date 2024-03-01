package org.eviyttehsarch.note

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.eviyttehsarch.note.core.SimpleNoteApplication

class SettingsViewModel : ViewModel() {
    private val sharedPreferences = SimpleNoteApplication.Context.getSharedPreferences("settings_shared_pref", Context.MODE_PRIVATE)

    private val _style = MutableStateFlow(SettingsItem.Style.defaultValue)
    val style: StateFlow<StyleValue>
        get() = _style

    private val _location = MutableStateFlow(SettingsItem.Location.defaultValue)
    val location: StateFlow<LocationValue>
        get() = _location

    private val _dateFormat = MutableStateFlow(SettingsItem.DateFormat.defaultValue)
    val dateFormat: StateFlow<DateFormatValue>
        get() = _dateFormat

    init {
        loadStyleData(SettingsItem.Style.defaultValue)
        loadDateFormatData(SettingsItem.DateFormat.defaultValue)
        loadLocationData(SettingsItem.Location.defaultValue)
    }

    private fun loadStyleData(defaultValue: StyleValue) {
        val key = SettingsItem.Style.key
        viewModelScope.launch {
            val stringValue = sharedPreferences.getString(key, null)
            _style.value = stringValue?.toStyleOrDefault() ?: defaultValue
        }
    }

    fun saveStyleData(value: StyleValue) {
        val key = SettingsItem.Style.key
        _style.value = value
        saveData(key, value.toString())
    }

    private fun loadDateFormatData(defaultValue: DateFormatValue) {
        val key = SettingsItem.DateFormat.key
        viewModelScope.launch {
            val stringValue = sharedPreferences.getString(key, null)
            _dateFormat.value = stringValue?.toDateFormatOrDefault() ?: defaultValue
        }
    }

    fun saveDateFormatData(value: DateFormatValue) {
        val key = SettingsItem.DateFormat.key
        _dateFormat.value = value
        saveData(key, value.toString())
    }

    private fun loadLocationData(defaultValue: LocationValue) {
        val key = SettingsItem.Location.key
        viewModelScope.launch {
            val stringValue = sharedPreferences.getString(key, null)
            _location.value = stringValue?.toLocationOrDefault() ?: defaultValue
        }
    }

    fun saveLocationData(value: LocationValue) {
        val key = SettingsItem.Location.key
        _location.value = value
        saveData(key, value.toString())
    }

    private fun saveData(key: String, value: String) {
        viewModelScope.launch {
            sharedPreferences.edit {
                putString(key, value)
            }
        }
    }

    fun resetSettings() {
        saveStyleData(SettingsItem.Style.defaultValue)
        saveDateFormatData(SettingsItem.DateFormat.defaultValue)
        saveLocationData(SettingsItem.Location.defaultValue)
    }
}