package spoofax.core.cmd.command;

import org.metaborg.core.MetaborgException;

public interface ICommand {
    boolean validate();

    int run() throws MetaborgException;
}
