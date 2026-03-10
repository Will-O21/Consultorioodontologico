package com.wdog.consultorioodontologico.sync

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object SyncState {
    private val _estaSincronizado = MutableStateFlow(true)
    val estaSincronizado = _estaSincronizado.asStateFlow()

    fun setSincronizado(valor: Boolean) {
        _estaSincronizado.value = valor
    }
}