/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package so.brendan.hfstospell;

import android.view.textservice.TextInfo;
import android.view.textservice.SuggestionsInfo;
import android.service.textservice.SpellCheckerService;
import android.service.textservice.SpellCheckerService.Session;
import android.util.Log;

import java.lang.Override;

import fi.helsinki.hfst.StringWeightPair;
import fi.helsinki.hfst.StringWeightPairVector;
import fi.helsinki.hfst.ZHfstOspeller;

/**
 * Service for spell checking, using HFST dictionaries.
 */
public final class HfstSpellCheckerService extends SpellCheckerService {
    private static final String TAG = HfstSpellCheckerService.class.getSimpleName();

    private ZHfstOspeller mSpeller;

    public HfstSpellCheckerService() {
        super();
        // FIXME: get rid of hardcoded locale here
        mSpeller = HfstUtils.getSpeller("zz_SJD");
        Log.d(TAG, "SPROUL: just created a spell checker");
    }

    @Override
    public Session createSession() {
        return new HfstSpellCheckerSession(mSpeller);
    }

    private class HfstSpellCheckerSession extends Session {
        private ZHfstOspeller mSpeller;
        // FIXME: use one of these from somewhere else?
        private final static String[] EMPTY_ARRAY = {};

        private HfstSpellCheckerSession(ZHfstSpeller speller) {
            mSpeller = speller;
        }

        /*
        @Override
        public void onCreate() {}
        */

        @Override
        public SuggestionsInfo onGetSuggestions(TextInfo textInfo, int suggestionsLimit) {
            String word = textInfo.getText();

            Log.d(TAG, "SPROUL: calling C++ spell checker");

            // Check if the word is spelled correctly.
            if (mSpeller.spell(word)) {
                return new SuggestionsInfo(SuggestionsInfo.RESULT_ATTR_IN_THE_DICTIONARY, EMPTY_ARRAY);
            }

            // If the word isn't correct, query the C++ spell checker for suggestions.
            StringWeightPairVector suggs = mSpeller.suggest(word);
            ArrayList<String> suggestions = new ArrayList<String>();

            for (int i = 0; i < suggs.size(); i++) {
                StringWeightPair sugg = suggs.get(i);
                String suggWord = suggs.getFirst();
                suggestions.add(suggWord);
            }

            int attrs = SuggestionsInfo.RESULT_ATTR_HAS_RECOMMENDED_SUGGESTIONS;

            return new SuggestionsInfo(attrs, (String[]) suggestions.toArray());
        }
    }
}
