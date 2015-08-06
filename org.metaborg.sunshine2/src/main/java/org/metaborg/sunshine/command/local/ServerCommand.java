package org.metaborg.sunshine.command.local;

import org.metaborg.core.MetaborgException;
import org.metaborg.sunshine.Main;
import org.metaborg.sunshine.command.base.ICommand;

import com.beust.jcommander.Parameters;
import com.martiansoftware.nailgun.Alias;
import com.martiansoftware.nailgun.AliasManager;
import com.martiansoftware.nailgun.NGServer;

@Parameters(commandDescription = "Starts up a Nailgun server with Sunshine loaded. "
    + "Use 'ng sunshine' to send commands to the server.")
public class ServerCommand implements ICommand {
    @Override public boolean validate() {
        return true;
    }

    @Override public int run() throws MetaborgException {
        final NGServer server = new NGServer();
        final AliasManager aliasManager = server.getAliasManager();
        aliasManager.addAlias(new Alias("sunshine", "Run Sunshine command-line jobs", Main.class));
        server.run();
        return 0;
    }
}
