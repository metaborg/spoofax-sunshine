package org.metaborg.sunshine.command;

import org.metaborg.core.MetaborgException;

import com.beust.jcommander.Parameters;

@Parameters
public class TransformCommand implements ICommand {
    @Override public int run() throws MetaborgException {
        return 0;
    }
}
