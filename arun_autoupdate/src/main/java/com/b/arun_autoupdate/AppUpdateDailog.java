package com.b.arun_autoupdate;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.HashMap;

public class AppUpdateDailog {
    private static FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
    private static HashMap<String, Object> firebaseDefaultMap;
    public static final String VERSION_CODE_KEY = "latest_app_version";
    private static String TAG="AppUpdateDailog.class";


    public static void initFCMConfigForUpdate(final Context context,final String appPackageName, final String title, final String message, final String button_text){
        try{
            firebaseDefaultMap = new HashMap<>();
            firebaseDefaultMap.put(VERSION_CODE_KEY, getCurrentVersionCode(context));
            mFirebaseRemoteConfig.setDefaults(firebaseDefaultMap);

            //Setting that default Map to Firebase Remote Config

            //Setting Developer Mode enabled to fast retrieve the values
            mFirebaseRemoteConfig.setConfigSettings(
                    new FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(BuildConfig.DEBUG)
                            .build());

            //Fetching the values here
            mFirebaseRemoteConfig.fetch().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        mFirebaseRemoteConfig.activateFetched();
                        Log.d(TAG, "Fetched value: " + mFirebaseRemoteConfig.getString(VERSION_CODE_KEY));
                        //calling function to check if new version is available or not
                        checkForUpdate(context, appPackageName, title, message, button_text);
                    } else {
                        //Toast.makeText(MainActivity.this, "Something went wrong please try again", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            Log.d(TAG, "Default value: " + mFirebaseRemoteConfig.getString(VERSION_CODE_KEY));
        }catch (Exception ex){

        }
    }

    // check for update
    private static void checkForUpdate(final Context context, final String appPackageName, final String title, final  String msg, final String okay) {
        try{
            int latestAppVersion = (int) mFirebaseRemoteConfig.getDouble(VERSION_CODE_KEY);
            if (latestAppVersion > getCurrentVersionCode(context)) {
                new AlertDialog.Builder(context).setTitle(title)
                        .setMessage(msg).setPositiveButton(okay, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                }
                            }
                        }).setCancelable(false).show();
            } else {
                //Toast.makeText(this,"This app is already up to date", Toast.LENGTH_SHORT).show();
            }
        }catch (Exception ex){}

    }

    private static int getCurrentVersionCode(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
