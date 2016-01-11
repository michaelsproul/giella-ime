package so.brendan.hfstospell;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.inputmethod.dictionarypack.DictionaryPackConstants;
import com.android.inputmethod.dictionarypack.DictionaryProvider;
import com.android.inputmethod.dictionarypack.DictionaryService;
import com.android.inputmethod.dictionarypack.MetadataDbHelper;
import com.android.inputmethod.dictionarypack.PrivateLog;
import com.android.inputmethod.latin.utils.DebugLogUtils;

import java.util.Collection;
import java.util.Collections;

public class HfstDictionaryProvider {}