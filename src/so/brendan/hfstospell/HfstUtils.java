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
import java.io.BufferedInputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import fi.helsinki.hfst.ZHfstOspeller;

import so.brendan.hfstospell.HfstDictionaryService;

final public class HfstUtils {
    private static final String TAG = HfstUtils.class.getSimpleName();
    private static Context mCtx;
    private static Object mLock = new Object();

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

    /// FIXME: There doesn't seem to be a better way to do this with Android.
    /// Can't use java.nio.file.Files, or AssetManager.openFd (because of compressed assets).
    public static void copyAssetToFile(InputStream src, File dest) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(src);
        byte[] buffer = new byte[bis.available()];
        bis.read(buffer);
        bis.close();
        FileOutputStream fos = new FileOutputStream(dest);
        fos.write(buffer);
        fos.close();
    }

    public static ZHfstOspeller configure(ZHfstOspeller s) {
        s.setQueueLimit(3);
        s.setWeightLimit(50);
        return s;
    }
}
