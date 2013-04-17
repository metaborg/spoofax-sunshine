package org.spoofax.sunshine.gitdrive;

import org.spoofax.sunshine.drivers.SunshineMainArguments;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

/**
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class SunshineGitArguments {

    @Parameter(names = "--from", description = "<HASH> Start at commit HASH")
    String fromCommit;

    @Parameter(names = "--to", description = "<HASH> Stop at commit HASH")
    String toCommit;

    @ParametersDelegate
    SunshineMainArguments sunshineArgs = new SunshineMainArguments();

    @Override
    public String toString() {
	String s = "";
	s += sunshineArgs.toString();
	s += "From commit: " + fromCommit + "\n";
	s += "To commit: " + toCommit + "\n";
	return s;
    }
}
