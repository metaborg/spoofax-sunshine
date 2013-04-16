package org.spoofax.sunshine.gitdrive;

import com.beust.jcommander.Parameter;

/**
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class GitRunArguments {

    @Parameter(names = "--from", description = "<HASH> Start at commit HASH")
    String fromCommit;

    @Parameter(names = "--to", description = "<HASH> Stop at commit HASH")
    String toCommit;

}
