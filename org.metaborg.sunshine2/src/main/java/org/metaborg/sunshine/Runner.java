package org.metaborg.sunshine;

import java.util.Map;
import java.util.Map.Entry;

import org.metaborg.core.MetaborgException;
import org.metaborg.sunshine.arguments.MainArguments;
import org.metaborg.sunshine.command.base.ICommand;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class Runner {
    private static final ILogger logger = LoggerUtils.logger(Runner.class);

    private final Map<String, ICommand> localCommands;
    private final Map<String, ICommand> remoteCommands;


    @Inject public Runner(@Named("local") Map<String, ICommand> localCommands,
        @Named("remote") Map<String, ICommand> remoteCommands) {
        this.localCommands = localCommands;
        this.remoteCommands = remoteCommands;
    }


    public int run(String[] args, boolean remote) {
        final Map<String, ICommand> commands = remote ? remoteCommands : localCommands;
        final MainArguments arguments = new MainArguments();
        final JCommander jc = new JCommander(arguments);
        for(Entry<String, ICommand> entry : commands.entrySet()) {
            jc.addCommand(entry.getKey(), entry.getValue());
        }

        try {
            jc.parse(args);
        } catch(ParameterException e) {
            logger.error(e.getMessage());
            final StringBuilder sb = new StringBuilder();
            jc.usage(sb);
            System.err.println(sb.toString());
            return -1;
        }

        if(arguments.help) {
            final StringBuilder sb = new StringBuilder();
            jc.usage(sb);
            System.err.println(sb.toString());
            return 0;
        }

        if(arguments.exit) {
            logger.info("Exitting immediately for testing purposes");
            return 0;
        }

        final ICommand command = commands.get(jc.getParsedCommand());
        if(command == null) {
            logger.error("No command was specified");
            final StringBuilder sb = new StringBuilder();
            jc.usage(sb);
            System.err.println(sb.toString());
            return -1;
        }

        if(!command.validate()) {
            final StringBuilder sb = new StringBuilder();
            jc.usage(jc.getParsedCommand(), sb);
            System.err.println(sb.toString());
            return -1;
        }

        try {
            return command.run();
        } catch(MetaborgException e) {
            logger.error("Error during command execution", e);
            return -1;
        }
    }
}
