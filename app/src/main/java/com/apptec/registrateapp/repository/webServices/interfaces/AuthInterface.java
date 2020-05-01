package com.apptec.registrateapp.repository.webServices.interfaces;

import com.apptec.registrateapp.repository.workers.RefreshTokenBody;
import com.apptec.registrateapp.util.Constants;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;


public interface AuthInterface {
    /**
     * Auth Interface
     */

    @POST(Constants.REFRESH_TOKEN_URL)
    Call<JsonObject> refreshToken(
            @Body RefreshTokenBody refreshTokenBody
    );

}
