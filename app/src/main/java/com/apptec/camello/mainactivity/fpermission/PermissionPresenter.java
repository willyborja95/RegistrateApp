package com.apptec.camello.mainactivity.fpermission;

import androidx.lifecycle.LiveData;

import com.apptec.camello.R;
import com.apptec.camello.mainactivity.BaseProcessListener;
import com.apptec.camello.models.PermissionModel;
import com.apptec.camello.models.PermissionType;
import com.apptec.camello.repository.localdatabase.RoomHelper;
import com.apptec.camello.repository.localdatabase.converter.DateConverter;
import com.apptec.camello.repository.webservices.ApiClient;
import com.apptec.camello.repository.webservices.GeneralCallback;
import com.apptec.camello.repository.webservices.pojoresponse.GeneralResponse;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Helps the interaction repository - view
 */
public class PermissionPresenter {

    /**
     * It return the live of permission into the database
     */
    public LiveData<List<PermissionModel>> getLiveDataListPermission() {
        return RoomHelper.getAppDatabaseInstance().permissionDao().getLiveDataListPermission();
    }

    /**
     * Save the permission requested
     */
    public void savePermission(PermissionType selectedItem, Calendar startDate, Calendar endDate, @Nullable String comment, @Nullable BaseProcessListener listener) {

        new Thread(() -> {

            if (listener != null) {
                listener.onProcessing(null, null);
            }

            if (isValidPermission(selectedItem, startDate, endDate)) {
                // Call the service on success save the permission into database
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

                                 /**
                                  * Method that will be called after the onResponse default method after doing some validations
                                  * * see {@link GeneralCallback}
                                  * This need to be override by the classes that implement GeneralCallback
                                  *
                                  * @param call     call
                                  * @param response response
                                  */
                                 @Override
                                 public void onFinalResponse(Call<GeneralResponse<PermissionDto>> call, Response<GeneralResponse<PermissionDto>> response) {
                                     // Save the permission also into the database

                                     if (response.isSuccessful()) {

                                         // Get the permission id form the response and also the status by the way
                                         int correctId = response.body().getWrappedData().getId();
                                         int correctStatus = response.body().getWrappedData().getStatusId();
                                         Timber.d("Permission: %s", response.body().getWrappedData().toString());
                                         Timber.i("CorrectId: %s", correctId);
                                         Timber.i("CorrectStatus: %s", correctStatus);

                                         // Update the permission with the id and status before save into database
                                         permission.setId(correctId);
                                         permission.setFkPermissionStatus(correctStatus);

                                         new Thread(new Runnable() {
                                             @Override
                                             public void run() {
                                                 Timber.d("Saving the permission result into database");
                                                 RoomHelper.getAppDatabaseInstance().permissionDao().insertOrReplace(permission);

                                                 if (listener != null) {
                                                     listener.onSuccessProcess(null, null);
                                                 }

                                             }
                                         }).start();
                                     } else {
                                         Timber.e("Response not successful");
                                         if (listener != null) {
                                             listener.onErrorOccurred(R.string.title_error_connection, R.string.message_error_connection);
                                         }

                                     }
                                 }


                                 /**
                                  * Method to be override by the calling class
                                  */
                                 @Override
                                 public void onFinalFailure(Call<GeneralResponse<PermissionDto>> call, Throwable t) {
                                     Timber.e(t, "Error while requesting a new permission");
                                     if (listener != null) {
                                         listener.onErrorOccurred(R.string.request_permission_date_error_title, R.string.request_permission_date_error);
                                     }
                                 }
                             }


                );
            } else {
                if (listener != null) {
                    listener.onErrorOccurred(R.string.request_permission_date_error_title, R.string.request_permission_date_error);
                }
            }


        }).start();
    }

    /**
     * Method that validates the permission entered
     *
     * @param selectedItem The permission type
     * @param startDate    the target start date
     * @param endDate      the target end date
     * @return true when the start date is before end date
     */
    private boolean isValidPermission(PermissionType selectedItem, Calendar startDate, Calendar endDate) {
        Timber.d("Validating data");
        if (startDate == null || endDate == null || selectedItem == null) {
            return false;
        }
        if (startDate.after(System.currentTimeMillis() - 1000)) {
            return false;
        }

        return startDate.before(endDate);

    }

    /**
     * Create a new Thread for pull the permissions
     */
    public void syncPermissionsWithNetwork(@Nullable BaseProcessListener listener) {
        new Thread(new SyncPermissions(listener)).start();
    }

    /**
     * Method that call the server to delete a permission
     *
     * @param permission the target permission
     * @param listener   listener of the process
     */
    public void deletePermission(@NotNull PermissionModel permission, @Nullable BaseProcessListener listener) {

        if (listener != null) {
            listener.onProcessing(null, null);
        }

        PermissionRetrofitInterface permissionRetrofitInterface = ApiClient.getClient().create(PermissionRetrofitInterface.class);
        Call<GeneralResponse> call = permissionRetrofitInterface.deletePermission(
                ApiClient.getAccessToken(),
                permission.getId()
        );
        Timber.d(call.request().toString());
        call.enqueue(new GeneralCallback<GeneralResponse>(call) {
            @Override
            public void onFinalResponse(Call<GeneralResponse> call, Response<GeneralResponse> response) {

                Timber.d(response.toString());

                if (response.code() == 404 || response.code() == 200) {
                    // Permission already deleted on the server
                    // Delete it from database
                    Timber.d("Permission deleted successful");
                    deletePermissionFromDatabase(permission);
                    if (listener != null) {
                        Timber.d("Notify the listener");
                        listener.onSuccessProcess(null, null);
                    }

                } else {
                    // An error occurred
                    if (listener != null) {
                        listener.onErrorOccurred(R.string.error, R.string.unknown_error);
                    }

                }


            }

            @Override
            public void onFinalFailure(Call<GeneralResponse> call, Throwable t) {
                Timber.e(t);
                if (listener != null) {
                    listener.onErrorOccurred(R.string.no_internet_connection_title, R.string.no_internet_connection);
                }
            }
        });

    }


    private void deletePermissionFromDatabase(PermissionModel permission) {
        try {
            new Thread(() -> {
                Timber.d("Deleting permission from database");
                RoomHelper.getAppDatabaseInstance().permissionDao().delete(permission);
            }).start();
        } catch (Exception e) {
            Timber.w("Permission already deleted from database");
        }
    }

}




