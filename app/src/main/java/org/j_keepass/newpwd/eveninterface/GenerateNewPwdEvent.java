package org.j_keepass.newpwd.eveninterface;

public interface GenerateNewPwdEvent {
    void generateNewPwd(boolean useDigit, boolean useUpperCase, boolean useLowerCase, boolean useSymbol, int length);
    void generateNewPwd();
}
