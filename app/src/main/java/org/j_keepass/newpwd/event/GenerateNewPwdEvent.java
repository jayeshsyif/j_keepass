package org.j_keepass.newpwd.event;

public interface GenerateNewPwdEvent {
    void generateNewPwd(boolean useDigit, boolean useUpperCase, boolean useLowerCase, boolean useSymbol, int length);
    void generateNewPwd();
    void showNewPwd(String newPwd,boolean useDigit, boolean useLowerCase, boolean useUpperCase, boolean useSymbol, int length);
    void showFailedNewGenPwd(String errorMsg);
}
