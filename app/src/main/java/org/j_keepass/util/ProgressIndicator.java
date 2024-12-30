package org.j_keepass.util;

public interface ProgressIndicator {
    void onUpdate(int progress);
    void onDone();
}
