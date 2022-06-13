package org.j_keepass.util;

import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;

public class Common {

    //this objects are not serializable so cannot be sent using bundle. So using this approach
    public static Database<?, ?, ?, ?> database;
    public static Group<?, ?, ?, ?> group;
    public static Entry<?, ?, ?, ?> entry;
}
