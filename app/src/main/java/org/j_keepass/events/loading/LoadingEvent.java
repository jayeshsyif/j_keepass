package org.j_keepass.events.loading;

public interface LoadingEvent {
    void showLoading();
    void dismissLoading();

    void updateLoadingText(String text);
}
