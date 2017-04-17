package spoofax.core.cmd;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;

import spoofax.core.cmd.command.ICommand;
import spoofax.core.cmd.delegate.LanguageDelegate;
import spoofax.core.cmd.delegate.LanguagesDelegate;
import spoofax.core.cmd.delegate.ProjectPathDelegate;
import spoofax.core.cmd.parameter.MainParameters;

public class CmdModule extends AbstractModule {
    @Override protected void configure() {
        bindRunner();
        bindCommands(MapBinder.newMapBinder(binder(), String.class, ICommand.class));
        bindParameters();
        bindDelegates();
    }


    /**
     * Binds the runner. Override to bind a different runner.
     */
    protected void bindRunner() {
        bind(Runner.class).in(Singleton.class);
    }

    /**
     * Binds commands. Extend to bind additional commands.
     * 
     * @param commandsBinder
     *            Command binder.
     */
    protected void bindCommands(MapBinder<String, ICommand> commandsBinder) {

    }

    /**
     * Binds parameter classes. Extend to bind additional parameter classes.
     */
    protected void bindParameters() {
        bind(MainParameters.class);
    }

    /**
     * Binds parameter delegates. Extend to bind additional parameter delegates.
     */
    protected void bindDelegates() {
        bind(LanguageDelegate.class);
        bind(LanguagesDelegate.class);
        bind(ProjectPathDelegate.class);
    }
}
