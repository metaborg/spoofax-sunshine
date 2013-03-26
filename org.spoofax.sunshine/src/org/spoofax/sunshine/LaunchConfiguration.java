package org.spoofax.sunshine;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import org.spoofax.sunshine.framework.language.ALanguage;

public class LaunchConfiguration {
	public boolean as_daemon;
	public int warmup_rounds;
	
	public final Collection<ALanguage> languages = new LinkedList<ALanguage>();
	
	public String project_dir;
	
	/* do not do anything other parsing supported files */
	public boolean doParseOnly;
	
	/* do analyze */
	public boolean doAnalyze;
	
	/* run a builder on a file post analysis */
	public boolean doPostAnalysisBuild;
	public String postAnalysisBuilder;

	
	/* run a pre-analysis builder */
	public boolean doPreAnalysisBuild;
	public String preAnalysisBuilder;
	
	/* the file to target when calling a pre/post analysis builder */
	public File builderTarget;
	public boolean autogit;
	
	@Override
	public String toString() {
		final StringBuffer buf = new StringBuffer("Parameters:\n");
		buf.append("\t Languages: ");
		buf.append(languages);
		buf.append("\n");
		buf.append("Project: ");
		buf.append(this.project_dir);
		buf.append("\n");
		buf.append("\t DAEMON: ");
		buf.append(this.as_daemon);
		buf.append("\n");
		buf.append("\t WARMUPS: ");
		buf.append(this.warmup_rounds);
		buf.append("\n");
		buf.append("\t PARSEONLY: ");
		buf.append(this.doParseOnly);
		buf.append("\n");
		buf.append("\t PREANALYSIS: ");
		buf.append(this.preAnalysisBuilder);
		buf.append("@");
		buf.append(this.builderTarget);
		buf.append("\n");
		buf.append("\t DOANALYZE: ");
		buf.append(this.doAnalyze);
		buf.append("\n");
		buf.append("\t POSTANALYSIS: ");
		buf.append(this.postAnalysisBuilder);
		buf.append("@");
		buf.append(this.builderTarget);
		buf.append("\n");
		buf.append("\t PROJECT: ");
		buf.append(this.project_dir);
		buf.append("\n");
		return buf.toString();
	}


	public void invariant() {
		assert project_dir != null;
		assert !(autogit && as_daemon);
		
		if(doParseOnly){
			assert !doAnalyze;
			assert !doPreAnalysisBuild;
			assert !doPostAnalysisBuild;
		}

		if(doPreAnalysisBuild){
			assert preAnalysisBuilder != null && preAnalysisBuilder.length() > 0;
			assert builderTarget != null;
		}
		
		if(doPostAnalysisBuild){
			assert doAnalyze;
			assert postAnalysisBuilder != null && postAnalysisBuilder.length() > 0;
			assert builderTarget != null;
		}
		
	}
}
