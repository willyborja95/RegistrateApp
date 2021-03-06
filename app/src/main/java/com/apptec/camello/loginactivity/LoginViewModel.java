package com.apptec.camello.loginactivity;

import android.app.Application;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.apptec.camello.R;
import com.apptec.camello.util.Event;
import com.apptec.camello.util.Process;

import timber.log.Timber;

/**
 * View model for the login activity
 */
public class LoginViewModel extends AndroidViewModel {

    // This variable help to show error is the form in the ui is wrong
    public MutableLiveData<LoginFormState> loginFormState;

    // To show the progress of the login progress to the view
    private MutableLiveData<Event<Process>> loginProgress;

    // Read IMEI permission
    public MutableLiveData<Boolean> permissionGranted = new MutableLiveData<>(false);

    // Request permission fro read IMEI
    private MutableLiveData<Boolean> shouldRequestPermission = new MutableLiveData<>(false);

    // Presenter that do hard work
    private LoginPresenter loginPresenter;

    // To know which activity we have to navigate to
    private final MutableLiveData<Event<Integer>> newDestination = new MutableLiveData<>();

    // Variable to know if we should navigate to next fragment
    private final MutableLiveData<Event<Boolean>> shouldNavigateToMainActivity = new MutableLiveData<>();

    /**
     * Constructor
     */
    public LoginViewModel(@NonNull Application application) {

        super(application);

        loginFormState = new MutableLiveData<>();
        loginProgress = new MutableLiveData<>(new Event<>(new Process(Process.NOT_INIT, null, null))); // Because we don't know yet the result

        loginPresenter = new LoginPresenter();
        Timber.d("Login view model view model built");

        // Verify if is there a previous user session active
        verifyPreviousLogin();

    }

    /**
     * Verify if is there a previous user session active
     * <p>
     * Change the login result if yes and the login activity will know that she should navigate
     * to the main activity
     */
    private void verifyPreviousLogin() {
        loginPresenter.verifyPreviousLogin(loginProgress);
    }

    /**
     * Expose the data
     */
    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<Event<Process>> getLoginProgress() {
        return loginProgress;
    }

    /**
     * Called when the login button is clicked
     * - Check if the permission for read IMEI is granted
     * - Validates username and password
     */
    public void loginClicked(String email, String password) {
        Timber.d("Login clicked");
        // Validates data
        if (!isUserNameValid(email) || !isPasswordValid(password)) {
            Timber.d("Invalid form");
            loginFormState.setValue(new LoginFormState(R.string.invalid_email, R.string.invalid_password));
        } else {
            Timber.d("Valid form");

            if (this.permissionGranted.getValue()) {
                // This event will be listen by the activity that will show the progress bar
                loginFormState.setValue(new LoginFormState(true));


                // Call the presenter to verify the credentials and the log the user
                // Send the result so the activity can observe automatically the result
                loginPresenter.handleLogin(loginProgress, email, password);
            } else {
                // Say the activity to ask the permission
                shouldRequestPermission.setValue(true);
            }
        }
    }

    /**
     * Method called by the {@link com.apptec.camello.loginactivity.forgotpassword.ForgotPasswordDialog}
     * to call the server on the corresponding url
     *
     * @param targetEmail the user email
     */
    public void recoverPassword(String targetEmail) {
        // Using the same loginProgress as the listener because I don't think is necessary create a new
        // observer on the formFragment
        loginPresenter.callRecoverPassword(targetEmail, loginProgress);
    }

    /**
     * Expose the destination
     *
     * @return newDestination
     */
    public LiveData<Event<Integer>> getNewDestination() {
        return newDestination;
    }

    /**
     * Method called inside the fragments and will trigger a navigation to the new destination
     *
     * @param destinationId
     */
    public void setNewDestination(int destinationId) {
        newDestination.setValue(new Event<>(destinationId));
    }


    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 3;
    }


    // To notify the activity if we should request the permission
    public MutableLiveData<Boolean> getShouldRequestPermission() {
        return shouldRequestPermission;
    }

    public void navigateToMainActivity() {
        this.shouldNavigateToMainActivity.setValue(new Event<>(true));
    }

    public MutableLiveData<Event<Boolean>> getShouldNavigateToMainActivity() {
        return shouldNavigateToMainActivity;
    }
}
