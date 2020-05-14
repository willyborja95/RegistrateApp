package com.apptec.registrateapp.mainactivity.fnotification.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.apptec.registrateapp.App;
import com.apptec.registrateapp.R;
import com.apptec.registrateapp.mainactivity.MainViewModel;
import com.apptec.registrateapp.models.NotificationModel;

import java.util.List;

public class NotificationFragment extends Fragment {
    /**
     * NotificationsFragment
     */

    private final String TAG = NotificationFragment.class.getSimpleName();

    //UI elements
    private ListView notificationsListView;
    private NotificationListAdapter notificationListAdapter;
    private TextView notificationTextView;
    ProgressDialog progressDialog;

    // Instance of ViewModel
    private MainViewModel mainViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        /**
         * Link the view model
         */
        super.onCreate(savedInstanceState);
        mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);                    // Getting the view model
        mainViewModel.setActiveFragmentName(getString(R.string.notifications_fragment_title));
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        /**
         * Inflate the view.
         * Observe the notifications Live Data that is got from the Room database
         * Create an onItemClickListener to show a dialog about each notification when is pressed.
         */

        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        notificationsListView = view.findViewById(R.id.notification_list_view);

        // Observing the view model mNotification
        notificationListAdapter = new NotificationListAdapter(App.getContext(), mainViewModel.getNotifications());
        mainViewModel.getNotifications().observe(getActivity(), new Observer<List<NotificationModel>>() {
            @Override
            public void onChanged(List<NotificationModel> notifications) {
                notificationsListView.setAdapter(notificationListAdapter);
            }
        });

        // When clicked show a dialog with more information
        notificationsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /**
                 * Show a dialog with extended information about the dialog
                 */
                Log.d(TAG, "Item clicked");
                NotificationModel notification = notificationListAdapter.getItem(position);
                DialogNotification dialogNotification = new DialogNotification().setNotification(notification);
                dialogNotification.show(getFragmentManager(), DialogNotification.class.getSimpleName());
            }
        });

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /**
         * This method could be use for show a message that there are not new notifications.
         * Or maybe the on change Live Data into the observer.
         */

        // Linking UI elements
        progressDialog = new ProgressDialog(getContext());
        notificationsListView = view.findViewById(R.id.notification_list_view);
        // notificationTextView = view.findViewById(R.id.notification_text_view);

        // showNotNewNotificationsMessage();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

    }


    // Dialogs
    // These methods are not used. Probably in the future we will remove it
    public void showAssistanceProgressDialog(String message) {
        /**
         * This is not called now.
         */
        progressDialog.setMessage(message);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);
    }


    public void hideAssistanceProgressDialog() {
        progressDialog.dismiss();
    }


    public void showConnectionErrorMessage() {
        showDialog(getContext().getString(R.string.title_error_connection), getContext().getString(R.string.message_error_connection));
    }


    public void showDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
        alertDialog.show();
    }


    public void showNotNewNotificationsMessage() {
        if (notificationTextView != null) {
            notificationTextView.setVisibility(View.VISIBLE);
        } else {
            Log.d(TAG, "notificationTextView is null");
        }
        if (notificationsListView != null) {
            notificationsListView.setVisibility(View.GONE);
        } else {
            Log.d(TAG, "notificationListView is null");
        }

    }


}