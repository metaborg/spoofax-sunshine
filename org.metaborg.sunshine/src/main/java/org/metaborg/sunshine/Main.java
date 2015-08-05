package org.metaborg.sunshine;

import java.util.Map;
import java.util.Map.Entry;

import org.metaborg.core.MetaborgException;
import org.metaborg.spoofax.meta.core.SpoofaxMetaModule;
import org.metaborg.sunshine.command.ICommand;
import org.metaborg.sunshine.command.arguments.CommonArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public final CommonArguments arguments;
    public final Map<String, ICommand> commands;


    public static void main(String[] args) {
        final Injector baseInjector = Guice.createInjector(new SunshineModule());
        final Injector injector = baseInjector.createChildInjector(new SpoofaxMetaModule());
        final Main main = injector.getInstance(Main.class);
        final int result = main.run(args);
        System.exit(result);
    }


    @Inject public Main(CommonArguments arguments, Map<String, ICommand> commands) {
        this.arguments = arguments;
        this.commands = commands;
    }


    public int run(String[] args) {
        final JCommander jc = new JCommander(arguments);
        for(Entry<String, ICommand> entry : commands.entrySet()) {
            jc.addCommand(entry.getKey(), entry.getValue());
        }

        try {
            jc.parse(args);
        } catch(ParameterException e) {
            logger.error(e.getMessage());
            jc.usage();
            return -1;
        }

        final ICommand command = commands.get(jc.getParsedCommand());
        if(command == null) {
            logger.error("No command was specified");
            jc.usage();
            return -1;
        }

        if(!command.validate()) {
            jc.usage();
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
