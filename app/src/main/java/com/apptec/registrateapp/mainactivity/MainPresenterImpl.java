package com.apptec.registrateapp.mainactivity;

import androidx.lifecycle.MutableLiveData;

public class MainPresenterImpl {
    /**
     * This class will help the main activity
     */

    MainInteractorImpl mainInteractor;

    public MainPresenterImpl() {
        /**
         * Empty constructor
         */
        mainInteractor = new MainInteractorImpl();
    }


    public void initializeDeviceVerification(MutableLiveData<Boolean> isNeedRegisterDevice) {
        /**
         * Calling the interactor
         */
        mainInteractor.initializeDeviceVerification(isNeedRegisterDevice);
    }

    public void saveThisDevice(String name, String model, MutableLiveData<Boolean> isNeedRegisterDevice) {
        /**
         * Calling the interactor
         */
        mainInteractor.saveThisDevice(name, model, isNeedRegisterDevice);
    }

}