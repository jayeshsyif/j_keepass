package org.j_keepass.newpwd.eveninterface;


import org.j_keepass.util.Util;

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
        Util.log("In listener got generate New Pwd");
        if (listeners.size() == 0) {
            PasswordGenerator.register();
        }
        for (GenerateNewPwdEvent listener : listeners) {
            listener.generateNewPwd();
        }
    }

    public void generateNewPwd(boolean useDigit, boolean useLowerCase, boolean useUpperCase, boolean useSymbol, int length) {
        Util.log("In listener got generate New Pwd with all params");
        if (listeners.size() == 0) {
            PasswordGenerator.register();
        }
        for (GenerateNewPwdEvent listener : listeners) {
            listener.generateNewPwd(useDigit, useLowerCase, useUpperCase, useSymbol, length);
        }
    }
}
