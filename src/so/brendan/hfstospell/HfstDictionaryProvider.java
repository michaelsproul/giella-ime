package so.brendan.hfstospell;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.android.inputmethod.dictionarypack.DictionaryPackConstants;
import com.android.inputmethod.dictionarypack.DictionaryProvider;
import com.android.inputmethod.dictionarypack.DictionaryService;
import com.android.inputmethod.dictionarypack.MetadataDbHelper;
import com.android.inputmethod.dictionarypack.PrivateLog;
import com.android.inputmethod.latin.utils.DebugLogUtils;

import java.util.Collection;
import java.util.Collections;

public class HfstDictionaryProvider extends ContentProvider {
    private static final String TAG = HfstDictionaryProvider.class.getSimpleName();

    public static final Uri CONTENT_URI = DictionaryProvider.CONTENT_URI;
    public static final String DICT_LIST_MIME_TYPE = DictionaryProvider.DICT_LIST_MIME_TYPE;
    public static final String DICT_DATAFILE_MIME_TYPE = DictionaryProvider.DICT_DATAFILE_MIME_TYPE;

    private static final int NO_MATCH = 0;
    private static final int DICTIONARY_V2_METADATA = 3;
    private static final int DICTIONARY_V2_WHOLE_LIST = 4;
    private static final int DICTIONARY_V2_DICT_INFO = 5;
    private static final int DICTIONARY_V2_DATAFILE = 6;

    private static final UriMatcher sUriMatcherV2 = new UriMatcher(NO_MATCH);
    static
    {
        sUriMatcherV2.addURI(DictionaryPackConstants.AUTHORITY, "*/metadata",
                DICTIONARY_V2_METADATA);
        sUriMatcherV2.addURI(DictionaryPackConstants.AUTHORITY, "*/list", DICTIONARY_V2_WHOLE_LIST);
        sUriMatcherV2.addURI(DictionaryPackConstants.AUTHORITY, "*/dict/*",
                DICTIONARY_V2_DICT_INFO);
        sUriMatcherV2.addURI(DictionaryPackConstants.AUTHORITY, "*/datafile/*",
                DICTIONARY_V2_DATAFILE);
    }

    private static final class HfstDictCursor extends AbstractCursor {
        static private final String[] columnNames = { MetadataDbHelper.WORDLISTID_COLUMN,
                MetadataDbHelper.LOCALE_COLUMN, MetadataDbHelper.RAW_CHECKSUM_COLUMN };

        private final String name;

        public HfstDictCursor(String name) {
            this.name = name;
        }

        @Override
        public int getCount() {
            return HfstUtils.spellerExists(name) ? 1 : 0;
        }

        @Override
        public String[] getColumnNames() {
            return columnNames;
        }

        @Override
        public String getString(final int column) {
            if (column > 2) {
                return null;
            }

            if (column == 2) {
                return "";
            }

            return name;
        }

        @Override public double getDouble(int column) { return 0; }
        @Override public float getFloat(int column) { return 0; }
        @Override public int getInt(int column) { return 0; }
        @Override public short getShort(int column) { return 0; }
        @Override public long getLong(int column) { return 0; }

        @Override
        public boolean isNull(final int column) {
            return column != 0;
        }
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    private static int matchUri(final Uri uri) {
        int protocolVersion = 1;
        final String protocolVersionArg = uri.getQueryParameter(
                DictionaryProvider.QUERY_PARAMETER_PROTOCOL_VERSION);
        if ("2".equals(protocolVersionArg)) protocolVersion = 2;
        switch (protocolVersion) {
            case 2: return sUriMatcherV2.match(uri);
            default: return NO_MATCH;
        }
    }

    private static String getClientId(final Uri uri) {
        int protocolVersion = 1;
        final String protocolVersionArg = uri.getQueryParameter(
                DictionaryProvider.QUERY_PARAMETER_PROTOCOL_VERSION);
        if ("2".equals(protocolVersionArg)) protocolVersion = 2;
        switch (protocolVersion) {
            case 2: return uri.getPathSegments().get(0);
            default: return null;
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "SPROUL Our shit is being run");
        final String clientId = getClientId(uri);
        final int match = matchUri(uri);

        switch (match) {
            case DICTIONARY_V2_WHOLE_LIST:
                // TODO
                //return MetadataDbHelper.queryDictionaries(getContext(), clientId);
                break;
            case DICTIONARY_V2_DICT_INFO:
                // In protocol version 2, we return null if the client is unknown. Otherwise
                // we behave exactly like for protocol 1.
                //if (!MetadataDbHelper.isClientKnown(getContext(), clientId)) return null;
                // Fall through
                /*
                final String locale = uri.getLastPathSegment();
                final Collection<WordListInfo> dictFiles =
                        getDictionaryWordListsForLocale(clientId, locale);
                // TODO: pass clientId to the following function
                DictionaryService.updateNowIfNotUpdatedInAVeryLongTime(getContext());
                if (null != dictFiles && dictFiles.size() > 0) {
                    PrivateLog.log("Returned " + dictFiles.size() + " files");
                    return new ResourcePathCursor(dictFiles);
                }
                PrivateLog.log("No dictionary files for this URL");
                return new ResourcePathCursor(Collections.<WordListInfo>emptyList());
                */
                break;
        }

        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = matchUri(uri);
        switch (match) {
            case NO_MATCH: return null;
            case DICTIONARY_V2_WHOLE_LIST:
            case DICTIONARY_V2_DICT_INFO: return DICT_LIST_MIME_TYPE;
            case DICTIONARY_V2_DATAFILE: return DICT_DATAFILE_MIME_TYPE;
            default: return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Updating dictionary words is not supported");
    }
}
