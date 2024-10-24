package org.j_keepass.util;

import android.net.Uri;

import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.kdbx.KdbxCreds;

public class Common {

    //this objects are not serializable so cannot be sent using bundle. So using this approach
    public static Database<?, ?, ?, ?> database;
    public static Group<?, ?, ?, ?> group;
    public static Entry<?, ?, ?, ?> entry;
    public static KdbxCreds creds;
    public static Uri kdbxFileUri;

    public static boolean isCodecAvailable = false;
    public static final float ANIMATION_TIME = 0.05f;

    public static final String DOT_SYMBOL_CODE = " \u2192 ";
    public static final String SUB_DIRECTORY_ARROW_SYMBOL_CODE = " \u21B3 ";
}
