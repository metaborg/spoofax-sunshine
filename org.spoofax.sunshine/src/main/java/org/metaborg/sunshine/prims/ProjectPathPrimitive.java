package org.metaborg.sunshine.prims;

import org.metaborg.sunshine.environment.LaunchConfiguration;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class ProjectPathPrimitive extends AbstractPrimitive {

    public ProjectPathPrimitive() {
        super("SSL_EXT_projectpath", 0, 0);
    }

    @Override public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) throws InterpreterException {

        final ServiceRegistry sunshineEnv = ServiceRegistry.INSTANCE();
        final LaunchConfiguration launch = sunshineEnv.getService(LaunchConfiguration.class);
        IStrategoString projectPath = launch.termFactory.makeString(launch.projectDir.getName().getPath());
        env.setCurrent(projectPath);
        return true;
    }
}
