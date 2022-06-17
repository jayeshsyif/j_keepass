package org.j_keepass.util;

import androidx.annotation.Nullable;

public class KpCustomException extends Exception {

    private int resourceId;
    private boolean useId = false;

    public KpCustomException(String message) {
        super(message);
    }

    public KpCustomException(int resourceId) {
        this.resourceId = resourceId;
        this.useId = true;
    }

    public boolean isUseId() {
        return useId;
    }

    public int getResourceId() {
        return resourceId;
    }

    @Nullable
    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
