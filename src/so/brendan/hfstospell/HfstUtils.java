package so.brendan.hfstospell;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import fi.helsinki.hfst.ZHfstOspeller;

final public class HfstUtils {
    private static final String TAG = HfstUtils.class.getSimpleName();

    private HfstUtils() {}

    private static Context mCtx;

    private static final String ACCEPTOR = "acceptor.default.hfst";
    private static final String ERRMODEL = "errmodel.default.hfst";

    static {
        System.loadLibrary("lzma");
        System.loadLibrary("archive");
        System.loadLibrary("stlport_shared");
        System.loadLibrary("hfstospell");
    }

    public static void init(Context ctx) {
        mCtx = ctx;
    }

    public static void loadNativeLibrary() {
        // Ensures the static initializer is called
    }

    private static File getSpellerCache() {
        File spellerCache = new File(mCtx.getCacheDir(), "spellers");
        spellerCache.mkdir();
        return spellerCache;
    }

    private static File extractSpellerFromAssets(String language) throws IOException {
        Log.d(TAG, "language is " + language);
        // Open the dictionary asset.
        BufferedInputStream bis = new BufferedInputStream(mCtx.getAssets().open("dicts/" + language + ".zhfst"));
        // Create a copy of the dictionary in the cache directory.
        File f = new File(mCtx.getCacheDir() + "/" + language + ".zhfst");

        byte[] buffer = new byte[bis.available()];
        bis.read(buffer);
        bis.close();

        Log.d(TAG, "SPROUL: byte buffer size is: " + Integer.toString(buffer.length));

        FileOutputStream fos = new FileOutputStream(f);
        fos.write(buffer);
        fos.close();

        return f;
    }

    public static boolean spellerExists(String language) {
        try {
            mCtx.getAssets().open("dicts/" + language + ".zhfst").close();
            return true;
        } catch (Exception e) {
            return false;
        }
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
    public static ZHfstOspeller getSpeller(@Nonnull String language) {
        ZHfstOspeller zhfst;
        // Directory for extracted (cached) version of this spell checker.
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
        zhfst.setTemporaryDir(getSpellerCache().getAbsolutePath());

        File zhfstFile;
        try {
            zhfstFile = extractSpellerFromAssets(language);
        } catch (IOException e) {
            Log.e(TAG, "Could not load " + language + ".zhfst", e);
            return null;
        }

        File tmpPath = new File(zhfst.readZhfst(zhfstFile.getAbsolutePath()));

        zhfstFile.delete();
        tmpPath.renameTo(spellerDir);

        Log.i(TAG, "Newly created cached language " + language);

        return configure(zhfst);
    }
}
