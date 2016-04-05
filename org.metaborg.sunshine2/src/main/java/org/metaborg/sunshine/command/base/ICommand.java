package org.metaborg.sunshine.command.base;

import org.metaborg.core.MetaborgException;

public interface ICommand {
    boolean validate();

    int run() throws MetaborgException;
}
