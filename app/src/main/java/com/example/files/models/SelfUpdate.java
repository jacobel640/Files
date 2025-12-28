package com.example.files.models;

import com.google.gson.annotations.SerializedName;

public class SelfUpdate {

    @SerializedName("version_name")
    public String versionName = "";

    @SerializedName("version_code")
    public int versionCode = 0;

    @SerializedName("files_build")
    public String filesBuild = "";

    @SerializedName("changelog")
    public String changelog = "";

}
