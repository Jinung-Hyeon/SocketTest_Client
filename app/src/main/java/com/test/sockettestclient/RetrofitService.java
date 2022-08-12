package com.test.sockettestclient;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RetrofitService {
    @GET("api/did/broadcast")
    Call<ObjectResult> getPosts(@Query("androidId") String androidId);
}
