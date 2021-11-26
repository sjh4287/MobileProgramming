package com.bignerdranch.android.myapplication;

import com.google.gson.annotations.SerializedName;

public class TerminalInfo {
    @SerializedName("Emptycnt")

    int Emptycnt;

    @SerializedName("Parkcnt")

    int Parkcnt;

    @SerializedName("Tmname")

    String Tmname;

    @SerializedName("Rackcnt")

    int Rackcnt;

    @SerializedName("Latitude")

    double Latitude;

    @SerializedName("Longitude")

    double Longitude;

    @SerializedName("Vno")

    int Vno;

    public int getEmptycnt(){
        return Emptycnt;
    }

    public int getParkcnt(){
        return Parkcnt;
    }

    public String getTmname(){
        return Tmname;
    }

    public double getLatitude(){
        return Latitude;
    }

    public double getLongitude(){
        return Longitude;
    }

    public int getRackcnt(){
        return Rackcnt;
    }

    public int getVno(){
        return Vno;
    }


}
