package com.example.files.models

import com.google.gson.annotations.SerializedName

data class SelfUpdate(
    @JvmField
    @SerializedName("version_name")
    val versionName: String = "",

    @JvmField
    @SerializedName("version_code")
    val versionCode: Int = 0,

    @JvmField
    @SerializedName("files_build")
    val filesBuild: String = "",

    @JvmField
    @SerializedName("changelog")
    val changelog: String = ""
)
