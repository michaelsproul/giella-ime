package so.brendan.hfstospell;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.os.IBinder;
import android.os.Bundle;

import java.io.File;
import java.io.IOException;
import java.lang.Exception;
import java.lang.Override;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import so.brendan.hfstospell.HfstUtils;

// This class handles synchronised speller related tasks on an isolated process.
public class SpellerService extends IntentService {

    private static final String TAG = SpellerService.class.getSimpleName();

    // Actions understood by the speller service.
    public static final String PACKAGE_NAME = "so.brendan.keyboard";
    // Sent to this class to request dictionary installation.
    public static final String ACTION_INSTALL_DICT = PACKAGE_NAME + ".INSTALL_DICT";
    // Sent by this class to signal successful dictionary installation.
    public static final String ACTION_DICT_INSTALLED = PACKAGE_NAME + ".DICT_INSTALLED";
    // Key name for passing the locale in an intent.
    public static final String EXTRA_LOCALE_KEY = PACKAGE_NAME + ".Locale";

    public SpellerService() {
        super(PACKAGE_NAME + "SpellerService");
    }



    // Copy the fallback dictionary and metadata for a given locale to the main dictionary directory.
    // Return the installed dictionary file, or null if no bundled dictionary was available.
    private static void installBundled(@Nonnull String locale) throws Exception {
        // If the file already exists, then it must have been put there by us so there's
        // no need to install a new one.
        File dictPath = HfstUtils.dictionaryFile(locale);
        if (dictPath.exists()) {
            return;
        }

        if (!HfstUtils.isBundled(locale)) {
            throw new Exception("Not a bundled locale");
        }

        HfstUtils.copyAssetToFile(HfstUtils.bundledMetadata(locale), HfstUtils.metadataFile(locale));
        HfstUtils.copyAssetToFile(HfstUtils.bundledDictionary(locale), HfstUtils.dictionaryFile(locale));
    }

    @Override
    public void onHandleIntent(Intent intent) {
        Log.d(TAG, "RECEIVED INTENT");
        switch (intent.getAction()) {
            case ACTION_INSTALL_DICT:
                String locale = intent.getStringExtra(EXTRA_LOCALE_KEY);
                if (locale == null) {
                    Log.e(TAG, "No locale in install dict request");
                    return;
                }
                HfstUtils.init(this);
                try {
                    installBundled(locale);
                } catch (Exception e) {
                    Log.e(TAG, "Unable to install bundled dictionary for locale: " + locale);
                    Log.e(TAG, "Exception: " + e.getMessage());
                    return;
                }
                Log.d(TAG, "Sending DICT_INSTALLED broadcast");
                sendBroadcast(dictInstalledIntent(locale));
            default:
                return;
        }
    }

    // Create an intent to broadcast that the dictionary for a given locale is now installed.
    public static Intent dictInstalledIntent(String locale) {
        return intentWithLocale(ACTION_DICT_INSTALLED, locale);
    }

    // Create an intent to broadcast that a dictionary for the given locale needs to be installed.
    public static Intent installDictIntent(String locale) {
        return intentWithLocale(ACTION_INSTALL_DICT, locale);
    }

    private static Intent intentWithLocale(String action, String locale) {
        Intent intent = new Intent(action);
        intent.putExtra(HfstDictionaryService.EXTRA_LOCALE_KEY, locale);
        return intent;
    }
}