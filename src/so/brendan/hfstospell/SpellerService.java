package so.brendan.hfstospell;

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
class SpellerService extends BroadcastReceiver {

    private static final String TAG = SpellerService.class.getSimpleName();

    // Actions understood by the speller service.
    public static final String PACKAGE_NAME = "so.brendan.keyboard";
    // Sent to this class to request dictionary installation.
    public static final String ACTION_INSTALL_DICT = PACKAGE_NAME + ".INSTALL_DICT";
    // Sent by this class to signal successful dictionary installation.
    public static final String ACTION_DICT_INSTALLED = PACKAGE_NAME + ".DICT_INSTALLED";
    // Key name for passing the locale in an intent.
    public static final String EXTRA_LOCALE_KEY = PACKAGE_NAME + ".Locale";

    public SpellerService() {}

    @Nullable
    private static String extractLocale(Intent intent) {
        Bundle intentExtras = intent.getExtras();
        if (intentExtras == null) {
            return null;
        }
        return intentExtras.getString(EXTRA_LOCALE_KEY);
    }

    // Copy the fallback dictionary and metadata for a given locale to the main dictionary directory.
    // Return the installed dictionary file, or null if no bundled dictionary was available.
    private static void installBundled(@NonNull String locale) throws Exception {
        // If the file already exists, then it must have been put there by us so there's
        // no need to install a new one.
        File dictPath = HfstUtils.dictionaryFile(locale);
        if (dictPath.exists()) {
            return;
        }

        if (!HfstUtils.isBundled(locale)) {
            throw Exception("Not a bundled locale");
        }

        HfstUtils.copyAssetToFile(HfstUtils.bundledMetadata(locale), HfstUtils.metadataFile(locale));
        HfstUtils.copyAssetToFile(HfstUtils.bundledDictionary(locale), HfstUtils.dictionaryFile(locale));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case ACTION_INSTALL_DICT:
                String locale = extractLocale(intent);
                if (locale == null) {
                    Log.e(TAG, "No locale in install dict request");
                    return;
                }
                HfstUtils.init(context);
                try {
                    installBundled(locale);
                } catch (Exception e) {
                    Log.e("Unable to install bundled dictionary for locale: " + locale);
                    Log.e("Exception: " + e.getMessage());
                    return;
                }
                context.sendBroadcast(dictInstalledIntent(locale));
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

    @Override
    public IBinder peekService(Context myContext, Intent service) {
        return super.peekService(myContext, service);
    }
}