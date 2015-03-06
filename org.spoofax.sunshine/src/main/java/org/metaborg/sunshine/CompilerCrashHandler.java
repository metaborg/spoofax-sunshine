package org.metaborg.sunshine;

import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompilerCrashHandler implements UncaughtExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(CompilerCrashHandler.class);

    @Override public void uncaughtException(Thread arg0, Throwable arg1) {
        logger.error("Thread {} died with uncaught exception {}", arg0.getName(), arg1);
        arg1.printStackTrace();
        logger.error("Exiting because of previous error");
        System.exit(1);
    }
}
