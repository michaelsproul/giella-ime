package so.brendan.hfstospell;

import java.lang.Override;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.os.IBinder;
import android.os.Bundle;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import so.brendan.hfstospell.HfstUtils;

class HfstDictionaryService extends IntentService {
    private static final String TAG = HfstDictionaryService.class.getSimpleName();
    private Context mCtx;

    // Intent actions that we understand.
    public static final String PACKAGE_NAME = "so.brendan.hfstospell";
    public static final String ACTION_UPDATE_DICT = PACKAGE_NAME + ".UPDATE_DICT";

    // Key name for passing the locale in an intent.
    public static final String EXTRA_LOCALE_KEY = PACKAGE_NAME + ".Locale";

    public HfstDictionaryService() {
        super("HFST Dictionary Service");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mCtx = this;
    }

    @Override
    protected void onHandleIntent (Intent intent) {
        Log.d(TAG, "SPROUL: received an intent");
        switch (intent.getAction()) {
            case ACTION_UPDATE_DICT:
                handleUpdateDict(intent);
            default:
                return;
        }
    }

    private void handleUpdateDict(Intent intent) {
        Bundle intentExtras = intent.getExtras();
        String locale = intentExtras == null ? null : intentExtras.getString(EXTRA_LOCALE_KEY);
        if (locale == null) {
            Log.e(TAG, "No locale in dict update request");
        }

        Log.d(TAG, "Hello! Locale is: " + locale);
    }
}