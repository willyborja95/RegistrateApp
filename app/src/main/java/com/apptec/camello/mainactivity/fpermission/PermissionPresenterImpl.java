package com.apptec.camello.mainactivity.fpermission;

import androidx.lifecycle.LiveData;

import com.apptec.camello.models.PermissionModel;
import com.apptec.camello.models.PermissionType;
import com.apptec.camello.repository.localdatabase.RoomHelper;
import com.apptec.camello.repository.localdatabase.converter.DateConverter;
import com.apptec.camello.repository.webservices.ApiClient;
import com.apptec.camello.repository.webservices.GeneralCallback;
import com.apptec.camello.repository.webservices.pojoresponse.GeneralResponse;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

public class PermissionPresenterImpl {

    /**
     * Helps the interaction repository - view
     */

    public LiveData<List<PermissionModel>> getLiveDataListPermission() {
        /**
         * It return the live of permission into the database
         */
        return RoomHelper.getAppDatabaseInstance().permissionDao().getLiveDataListPermission();
    }

    public void savePermission(PermissionType selectedItem, Calendar startDate, Calendar endDate, String comment) {
        /**
         * Save the permission requested
         */

        new Thread(() -> {
            /**
             * Call the service
             * on success save the permission into database
             */

            Date startDateDate = startDate.getTime();
            Date endDateDate = endDate.getTime();

            Long startDate1 = DateConverter.toTimestamp(startDateDate);
            Long endDate1 = DateConverter.toTimestamp(endDateDate);

            // Creating the body
            PermissionModel permission = new PermissionModel(comment, selectedItem.getId(), 1, startDate1, endDate1);
            PermissionDto permissionDto = new PermissionDto(permission);

            PermissionRetrofitInterface permissionRetrofitInterface = ApiClient.getClient().create(PermissionRetrofitInterface.class);
            Call<GeneralResponse<PermissionDto>> call = permissionRetrofitInterface.createPermission(
                    ApiClient.getAccessToken(),
                    permissionDto
            );
            Timber.d(permissionDto.toString(), permissionDto);
            call.enqueue(new GeneralCallback<GeneralResponse<PermissionDto>>(call) {

                @Override
                public void onResponse(Call<GeneralResponse<PermissionDto>> call, Response<GeneralResponse<PermissionDto>> response) {
                    // Save the permission also into the database

                    if (response.isSuccessful()) {

                        // Get the permission id form the response and also the status by the way
                        int correctId = response.body().getWrappedData().getId();
                        int correctStatus = response.body().getWrappedData().getStatusId();
                        Timber.d("Permission: " + response.body().getWrappedData().toString());
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

                        Timber.e("Response not successful");
                    }
                }
            });

        }).start();
    }


    public void syncPermissionsWithNetwork() {
        /**
         * Create a new Thread for pull the permissions
         */
        new Thread(new SyncPermissions()).start();

    }
}