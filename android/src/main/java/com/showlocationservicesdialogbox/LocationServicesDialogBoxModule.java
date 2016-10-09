package com.showlocationservicesdialogbox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.text.Html;
import com.facebook.react.bridge.*;

public class LocationServicesDialogBoxModule extends ReactContextBaseJavaModule implements ActivityEventListener{
    private Promise promiseCallback;
    private ReadableMap map;
    private Activity currentActivity;

    public LocationServicesDialogBoxModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addActivityEventListener(this);

    }

    @Override
    public String getName() {
        return "LocationServicesDialogBox";
    }

    @ReactMethod
    public void checkLocationServicesIsEnabled(ReadableMap configMap, Promise promise) {
        promiseCallback = promise;
        map = configMap;
        currentActivity = getCurrentActivity();
        checkLocationService(false);
    }

    private void checkLocationService(Boolean activityResult) {
        if (currentActivity != null && promiseCallback != null) {
            LocationManager locationManager = (LocationManager) currentActivity.getSystemService(Context.LOCATION_SERVICE);

            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                if (activityResult || (map == null)) {
                    promiseCallback.reject(new Throwable("disabled"));
                } else {
                    displayPromptForEnablingGPS(currentActivity, map, promiseCallback);
                }
            } else {
                promiseCallback.resolve("enabled");
            }
        }
        else if (promiseCallback != null) {
            promiseCallback.reject(new Throwable("disabled"));
        }
    }

    private static void displayPromptForEnablingGPS(final Activity activity, final ReadableMap configMap, final Promise promise) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final String action = android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;

        builder.setMessage(Html.fromHtml(configMap.getString("message")))
                .setPositiveButton(configMap.getString("ok"),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int id) {
                                activity.startActivityForResult(new Intent(action), 1);
                                dialogInterface.dismiss();
                            }
                        })
                .setNegativeButton(configMap.getString("cancel"),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int id) {
                                promise.reject(new Throwable("disabled"));
                                dialogInterface.cancel();
                            }
                        });
        builder.create().show();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
      checkLocationService(true);
    }

    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
      checkLocationService(true);
    }

    public void onNewIntent(Intent intent) {

    }
}
