package com.example.files.utils

import com.example.files.models.JFile

class Filters {
    var textFilter: String = ""
        get() = field.lowercase()
        private set

    var dateFilter: Long = 0
        private set

    var typeFilter: JFile.Type? = null
        private set

    init {
        initFilters()
    }

    private fun initFilters() {
        this.textFilter = ""
        this.dateFilter = 0
        this.typeFilter = null
    }

    fun setTextFilter(text: String): Filters {
        this.textFilter = text.trim()
        return this
    }

    fun setDateFilter(date: Long): Filters {
        this.dateFilter = date
        return this
    }

    fun setTypeFilter(type: JFile.Type?): Filters {
        this.typeFilter = type
        return this
    }

    fun clearFilters() {
        initFilters()
    }

    fun clearDateFilter() {
        this.dateFilter = 0
    }

    fun clearTypeFilter() {
        this.typeFilter = null
    }
}
