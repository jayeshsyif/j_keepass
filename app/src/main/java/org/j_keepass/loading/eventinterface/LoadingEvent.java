package org.j_keepass.loading.eventinterface;

import android.content.Context;

public interface LoadingEvent {
    void showLoading();
    void dismissLoading();

    void updateLoadingText(String text);
}
