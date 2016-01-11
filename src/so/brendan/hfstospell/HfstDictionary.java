package so.brendan.hfstospell;

import com.android.inputmethod.latin.Dictionary;
import com.android.inputmethod.latin.NgramContext;
import com.android.inputmethod.latin.SuggestedWords;
import com.android.inputmethod.latin.SuggestedWords.SuggestedWordInfo;
import com.android.inputmethod.latin.common.ComposedData;
import com.android.inputmethod.latin.settings.SettingsValuesForSuggestion;

import java.util.ArrayList;
import java.util.Locale;

import fi.helsinki.hfst.StringWeightPair;
import fi.helsinki.hfst.StringWeightPairVector;
import fi.helsinki.hfst.ZHfstOspeller;

import so.brendan.hfstospell.HfstUtils;

public class HfstDictionary {}

/*
public class HfstDictionary extends Dictionary {
    private ZHfstOspeller mSpeller;
    private HfstUtils mHfstUtils;

    public HfstDictionary(String dictType, Locale locale) {
        super(dictType, locale);
        mHfstUtils = new HfstUtils(null);
        mSpeller = HfstUtils.getSpeller(locale);
    }

    public HfstDictionary(Locale locale) {
        this(Dictionary.TYPE_MAIN, locale);
    }

    protected ArrayList<SuggestedWordInfo> getSuggestions(ComposedData composedData,
                                                          NgramContext ngramContext) {
        StringWeightPairVector suggs = mSpeller.suggest(composedData.mTypedWord);
        ArrayList<SuggestedWordInfo> out = new ArrayList<>();

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
        return mSpeller.spell(word);
    }
}
*/
