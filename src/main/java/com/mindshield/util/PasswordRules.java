package com.mindshield.util;

/**
 * Ortak kayıt / şifre kuralları: en az 4 karakter, en az bir harf ve bir rakam.
 */
public final class PasswordRules {

    private PasswordRules() {
    }

    // Boş veya null dönerse şifre geçerlidir; aksi halde kullanıcıya gösterilecek Türkçe hata mesajı. 
    public static String validate(String password) { 
        if (password == null || password.isEmpty()) {
            return "Şifre boş bırakılamaz.";
        }
        if (password.length() < 4) {
            return "Şifre en az 4 karakter olmalıdır.";
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            }
            if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        if (!hasLetter || !hasDigit) {
            return "Şifre en az bir harf ve en az bir rakam içermelidir.";
        }
        return null;
    }
}
