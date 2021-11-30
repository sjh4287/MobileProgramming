package com.bignerdranch.android.myapplication;

import retrofit2.Call;
import retrofit2.http.GET;

public interface TermInterface {
    @GET("http://api.nubija.com:1577/ubike/nubijaInfoApi.do?apikey=kTKnDZYpryizkfmPsCyu")
    Call<Terminal> getPosts();
}

