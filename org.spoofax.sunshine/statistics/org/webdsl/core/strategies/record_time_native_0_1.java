package org.webdsl.core.strategies;

import org.spoofax.interpreter.terms.IStrategoReal;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.Environment;
import org.spoofax.sunshine.statistics.RecordingStack;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class record_time_native_0_1 extends Strategy {

	public static record_time_native_0_1 instance = new record_time_native_0_1();

	@Override
	public IStrategoTerm invoke(Context context, IStrategoTerm time, IStrategoTerm name) {
		if (Environment.INSTANCE().getLaunchConfiguration().storeStats) {
			final String nameStr = ((IStrategoString) name).stringValue();
			final Long timeLon = Math.round(((IStrategoReal) time).realValue() * 1000);
			RecordingStack.INSTANCE().current().addDataPoint(nameStr, timeLon);
		}
		return time;
	}
}
