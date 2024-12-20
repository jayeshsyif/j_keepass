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

    private ArrayList<GenerateNewPwdEvent> listeners = new ArrayList<>();

    public void addListener(GenerateNewPwdEvent listener) {
        listeners.add(listener);
    }

    public void removeListener(GenerateNewPwdEvent listener) {
        listeners.remove(listener);
    }

    public void generateNewPwd() {
        Utils.log("In listener got generate New Pwd");
        if (listeners.size() == 0 || listeners.size() > 0) {
            if (!listeners.contains(PasswordGenerator.getInstance())) {
                PasswordGenerator.register();
            }
        }
        for (GenerateNewPwdEvent listener : listeners) {
            listener.generateNewPwd();
        }
    }

    public void generateNewPwd(boolean useDigit, boolean useLowerCase, boolean useUpperCase, boolean useSymbol, int length) {
        Utils.log("In listener got generate New Pwd with all params");
        if (listeners.size() == 0 || listeners.size() > 0) {
            if (!listeners.contains(PasswordGenerator.getInstance())) {
                PasswordGenerator.register();
            }
        }
        for (GenerateNewPwdEvent listener : listeners) {
            listener.generateNewPwd(useDigit, useLowerCase, useUpperCase, useSymbol, length);
        }
    }


    public void showNewPwd(String newPwd, boolean useDigit, boolean useLowerCase, boolean useUpperCase, boolean useSymbol, int length) {
        Utils.log("In listener show new pwd");
        for (GenerateNewPwdEvent listener : listeners) {
            listener.showNewPwd(newPwd, useDigit, useLowerCase, useUpperCase, useSymbol, length);
        }
    }

    public void showFailedNewGenPwd(String errorMsg) {
        Utils.log("In listener show failed gen pwd");
        for (GenerateNewPwdEvent listener : listeners) {
            listener.showFailedNewGenPwd(errorMsg);
        }
    }
}
