package org.webdsl.core.strategies;

import org.spoofax.interpreter.terms.IStrategoReal;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.Environment;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class record_time_native_0_1 extends Strategy {

	public static record_time_native_0_1 instance = new record_time_native_0_1();

	@Override
	public IStrategoTerm invoke(Context context, IStrategoTerm time, IStrategoTerm name) {
		Environment.INSTANCE().getCurrentRoundMetrics().recordTimeSpent(((IStrategoString) name).stringValue(), ((IStrategoReal) time).realValue());
		return time;
	}
}
