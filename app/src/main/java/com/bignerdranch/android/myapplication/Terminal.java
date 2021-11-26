package com.bignerdranch.android.myapplication;


import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

//http://api.nubija.com:1577/ ubike/nubijaInfoApi.do?apikey=kTKnDZYpryizkfmPsCyu
//레트로핏 인터페이스

public interface Terminal {
    @GET("ubike/nubijaInfoApi.do?apikey=kTKnDZYpryizkfmPsCyu")
    Call<TerminalInfo> getPosts(@Path("post") String post);

}
