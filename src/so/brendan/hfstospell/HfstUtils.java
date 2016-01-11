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

import fi.helsinki.hfst.StringWeightPair;
import fi.helsinki.hfst.StringWeightPairVector;
import fi.helsinki.hfst.ZHfstOspeller;

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
        //test();
    }

    private static void test() {
        ZHfstOspeller speller = getSpeller("se");
        StringWeightPairVector vec = speller.suggest("nuvviDspeller");

        for (int i = 0; i < vec.size(); ++i) {
            Log.d(TAG, String.format("%s: %s", vec.get(i).getFirst(), vec.get(i).getSecond()));
        }
    }

    public static void loadNativeLibrary() {
        // Ensures the static initializer is called
    }

    private static File getSpellerCache() {
        return new File(mCtx.getFilesDir(), "spellers");
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

    public static ZHfstOspeller configure(ZHfstOspeller s) {
        s.setQueueLimit(3);
        s.setWeightLimit(50);
        return s;
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

    @Nullable
    public static ZHfstOspeller getSpeller(@Nonnull String language) {
        ZHfstOspeller zhfst;
        File spellerDir = new File(getSpellerCache(), language);

        // If pre-cached, reuse.
        if (spellerDir.isDirectory()) {
            File acceptor = new File(spellerDir, ACCEPTOR);
            File errmodel = new File(spellerDir, ERRMODEL);

            if (acceptor.exists() && errmodel.exists()) {
                Log.i(TAG, "Using cached speller for " + language);
                return configure(new ZHfstOspeller(acceptor.getAbsolutePath(),
                                                   errmodel.getAbsolutePath()));
            }
        }

        // Otherwise, unzip and rock on
        zhfst = new ZHfstOspeller();
        zhfst.setTemporaryDir(mCtx.getCacheDir().getAbsolutePath());

        File zhfstFile = new File("wtf");
        /*
        try {
            zhfstFile = new File("aseafasd");
        } catch (IOException e) {
            Log.e(TAG, "Could not load " + language + ".zhfst", e);
            return null;
        }
        */

        File tmpPath = new File(zhfst.readZhfst(zhfstFile.getAbsolutePath()));
        Log.w(TAG, "tmpPath: " + tmpPath);

        zhfstFile.delete();
        if (!spellerDir.mkdirs() || !tmpPath.renameTo(spellerDir)) {
            Log.e(TAG, "Temp path could not be renamed!");
            return null;
        }

        // Re-run to get cached version.
        zhfst.delete();
        return getSpeller(language);
    }

    public static InputStream bundledDictionary(String locale) throws IOException {
        return mCtx.getAssets().open("dicts/" + dictionaryFilename(locale));
    }

    public static InputStream bundledMetadata(String locale) throws IOException {
        return mCtx.getAssets().open("dicts/" + metadataFilename(locale));
    }

    /// XXX: There isn't a better way to do this with Android.
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
}
