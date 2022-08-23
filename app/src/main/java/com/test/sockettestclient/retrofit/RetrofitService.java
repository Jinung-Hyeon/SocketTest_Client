package com.test.sockettestclient.retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RetrofitService {
    @GET("api/did/broadcast")
    Call<ObjectResult> getPosts(@Query("androidId") String androidId);

    @GET("api/did/broadcast")
    Call<ObjectResult> getPost(@Query("androidId") String androidId,@Query("contentsIdx") int contentsIdx);

    // contentsIdx에 null값을 보내서 contentsIdx 정보를 얻어옴.
    @GET("api/did/broadcast")
    Call<ObjectResult> nullPost(@Query("androidId") String androidId,@Query("contentsIdx") String contentsIdx);
}
