package org.webdsl.core.strategies;

import org.spoofax.interpreter.terms.IStrategoReal;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.statistics.Statistics;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class record_time_native_0_1 extends Strategy {

    public static record_time_native_0_1 instance = new record_time_native_0_1();

    @Override
    public IStrategoTerm invoke(Context context, IStrategoTerm time,
	    IStrategoTerm name) {
	final String nameStr = ((IStrategoString) name).stringValue();
	final Long timeLong = Math
		.round(((IStrategoReal) time).realValue() * 1000);
	Statistics.addDataPoint(nameStr, timeLong);
	return time;
    }
}
