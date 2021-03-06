package com.apptec.camello.auth.refreshtoken;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.apptec.camello.repository.sharedpreferences.SharedPreferencesHelper;
import com.apptec.camello.repository.webservices.ApiClient;
import com.apptec.camello.repository.webservices.interfaces.AuthInterface;
import com.apptec.camello.repository.webservices.pojoresponse.GeneralResponse;
import com.apptec.camello.util.Constants;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.JsonObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Worker for refresh token
 */
public class RefreshTokenWorker extends Worker {


    public RefreshTokenWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    /**
     * Initializing credentials manager code goes here
     */
    @NonNull
    @Override
    public Result doWork() {

        Timber.d("Init doWork");
        AuthInterface authInterface = ApiClient.getClient().create(AuthInterface.class);
        RefreshTokenBody body = new RefreshTokenBody(ApiClient.getAccessToken(), ApiClient.getRefreshToken());
        Call<GeneralResponse<JsonObject>> refreshCall = authInterface.refreshToken(
                body
        );

        try {

            Timber.d("Body of the request for request the new token: ");
            Timber.d("Body: %s", body);
            Timber.d("Url: %s", refreshCall.request().url());
            // With do not enqueue the call because it is no necessary an immediate response from this worker
            Response<GeneralResponse<JsonObject>> response = refreshCall.execute();
            // Get the new access token
            String newAccessToken = response.body().getWrappedData().get("accessToken").getAsString();
            SharedPreferencesHelper.putStringValue(Constants.USER_ACCESS_TOKEN, newAccessToken); // Storage in shared preferences

            Timber.i("Token refreshed");
        } catch (IOException e) {
            e.printStackTrace();
            Timber.w("Refresh token failure: %s", e.getMessage());
            return Result.failure();
        } catch (Exception e) {
            Timber.e(e);
            FirebaseCrashlytics.getInstance().recordException(e);
            return Result.failure();
        }


        // Indicate whether the task finished successfully with the Result
        return Result.success();
    }


}