package spoofax.core.cmd;

import java.util.Map;
import java.util.Map.Entry;

import org.metaborg.core.MetaborgException;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.inject.Inject;

import spoofax.core.cmd.command.ICommand;
import spoofax.core.cmd.parameter.MainParameters;

public class Runner {
    private static final ILogger logger = LoggerUtils.logger(Runner.class);

    private final Map<String, ICommand> commands;
    private final MainParameters mainParameters;


    @Inject public Runner(Map<String, ICommand> commands, MainParameters mainParameters) {
        this.commands = commands;
        this.mainParameters = mainParameters;
    }


    public int run(String[] args) {
        final JCommander jc = new JCommander(mainParameters);
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

        if(mainParameters.help) {
            final StringBuilder sb = new StringBuilder();
            jc.usage(sb);
            System.err.println(sb.toString());
            return 0;
        }

        if(mainParameters.exit) {
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
