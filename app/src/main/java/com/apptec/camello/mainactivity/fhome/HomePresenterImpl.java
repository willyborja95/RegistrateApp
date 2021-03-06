package com.apptec.camello.mainactivity.fhome;

import androidx.lifecycle.MutableLiveData;

import com.apptec.camello.R;
import com.apptec.camello.mainactivity.BaseProcessListener;
import com.apptec.camello.repository.webservices.ApiClient;
import com.apptec.camello.repository.webservices.GeneralCallback;
import com.apptec.camello.repository.webservices.pojoresponse.GeneralResponse;
import com.apptec.camello.util.Constants;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.Nullable;

import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

/**
 * This class is in the middle of the MVVM pattern.
 * It interacts as an intermediary between the Model and the View.
 */
public class HomePresenterImpl {


    /**
     * Method that call the backend to know the status of thje current user
     *
     * @param listener
     */
    public void getAssistanceStatus(@Nullable BaseProcessListener listener, MutableLiveData<Boolean> isWorking) {
        if (listener != null) {
            listener.onProcessing(null, null);
        }

        AssistanceRetrofitInterface retrofitInterface = ApiClient.getClient().create(AssistanceRetrofitInterface.class);

        Call<GeneralResponse<JsonObject>> call = retrofitInterface.getAssistanceState(ApiClient.getAccessToken());

        call.enqueue(new GeneralCallback<GeneralResponse<JsonObject>>(call) {
            @Override
            public void onFinalResponse(Call<GeneralResponse<JsonObject>> call, Response<GeneralResponse<JsonObject>> response) {
                // Get the assistance status
                if (response.code() == 200) {

                    String state = (String) response.body().getWrappedData().get("state").getAsString();

                    if (state.equals(Constants.WORKING)) {
                        Timber.d("State working");
                        isWorking.postValue(true);

                    } else if (state.equals(Constants.NOT_WORKING)) {
                        Timber.d("State not working");
                        isWorking.postValue(false);

                    }
                    if (listener != null) {
                        listener.onSuccessProcess(R.string.synced, null);
                    }
                } else {
                    onFinalFailure(call, new Throwable("Response not successful"));
                }
            }

            @Override
            public void onFinalFailure(Call<GeneralResponse<JsonObject>> call, Throwable t) {
                if (listener != null) {
                    listener.onErrorOccurred(R.string.title_error_connection, R.string.message_error_connection);
                }
            }
        });


    }
}
