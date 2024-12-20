package org.j_keepass.loading.event;

public interface LoadingEvent {
    void showLoading();
    void dismissLoading();

    void updateLoadingText(String text);
}
