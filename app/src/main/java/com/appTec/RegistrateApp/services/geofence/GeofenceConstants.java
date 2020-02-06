package com.appTec.RegistrateApp.services.geofence;

import static android.provider.ContactsContract.Directory.PACKAGE_NAME;

public class GeofenceConstants {
    private GeofenceConstants() {}

    public static final String GEOFENCES_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY";

    private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;
    public static final float GEOFENCE_RADIUS_IN_METERS = 100;
}
