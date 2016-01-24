import java.lang.Override;

import android.app.Service;
import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import so.brendan.hfstospell.HfstUtils;

class HfstDictionaryService extends Service {
    private static final String TAG = HfstDictionaryService.class.getSimpleName();
    private Context mCtx;

    @Override
    public void onCreate() {
        mCtx = this;
        HfstUtils.init(mCtx);

        Log.d(TAG, "SPROUL: Copying zhfst file from assets to files");

        // Asset stream.
        BufferedInputStream assetStream =
                new BufferedInputStream(mCtx.getAssets().open("dicts/" + "se" + ".zhfst"));

        // Read the asset into a buffer.
        byte[] buffer = new byte[assetStream.available()];
        assetStream.read(buffer);
        assetStream.close();

        Log.d(TAG, "SPROUL: byte buffer size is: " + Integer.toString(buffer.length));

        // Write the buffer to the output file.
        FileOutputStream fileStream = mCtx.openFileOutput(HfstUtils.dictionaryName("se"), Context.MODE_PRIVATE);
        fileStream.write(buffer);
        fileStream.close();

        Log.d(TAG, "SPROUL: created the zhfst file in the app files directory");
    }
}