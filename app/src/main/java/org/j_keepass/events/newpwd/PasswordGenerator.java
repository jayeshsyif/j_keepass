package org.j_keepass.events.newpwd;

import org.j_keepass.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PasswordGenerator implements GenerateNewPwdEvent {

    private PasswordGenerator() {
    }

    private static final PasswordGenerator SOURCE = new PasswordGenerator();

    public static PasswordGenerator getInstance() {
        return SOURCE;
    }

    public static void register() {
        GenerateNewPasswordEventSource.getInstance().addListener(SOURCE);
    }

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String PUNCTUATION = "!@#$%&*()_+-=[]|,./?><";

    @Override
    public void generateNewPwd(boolean useDigit, boolean useLowerCase, boolean useUpperCase, boolean useSymbol, int length) {
        Utils.log("Generate new pwd event received");
        if (!useDigit && !useSymbol && !useLowerCase && !useUpperCase) {
            GenerateNewPasswordEventSource.getInstance().showFailedNewGenPwd("All cannot be false! Please provide proper Input.");
        } else if (length == 0) {
            GenerateNewPasswordEventSource.getInstance().showFailedNewGenPwd("Password length can't be 0 (Zero)");
        } else {
            StringBuilder password = new StringBuilder(length);
            Random random = new Random(System.nanoTime());
            List<String> charCategories = new ArrayList<>(4);
            if (useLowerCase) {
                charCategories.add(LOWER);
            }
            if (useUpperCase) {
                charCategories.add(UPPER);
            }
            if (useDigit) {
                charCategories.add(DIGITS);
            }
            if (useSymbol) {
                charCategories.add(PUNCTUATION);
            }

            for (int i = 0; i < length; i++) {
                String charCategory = charCategories.get(random.nextInt(charCategories.size()));
                int position = random.nextInt(charCategory.length());
                password.append(charCategory.charAt(position));
            }
            Utils.log("Generated new pwd");
            Utils.sleepFor3MSec();
            GenerateNewPasswordEventSource.getInstance().showNewPwd(new String(password), useDigit, useLowerCase, useUpperCase, useSymbol, length);
        }
    }

    @Override
    public void generateNewPwd() {
        generateNewPwd(true, true, true, true, 20);
    }

    @Override
    public void showNewPwd(String newPwd, boolean useDigit, boolean useLowerCase, boolean useUpperCase, boolean useSymbol, int length) {
        //ignore
    }

    @Override
    public void showFailedNewGenPwd(String errorMsg) {
        //ignore
    }
}
