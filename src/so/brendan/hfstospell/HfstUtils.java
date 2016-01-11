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
    private static final String ACCEPTOR = "acceptor.default.hfst";
    private static final String ERRMODEL = "errmodel.default.hfst";

    private Context mCtx;

    public HfstUtils(Context ctx) {
        mCtx = ctx;
    }

    static {
        System.loadLibrary("lzma");
        System.loadLibrary("archive");
        System.loadLibrary("stlport_shared");
        System.loadLibrary("hfstospell");
    }

    /*
    public static void init(Context ctx) {
        mCtx = ctx;
    }
    */

    public static void loadNativeLibrary() {
        // Ensures the static initializer is called
    }

    private File getSpellerCache() {
        File spellerCache = new File(mCtx.getCacheDir(), "spellers");
        spellerCache.mkdir();
        return spellerCache;
    }

    // Copy a dictionary from the assets directory into the cache directory.
    // Return the absolute path to the copied dictionary, as a string.
    private File extractSpellerFromAssets(String language) throws IOException {
        Log.d(TAG, "SPROUL: language is " + language);
        // Open the dictionary asset.
        BufferedInputStream bis = new BufferedInputStream(mCtx.getAssets().open("dicts/" + language + ".zhfst"));
        // Path for the copy of the dictionary in the cache directory.
        File f = new File(mCtx.getCacheDir() + "/" + language + ".zhfst");

        // Read the asset into a buffer.
        byte[] buffer = new byte[bis.available()];
        bis.read(buffer);
        bis.close();

        Log.d(TAG, "SPROUL: byte buffer size is: " + Integer.toString(buffer.length));

        // Write the buffer to the output file.
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(buffer);
        fos.close();

        assert f.isFile();

        return f;
    }

    public boolean spellerExists(String language) {
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
    public ZHfstOspeller getSpeller(@Nonnull Locale locale) {
        return getSpeller(locale.getLanguage());
    }

    @Nullable
    public ZHfstOspeller getSpeller(@Nonnull String language) {
        ZHfstOspeller zhfst;
        // Directory path for extracted (cached) version of this spell checker.
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
        String zhfstPath = zhfstFile.getAbsolutePath();
        Log.d(TAG, "SPROUL, path is: " + zhfstPath);

        File tmpPath = new File(zhfst.readZhfst(zhfstPath));

        zhfstFile.delete();
        tmpPath.renameTo(spellerDir);

        Log.i(TAG, "Newly created cached language " + language);

        return configure(zhfst);
    }
}
