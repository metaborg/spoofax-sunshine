package org.metaborg.sunshine.command.base;

import org.metaborg.core.MetaborgException;

public interface ICommand {
    public boolean validate();
    
    public abstract int run() throws MetaborgException;
}
