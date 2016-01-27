package so.brendan.hfstospell;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.nio.channels.Channels;
import java.util.Locale;



import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import fi.helsinki.hfst.ZHfstOspeller;

import so.brendan.hfstospell.HfstDictionaryService;

final public class HfstUtils {
    private static final String TAG = HfstUtils.class.getSimpleName();
    private static Context mCtx;

    private static final String ACCEPTOR = "acceptor.default.hfst";
    private static final String ERRMODEL = "errmodel.default.hfst";

    static {
        System.loadLibrary("lzma");
        System.loadLibrary("archive");
        System.loadLibrary("stlport_shared");
        System.loadLibrary("hfstospell");
    }

    private HfstUtils() {}

    public static void init(Context ctx) {
        mCtx = ctx;
    }

    public static void loadNativeLibrary() {
        // Ensures the static initializer is called
    }

    public static boolean isBundled(String locale) {
        switch (locale) {
            case "se":
            case "zz_SJD":
                return true;
            default:
                return false;
        }
    }

    public static String metadataFilename(String locale) {
        return locale + "_metadata.json";
    }

    public static String dictionaryFilename(String locale) {
        return locale + ".zhfst";
    }

    public static File dictionaryFile(String locale) {
        return mCtx.getFileStreamPath(dictionaryFilename(locale));
    }

    public static File metadataFile(String locale) {
        return mCtx.getFileStreamPath(metadataFilename(locale));
    }

    public static InputStream bundledDictionary(String locale) throws IOException {
        return mCtx.getAssets().open("dicts/" + dictionaryFilename(locale));
    }

    public static InputStream bundledMetadata(String locale) throws IOException {
        return mCtx.getAssets().open("dicts/" + metadataFilename(locale));
    }

    public static void copyAssetToFile(InputStream src, File dest) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(dest);

        Channel input = Channels.newChannel(src);
        FileChannel output = outputStream.getChannel();

        input.transferTo(0, input.size(), output);

        inputStream.close();
        outputStream.close();
    }

    // Copy the fallback dictionary and metadata for a given locale to the main dictionary directory.
    // Return the installed dictionary file, or null if no bundled dictionary was available.
    @Nullable
    private static File installBundled(String locale) {
        if (!isBundled(locale)) {
            Log.w(TAG, "Unable to locate a bundled dictionary for locale: " + locale);
            return null;
        }
        File bundledDict = dictionaryFile(locale);
        try {
            copyAssetToFile(bundledMetadata(locale), metadataFile(locale));
            copyAssetToFile(bundledDictionary(locale), bundledDict);
        } catch (Exception e) {
            Log.e(TAG, "IO exception when installing bundled dictionary for locale: " + e);
            Log.e(TAG, "Exception: " + e.getMessage());
            return null;
        }
        return bundledDict;
    }

    // Create an intent to have the dictionary for the given locale updated by the updater service.
    public static Intent dictUpdateIntent(String locale) {
        Intent intent = new Intent(mCtx, HfstDictionaryService.class);
        intent.setAction(HfstDictionaryService.ACTION_UPDATE_DICT);
        intent.putExtra(HfstDictionaryService.EXTRA_LOCALE_KEY, locale);
        return intent;
    }

    private static ZHfstOspeller configure(ZHfstOspeller s) {
        s.setQueueLimit(3);
        s.setWeightLimit(50);
        return s;
    }

    @Nullable
    public static ZHfstOspeller getSpeller(@Nonnull Locale locale) {
        return getSpeller(locale.getLanguage());
    }

    @Nullable
    public static ZHfstOspeller getSpeller(@Nonnull String locale) {
        ZHfstOspeller zhfst = new ZHfstOspeller();

        File dictFile = dictionaryFile(locale);

        // Try to fall back to a bundled dictionary if no regular dictionary exists.
        if (!dictFile.exists()) {
            dictFile = installBundled(locale);
            mCtx.startService(dictUpdateIntent(locale));
            if (dictFile == null) {
                return null;
            }
        }

        zhfst.readZhfst(dictFile.getAbsolutePath());

        return configure(zhfst);
    }
}
