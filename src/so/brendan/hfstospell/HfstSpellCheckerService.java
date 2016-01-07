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

/**
 * Service for spell checking, using HFST dictionaries.
 */
public final class HfstSpellCheckerService extends SpellCheckerService {
    private static final String TAG = HfstSpellCheckerService.class.getSimpleName();

    public HfstSpellCheckerService() {
        super();
        Log.d(TAG, "SPROUL: just created a spell checker");
    }

    @Override
    public Session createSession() {
        return new HfstSpellCheckerSession();
    }

    private class HfstSpellCheckerSession extends Session {
        private HfstSpellCheckerSession() {}

        @Override
        public void onCreate() {}

        @Override
        public SuggestionsInfo onGetSuggestions(TextInfo textInfo, int suggestionsLimit) {
            Log.d(TAG, "SPROUL: getting suggestions");
            String[] suggestions = {"HELLO WORLD"};
            int attributes = SuggestionsInfo.RESULT_ATTR_LOOKS_LIKE_TYPO;

            return new SuggestionsInfo(attributes, suggestions);
        }
    }
}
