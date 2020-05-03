package com.bravura.bravura.entities;

import com.google.gson.annotations.SerializedName;


public class AudioTrack {

    public String id;

    @SerializedName("tit_art")
    public String title;

    public String duration;

    public String url;

    public String extra;

}
