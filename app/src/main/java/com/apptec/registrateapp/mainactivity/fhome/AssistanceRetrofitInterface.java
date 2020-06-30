package com.apptec.registrateapp.mainactivity.fhome;

import com.apptec.registrateapp.models.WorkZoneModel;
import com.apptec.registrateapp.repository.webservices.pojoresponse.GeneralResponse;
import com.apptec.registrateapp.util.Constants;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Interface with endpoint related to register the assistance
 */
public interface AssistanceRetrofitInterface {


    /**
     * Endpoint used to register an enter
     *
     * @param token         access token
     * @param workZoneModel current work zone id
     * @return Call<GeneralResponse < JsonObject>>
     */
    @POST(Constants.REGISTER_ASSISTANCE_URL)
    Call<GeneralResponse<JsonObject>> registerAssistance(
            @Header(Constants.AUTHORIZATION_HEADER) String token,
            @Body WorkZoneModel workZoneModel
    );

    /**
     * Enpoint used to register an exit without the work zone id
     *
     * @param token access token
     * @return Call<GeneralResponse < JsonObject>>
     */
    @POST(Constants.REGISTER_ASSISTANCE_URL)
    Call<GeneralResponse<JsonObject>> registerAssistance(
            @Header(Constants.AUTHORIZATION_HEADER) String token
    );


}