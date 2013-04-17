/**
 * 
 */
package org.spoofax.sunshine;

import java.lang.Thread.UncaughtExceptionHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class CompilerCrashHandler implements UncaughtExceptionHandler {

    private static final Logger logger = LogManager
	    .getLogger(CompilerCrashHandler.class.getName());

    @Override
    public void uncaughtException(Thread arg0, Throwable arg1) {
	logger.fatal("Thread {} died with uncaught exception {}",
		arg0.getName(), arg1);
	arg1.printStackTrace();
	logger.fatal("Exiting because of previous error");
	System.exit(1);
    }

}
