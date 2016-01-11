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

import java.util.ArrayList;
import java.lang.Override;

import fi.helsinki.hfst.StringWeightPair;
import fi.helsinki.hfst.StringWeightPairVector;
import fi.helsinki.hfst.ZHfstOspeller;

import so.brendan.hfstospell.HfstUtils;

/**
 * Service for spell checking, using HFST dictionaries.
 */
public final class HfstSpellCheckerService extends SpellCheckerService {
    private static final String TAG = HfstSpellCheckerService.class.getSimpleName();

    private HfstUtils mHfstUtils;
    private ZHfstOspeller mSpeller;

    public HfstSpellCheckerService() {
        super();
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "SPROUL: HfstSpellCheckerService::onCreate() running");

        if (this == null || this.getBaseContext() == null) {
            Log.e(TAG, "SPROUL why is this null?");
        }

        mHfstUtils = new HfstUtils(this.getBaseContext());

        // FIXME: get rid of hardcoded locale here
        mSpeller = mHfstUtils.getSpeller("se");

        Log.d(TAG, "SPROUL: just created a spell checker");
    }

    @Override
    public Session createSession() {
        return new HfstSpellCheckerSession(mSpeller);
    }

    private class HfstSpellCheckerSession extends Session {
        private ZHfstOspeller mSpeller;

        private HfstSpellCheckerSession(ZHfstOspeller speller) {
            mSpeller = speller;
        }

        @Override
        public void onCreate() {}

        @Override
        public SuggestionsInfo onGetSuggestions(TextInfo textInfo, int suggestionsLimit) {
            String word = textInfo.getText();

            Log.d(TAG, "SPROUL: calling C++ spell checker");

            // Check if the word is spelled correctly.
            if (mSpeller.spell(word)) {
                Log.d(TAG, "SPROUL Word spelled correctly: " + word );
                return new SuggestionsInfo(SuggestionsInfo.RESULT_ATTR_IN_THE_DICTIONARY, new String[0]);
            }

            // If the word isn't correct, query the C++ spell checker for suggestions.
            StringWeightPairVector suggs = mSpeller.suggest(word);
            Log.d(TAG, "SPROUL Word spelled incorrectly: " + word ", num suggestions " + Long.toString(suggs.size()));
            String[] suggestions = new String[(int) suggs.size()]; // dodgy int cast, should be ok.

            for (int i = 0; i < suggs.size(); i++) {
                suggestions[i] = suggs.get(i).getFirst();
            }

            int attrs;
            if (suggestions.length > 0) {
                attrs = SuggestionsInfo.RESULT_ATTR_HAS_RECOMMENDED_SUGGESTIONS;
            } else {
                attrs = SuggestionsInfo.RESULT_ATTR_LOOKS_LIKE_TYPO;
            }

            Log.d(TAG, "SPROUL suggestions: " + Arrays.toString(suggestions));
            return new SuggestionsInfo(attrs, suggestions);
        }
    }
}
