package org.metaborg.sunshine.prims.dummies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class RefreshResourcePrimitive extends AbstractPrimitive {
    private static final Logger logger = LoggerFactory.getLogger(RefreshResourcePrimitive.class.getName());

    public RefreshResourcePrimitive() {
        super("SSL_EXT_refreshresource", 0, 1);
    }

    @Override public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) throws InterpreterException {
        logger.debug("Dummy compatibility primitive {} called", this.getName());
        return true;
    }
}
