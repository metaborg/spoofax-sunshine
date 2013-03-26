/**
 * 
 */
package org.spoofax.sunshine;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class CompilerCrashHandler implements UncaughtExceptionHandler {

	@Override
	public void uncaughtException(Thread arg0, Throwable arg1) {
		System.err.println("Thread: " + arg0.getName() + " died with uncaught exception. Stacktrace follows...");
		arg1.printStackTrace();
		System.err.println("Exiting now!");
		System.exit(1);
	}

}
