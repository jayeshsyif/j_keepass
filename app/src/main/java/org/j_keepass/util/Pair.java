package org.j_keepass.util;

import java.io.Serial;

public class Pair<F, S> implements java.io.Serializable {

    @Serial
    private static final long serialVersionUID = 6L;

    public F first = null;
    public S second = null;

    //~--- constructors -------------------------------------------------------

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public Pair() {
    }
}
