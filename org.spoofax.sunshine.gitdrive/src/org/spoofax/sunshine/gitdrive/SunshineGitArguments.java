package org.spoofax.sunshine.gitdrive;

import java.util.ArrayList;
import java.util.List;

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

	@Parameter(names = "--skip", description = "<HASH>+ Skip the listed commits (space separated)")
	List<String> skipCommits = new ArrayList<String>();

	@Parameter(names = "--with-lib", description = "[PATH] A folder contents of which will be copied into the project prior to the rest of the work.")
	public String withlib;
	
	@ParametersDelegate
	SunshineMainArguments sunshineArgs = new SunshineMainArguments();

	@Override
	public String toString() {
		String s = "";
		s += sunshineArgs.toString();
		s += "From commit: " + fromCommit + "\n";
		s += "To commit: " + toCommit + "\n";
		s += "To skip: " + skipCommits + "\n";
		return s;
	}
}
