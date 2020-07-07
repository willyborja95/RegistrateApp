package com.apptec.camello.mainactivity.fhome.geofence;

import com.apptec.camello.App;
import com.apptec.camello.R;
import com.apptec.camello.mainactivity.BaseProcessListener;
import com.apptec.camello.mainactivity.fhome.AssistanceRetrofitInterface;
import com.apptec.camello.models.WorkZoneModel;
import com.apptec.camello.models.WorkingPeriodModel;
import com.apptec.camello.repository.localdatabase.RoomHelper;
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
 * Class runnable that will start all thin g to set the user working
 */
public class StartWorking<T extends BaseProcessListener> implements Runnable {

    // Attributes
    private WorkZoneModel workZoneModel;
    private boolean isFirstTime;


    // Listener
    private T listener;


    /**
     * Ideal constructor
     *
     * @param workZoneModel work zone when the user will going to work
     * @param isFirstTime   boolean that indicates if we have to create a new row in database or use an
     *                      exiting one. If is the first time we have to create a new row.
     * @param listener      listener of the process
     */
    public StartWorking(WorkZoneModel workZoneModel, boolean isFirstTime, T listener) {
        this.workZoneModel = workZoneModel;
        this.isFirstTime = isFirstTime;
        this.listener = listener;
    }


    @Override
    public void run() {

        // Indicates the listener that we take care of the process now
        listener.onProcessing();

        Timber.d("Handling the request to start working");

        if (this.isFirstTime) {
            // If there is not a previous working period into the database
            Timber.d("Not previous working period created, maybe the app is running by first time");

            Timber.d("Created a new working period");
            // Create a working period instance
            WorkingPeriodModel startedWorkingPeriod = new WorkingPeriodModel(
                    System.currentTimeMillis(),   // Current time
                    Constants.INT_WORKING_STATUS, // Not init status
                    workZoneModel.getId());       // Foreign key to the work zone

            Timber.d("Save the working period into database");
            // Save the working period into database
            RoomHelper.getAppDatabaseInstance().workingPeriodDao().insert(startedWorkingPeriod);

        } else {
            // Change the status of the last working period to "working"


            Timber.d("Change the status of the last working period to \"working\"");
            // Get the last working period
            WorkingPeriodModel lastWorkingPeriod = RoomHelper.getAppDatabaseInstance().workingPeriodDao().getLastWorkingPeriod();

            // Update it
            lastWorkingPeriod.setStatus(Constants.INT_WORKING_STATUS);
            lastWorkingPeriod.setStart_date(System.currentTimeMillis());
            lastWorkingPeriod.setWorkZoneId(workZoneModel.getId());

            // Save the changes into database
            RoomHelper.getAppDatabaseInstance().workingPeriodDao().updateWorkingPeriod(lastWorkingPeriod);


        }

        // Start tracking the work zone
        App.getGeofenceHelper().startTracking(workZoneModel);

        // Notify the server
        notifyTheServer(workZoneModel);
    }


    /**
     * Notify the server
     *
     * @param workZoneModel When the param is -1 means an exit
     */
    private void notifyTheServer(@Nullable WorkZoneModel workZoneModel) {
        Timber.d("Notify the server the user has change his working status");
        AssistanceRetrofitInterface retrofitInterface = ApiClient.getClient().create(AssistanceRetrofitInterface.class);
        Call<GeneralResponse<JsonObject>> call;

        call = getCall(retrofitInterface, workZoneModel);


        call.enqueue(new GeneralCallback<GeneralResponse<JsonObject>>(call) {
            @Override
            public void onResponse(Call<GeneralResponse<JsonObject>> call, Response<GeneralResponse<JsonObject>> response) {
                Timber.i("Assistance changed");
                Timber.i("Request code: " + response.code());

                // Notify the listener
                listener.onSuccessProcess();
            }

            /**
             * Let know the listener that an error happen
             */
            @Override
            public void onFinalFailure(Call<GeneralResponse<JsonObject>> call, Throwable t) {
                listener.onErrorOccurred(R.string.no_internet_connection_title, R.string.no_internet_connection);
            }
        });

    }

    private Call<GeneralResponse<JsonObject>> getCall(AssistanceRetrofitInterface retrofitInterface, WorkZoneModel workZoneModel) {
        Call<GeneralResponse<JsonObject>> call = retrofitInterface.registerAssistance(
                ApiClient.getAccessToken(),
                workZoneModel
        );
        return call;
    }

}