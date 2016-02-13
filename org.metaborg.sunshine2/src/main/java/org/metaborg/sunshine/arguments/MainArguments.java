package org.metaborg.sunshine.arguments;

import com.beust.jcommander.Parameter;

public class MainArguments {
    @Parameter(names = { "-h", "--help" }, description = "Shows this usage information", help = true) public boolean help;

    @Parameter(names = { "--exit" }, description = "Immediately exit, used for testing purposes", hidden = true) public boolean exit;
}
