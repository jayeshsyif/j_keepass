package org.j_keepass.events.newpwd;


import org.j_keepass.util.Utils;

import java.util.ArrayList;

public class GenerateNewPasswordEventSource {
    private static final GenerateNewPasswordEventSource SOURCE = new GenerateNewPasswordEventSource();

    private GenerateNewPasswordEventSource() {
    }

    public static GenerateNewPasswordEventSource getInstance() {
        return SOURCE;
    }

    private final ArrayList<GenerateNewPwdEvent> listeners = new ArrayList<>();

    public void addListener(GenerateNewPwdEvent listener) {
        listeners.add(listener);
    }

    public void removeListener(GenerateNewPwdEvent listener) {
        listeners.remove(listener);
    }

    public void generateNewPwd() {
        Utils.log("In listener got generate New Pwd");
        if (!listeners.contains(PasswordGenerator.getInstance())) {
            PasswordGenerator.register();
        }
        for (GenerateNewPwdEvent listener : listeners) {
            listener.generateNewPwd();
        }
    }

    public void generateNewPwd(boolean useDigit, boolean useLowerCase, boolean useUpperCase, boolean useSymbol, int length) {
        Utils.log("In listener got generate New Pwd with all params");
        if (!listeners.contains(PasswordGenerator.getInstance())) {
            PasswordGenerator.register();
        }
        for (GenerateNewPwdEvent listener : listeners) {
            listener.generateNewPwd(useDigit, useLowerCase, useUpperCase, useSymbol, length);
        }
    }


    public void showNewPwd(String newPwd, boolean useDigit, boolean useLowerCase, boolean useUpperCase, boolean useSymbol, int length) {
        Utils.log("In listener show new pwd");
        for (GenerateNewPwdEvent listener : listeners) {
            try {
                listener.showNewPwd(newPwd, useDigit, useLowerCase, useUpperCase, useSymbol, length);
            } catch (Throwable t) {
                // ignore
            }
        }
    }

    public void showFailedNewGenPwd(String errorMsg) {
        Utils.log("In listener show failed gen pwd");
        for (GenerateNewPwdEvent listener : listeners) {
            listener.showFailedNewGenPwd(errorMsg);
        }
    }
}
