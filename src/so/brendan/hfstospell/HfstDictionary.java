package so.brendan.hfstospell;

import com.android.inputmethod.latin.Dictionary;
import com.android.inputmethod.latin.NgramContext;
import com.android.inputmethod.latin.SuggestedWords;
import com.android.inputmethod.latin.SuggestedWords.SuggestedWordInfo;
import com.android.inputmethod.latin.common.ComposedData;
import com.android.inputmethod.latin.settings.SettingsValuesForSuggestion;

import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;

import fi.helsinki.hfst.StringWeightPair;
import fi.helsinki.hfst.StringWeightPairVector;
import fi.helsinki.hfst.ZHfstOspeller;

import so.brendan.hfstospell.SpellerWrapper;

public class HfstDictionary extends Dictionary {

    public static final TAG = HfstDictionary.class.getSimpleName();

    private SpellerWrapper mSpeller;

    public HfstDictionary(Context context, String dictType, Locale locale) {
        super(dictType, locale);

        mSpeller = new SpellerWrapper(context, locale);
    }

    public HfstDictionary(Context context, Locale locale) {
        this(context, Dictionary.TYPE_MAIN, locale);
    }

    protected ArrayList<SuggestedWordInfo> getSuggestions(ComposedData composedData,
                                                          NgramContext ngramContext) {
        ArrayList<SuggestedWordInfo> out = new ArrayList<>();

        ZHfstOspeller speller = mSpeller.getSpeller();
        if (speller == null) {
            Log.d(TAG, "Dictionary waiting to be initialised");
            return out;
        }

        StringWeightPairVector suggs = speller.suggest(composedData.mTypedWord);

        for (int i = 0; i < suggs.size(); ++i) {
            StringWeightPair sugg = suggs.get(i);
            out.add(new SuggestedWordInfo(sugg.getFirst(), ngramContext.extractPrevWordsContext(),
                    (int)sugg.getSecond(), SuggestedWordInfo.KIND_CORRECTION, this,
                    SuggestedWordInfo.NOT_AN_INDEX, SuggestedWordInfo.NOT_A_CONFIDENCE));
        }

        return out;
    }

    @Override
    public ArrayList<SuggestedWordInfo> getSuggestions(ComposedData composedData,
                                                       NgramContext ngramContext,
                                                       long proximityInfoHandle,
                                                       SettingsValuesForSuggestion settingsValuesForSuggestion,
                                                       int sessionId, float weightForLocale,
                                                       float[] inOutWeightOfLangModelVsSpatialModel) {
        return getSuggestions(composedData, ngramContext);
    }

    @Override
    public boolean isInDictionary(String word) {
        ZHfstOspeller speller = mSpeller.getSpeller();
        if (speller == null) {
            return true;
        } else {
            return mSpeller.spell(word);
        }
    }
}
