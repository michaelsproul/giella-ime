package so.brendan.hfstospell;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.io.File;
import java.lang.Override;

import fi.helsinki.hfst.ZHfstOspeller;

import so.brendan.hfstospell.SpellerService;
import so.brendan.hfstospell.HfstUtils;

// This class wraps a raw C++ object and ensures safe access by liasing with the SpellerService.
class SpellerWrapper {
    private static final String TAG = SpellerWrapper.class.getSimpleName();

    private Context mCtx;
    private String mLocale;
    private ZHfstOspeller mSpeller;
    private BroadcastReceiver mReceiver;

    private class Receiver extends BroadcastReceiver {
        public Receiver() {}

        @Override
        public void onReceive(Context recCtx, Intent intent) {
            // TODO: Check that returned locale matches here.
            Log.d(TAG, "Received DICT_INSTALLED broadcast");
            instantiateSpeller();
            // recCtx.unregisterReceiver(this);
        }
    }

    public SpellerWrapper(Context context, String locale) {
        mCtx = context;
        mLocale = locale;

        // Register the broadcast listener, listening for DICT_INSTALLED messages.
        IntentFilter filter = new IntentFilter(SpellerService.ACTION_DICT_INSTALLED);
        mReceiver = new Receiver();
        context.registerReceiver(mReceiver, filter);

        // Send an initial broadcast.
        getSpeller();
    }

    public ZHfstOspeller getSpeller() {
        // If we already have a speller, return it.
        if (mSpeller != null) {
            return mSpeller;
        }
        // Otherwise, request one.
        Log.d(TAG, "Sending INSTALL_DICT broadcast");
        mCtx.startService(requestDictIntent(mLocale));
        return null;
    }

    public Intent requestDictIntent(String locale) {
        Intent intent = SpellerService.installDictIntent(mLocale);
        intent.setClass(mCtx, SpellerService.class);
        return intent;
    }

    // Upon receiving word that it is safe to do so, load the speller file from its location.
    private void instantiateSpeller() {
        Log.d(TAG, "Instantiating the speller, hooray!");

        ZHfstOspeller speller = new ZHfstOspeller();

        speller.setTemporaryDir(mCtx.getCacheDir().getAbsolutePath());

        File dictFile = HfstUtils.dictionaryFile(mLocale);
        File tmpDir = new File(speller.readZhfst(dictFile.getAbsolutePath()));

        mSpeller = HfstUtils.configure(speller);

        // mCtx.unregisterReceiver(mReceiver);
        Log.d(TAG, "Done instantiating!");
    }
}