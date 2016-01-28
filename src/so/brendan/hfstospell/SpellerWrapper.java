package so.brendan.hfstospell;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

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

    public SpellerWrapper(Context context, String locale) {
        mCtx = context;
        mLocale = locale;

        // Register the broadcast listener, listening for DICT_INSTALLED messages.
        IntentFilter filter = new IntentFilter(SpellerService.ACTION_DICT_INSTALLED);
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context recCtx, Intent intent) {
                // TODO: Check that returned locale matches here.
                instantiateSpeller();
            }
        };
        context.registerReceiver(receiver, filter);

        // Send an initial broadcast.
        getSpeller();
    }

    public ZHfstOspeller getSpeller() {
        // If we already have a speller, return it.
        if (mSpeller != null) {
            return mSpeller;
        }
        // Otherwise, request one.
        mCtx.sendBroadcast(SpellerService.installDictIntent(mLocale));
        return null;
    }

    // Upon receiving word that it is safe to do so, load the speller file from its location.
    private void instantiateSpeller() {
        Log.d(TAG, "Instantiating the speller, hooray!");
        ZHfstOspeller speller = new ZHfstOspeller();
        File dictFile = HfstUtils.dictionaryFile(locale);
        speller.readZhfst(dictFile.getAbsolutePath());
        mSpeller = HfstUtils.configure(zhfst);
        Log.d(TAG, "Done instantiating!");
    }
}