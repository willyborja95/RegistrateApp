package com.appTec.RegistrateApp.repository.webServices.interfaces;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface PermissionTypeRetrofitInterface {

    @GET("permiso/")
    Call<JsonObject> get(@Header("authorization") String token);
}
