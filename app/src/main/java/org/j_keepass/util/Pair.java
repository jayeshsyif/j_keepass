package org.j_keepass.util;

public class Pair<F, S> implements java.io.Serializable {

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

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Pair{");
        sb.append("first=").append(first);
        sb.append(", second=").append(second);
        sb.append('}');
        return sb.toString();
    }
}