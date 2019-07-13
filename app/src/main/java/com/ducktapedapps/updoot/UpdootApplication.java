package com.ducktapedapps.updoot;

import android.app.Application;
import android.content.SharedPreferences;

import com.ducktapedapps.updoot.di.ApplicationModule;
import com.ducktapedapps.updoot.di.DaggerUpdootComponent;
import com.ducktapedapps.updoot.di.UpdootComponent;
import com.ducktapedapps.updoot.utils.constants;
import com.facebook.stetho.Stetho;

import java.util.UUID;

public class UpdootApplication extends Application {
    private UpdootComponent mUpdootComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        getUpdootComponent().inject(this);
        setUpLoginState();
        createDeviceId();
    }

    private void setUpLoginState() {
        SharedPreferences sharedPreferences = getUpdootComponent().getSharedPreferences();
        String login_state = sharedPreferences.getString(constants.LOGIN_STATE, null);
        if (login_state == null) {
            sharedPreferences
                    .edit()
                    .putString(constants.LOGIN_STATE, constants.LOGGED_OUT_STATE)
                    .apply();
        }
    }

    private void createDeviceId() {
        SharedPreferences sharedPreferences = getUpdootComponent().getSharedPreferences();
        String device_id = sharedPreferences.getString(constants.DEVICE_ID_KEY, null);
        if (device_id == null) {
            device_id = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(constants.DEVICE_ID_KEY, device_id);
            editor.apply();
        }
    }

    public UpdootComponent getUpdootComponent() {
        if (mUpdootComponent == null) {
            mUpdootComponent = DaggerUpdootComponent
                    .builder()
                    .applicationModule(new ApplicationModule(this))
                    .build();
        }
        return mUpdootComponent;
    }
}