package com.mindshield.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link Throwable#printStackTrace()} yerine kullanılır; IDE/Sonar uyarılarını giderir,
 * istisnaları {@link java.util.logging} ile sınıf adına göre kaydeder.
 */
public final class AppLog {

    private AppLog() {
    }

    public static void severe(Throwable t) {
        if (t == null) {
            return;
        }
        String loggerName = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .walk(s -> s.skip(1).findFirst().map(f -> f.getClassName()))
                .orElse("com.mindshield");
        Logger.getLogger(loggerName).log(Level.SEVERE, t.toString(), t);
    }
}
