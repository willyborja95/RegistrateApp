package com.apptec.registrateapp.mainactivity.fpermission;

import androidx.lifecycle.LiveData;

import com.apptec.registrateapp.models.PermissionModel;
import com.apptec.registrateapp.models.PermissionType;
import com.apptec.registrateapp.repository.localdatabase.RoomHelper;
import com.apptec.registrateapp.repository.localdatabase.converter.DateConverter;
import com.apptec.registrateapp.repository.sharedpreferences.SharedPreferencesHelper;
import com.apptec.registrateapp.repository.webservices.ApiClient;
import com.apptec.registrateapp.util.Constants;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class PermissionPresenterImpl {

    /**
     * Helps the interaction repository - view
     */
    private static final String TAG = "PermissionPresenterImpl";

    public LiveData<List<PermissionModel>> getLiveDataListPermission() {
        /**
         * It return the live of permission into the database
         */
        return RoomHelper.getAppDatabaseInstance().permissionDao().getLiveDataListPermission();
    }

    public void savePermission(PermissionType selectedItem, Calendar startDate, Calendar endDate) {
        /**
         * Save the permission requested
         */

        new Thread(new Runnable() {
            @Override
            public void run() {
                /**
                 * Call the service
                 * on success save the permission into database
                 */

                Date startDateDate = startDate.getTime();
                Date endDateDate = endDate.getTime();

                Long startDate = DateConverter.toTimestamp(startDateDate);
                Long endDate = DateConverter.toTimestamp(endDateDate);

                // Creating the body
                PermissionModel permission = new PermissionModel("", selectedItem.getId(), 1, startDate, endDate);
                PermissionDto permissionDto = new PermissionDto(permission);

                PermissionRetrofitInterface permissionRetrofitInterface = ApiClient.getClient().create(PermissionRetrofitInterface.class);
                Call<JsonObject> call = permissionRetrofitInterface.createPermission(
                        ApiClient.getAccessToken(),
                        permissionDto
                );
                Timber.d(permissionDto.toString(), permissionDto);
                call.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                        // Save the permission also into the database

                        if (response.isSuccessful()) {

                            // Get the permission id form the response and also the status by the way
                            int correctId = Integer.parseInt(response.body().get("data").getAsJsonObject().get("id").toString());
                            int correctStatus = Integer.parseInt(response.body().get("data").getAsJsonObject().get("status").toString());
                            Timber.i("CorrectId: " + correctId);
                            Timber.i("CorrectStatus: " + correctStatus);

                            // Update the permission with the id and status before save into database
                            permission.setId(correctId);
                            permission.setFkPermissionStatus(correctStatus);

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Timber.d("Saving the permission result into database");
                                    RoomHelper.getAppDatabaseInstance().permissionDao().insertOrReplace(permission);
                                }
                            }).start();
                        } else {
                            // TODO:
                            Timber.e("Response not successful");
                        }

                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        // TODO: Handle this
                        Timber.e(t, "onFailure: %s", t.getMessage());

                    }
                });

            }
        }).start();
    }


    public void pullPermissionCatalog() {
        /**
         * This method will pull the permission types available and the permission status from the network
         */
        Timber.d("Starting to pull the permission catalog from network");

        // Pull the permission types
        PermissionRetrofitInterface permissionRetrofitInterface = ApiClient.getClient().create(PermissionRetrofitInterface.class);
        Call<JsonObject> call = permissionRetrofitInterface.getPermissionTypes(
                ApiClient.getAccessToken() // Token
        );

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Timber.d("Response: " + response.body());

                // Save the permissions
                if (response.isSuccessful()) {

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Timber.d("Permission types: " + response.body().get("data"));

                            List<PermissionType> permissionTypesList = new ArrayList<>();

                            for (int i = 0; i < response.body().get("data").getAsJsonArray().size(); i++) {
                                // TODO: complete
                                // permissionTypesList.add(response.body().get("data").getAsJsonArray().get(i));
                            }
                        }
                    }).start();

                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });

    }


    public void syncPermissionsWithNetwork() {
        /**
         * This method will bring the permission of this user and save into the database
         */
        Timber.i("Starting to sync the permissions list from network");

        PermissionRetrofitInterface permissionRetrofitInterface = ApiClient.getClient().create(PermissionRetrofitInterface.class);

        Call<List<PermissionDto>> call = permissionRetrofitInterface.getAllPermissions(
                ApiClient.getAccessToken(), // Header
                SharedPreferencesHelper.getSharedPreferencesInstance().getInt(Constants.CURRENT_USER_ID, 0) // + Path
        );


        call.enqueue(new Callback<List<PermissionDto>>() {
            @Override
            public void onResponse(Call<List<PermissionDto>> call, Response<List<PermissionDto>> response) {

                Timber.d(response.body().toString());

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < response.body().size(); i++) {
                            // Save the list of permission into data
                            RoomHelper.getAppDatabaseInstance().permissionDao().insertOrReplace(response.body().get(i).getAsPermissionModel());
                        }
                    }
                }).start();


            }

            @Override
            public void onFailure(Call<List<PermissionDto>> call, Throwable t) {

            }
        });


    }
}
