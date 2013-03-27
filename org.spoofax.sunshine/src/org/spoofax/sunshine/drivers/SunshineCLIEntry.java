/**
 * 
 */
package org.spoofax.sunshine.drivers;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import org.spoofax.sunshine.CompilerException;
import org.spoofax.sunshine.LaunchConfiguration;
import org.spoofax.sunshine.SunshineMainDriver;
import org.spoofax.sunshine.framework.language.AdHocJarBasedLanguage;

/**
 * @author Vlad Vergu
 * 
 */
public class SunshineCLIEntry {

	private final static String DBG_WARM = "--warmup";
	private final static String LANG_JAR = "--lang-jar";
	private final static String LANG_TBL = "--lang-tbl";
	private final static String LANG_SSYMB = "--start-symbol";
	private final static String PROJ_DIR = "--proj-dir";
	private final static String PARSE_ONLY = "--pao";
	private final static String EXT_ENS = "--extens";
	private final static String LANG_NAME = "--lang-name";
	private final static String MOD_DAEMON = "--daemon";
	private final static String CALL_BUILDER = "--builder";
	private final static String BUILD_ON = "--build-on";

	// private static final String observer_fun = "editor-analyze";

	/**
	 * @param args
	 * @throws CompilerException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws CompilerException, InterruptedException {
		SunshineMainDriver driver = new SunshineMainDriver(parseArgs(args));
		driver.run();
		System.exit(0);
	}

	private static LaunchConfiguration parseArgs(String[] args) throws IllegalArgumentException {
		boolean lang_jar_next = false;
		boolean lang_tbl_next = false;
		boolean proj_dir_next = false;
		boolean extens_next = false;
		boolean lang_name_next = false;
		boolean lang_startsymb_next = false;
		boolean dbg_warmups_next = false;
		boolean call_builder_next = false;
		boolean build_on_next = false;

		boolean parse_only = false;
		boolean daemon = false;
		Collection<String> language_jars = new LinkedList<String>();
		String language_tbl = null;
		String project_dir = null;
		Collection<String> extens = new LinkedList<String>();
		String langname = null;
		String language_startsymb = null;
		String builderName = null;
		String buildOnTarget = null;
		int warmups = 0;
		for (String a : args) {
			if (a.equals(LANG_JAR)) {
				lang_jar_next = true;
				lang_tbl_next = false;
				proj_dir_next = false;
				extens_next = false;
				lang_name_next = false;
				lang_startsymb_next = false;
				dbg_warmups_next = false;
				call_builder_next = false;
				build_on_next = false;
			} else if (a.equals(LANG_TBL)) {
				lang_jar_next = false;
				lang_tbl_next = true;
				proj_dir_next = false;
				extens_next = false;
				lang_name_next = false;
				lang_startsymb_next = false;
				dbg_warmups_next = false;
				call_builder_next = false;
				build_on_next = false;
			} else if (a.equals(LANG_NAME)) {
				lang_jar_next = false;
				lang_tbl_next = false;
				proj_dir_next = false;
				extens_next = false;
				lang_name_next = true;
				lang_startsymb_next = false;
				dbg_warmups_next = false;
				call_builder_next = false;
				build_on_next = false;
			} else if (a.equals(PROJ_DIR)) {
				lang_jar_next = false;
				lang_tbl_next = false;
				proj_dir_next = true;
				extens_next = false;
				lang_name_next = false;
				lang_startsymb_next = false;
				dbg_warmups_next = false;
				call_builder_next = false;
				build_on_next = false;
			} else if (a.equals(EXT_ENS)) {
				lang_jar_next = false;
				lang_tbl_next = false;
				proj_dir_next = false;
				extens_next = true;
				lang_name_next = false;
				lang_startsymb_next = false;
				dbg_warmups_next = false;
				call_builder_next = false;
				build_on_next = false;
			} else if (a.equals(LANG_SSYMB)) {
				lang_jar_next = false;
				lang_tbl_next = false;
				proj_dir_next = false;
				extens_next = false;
				lang_name_next = false;
				lang_startsymb_next = true;
				dbg_warmups_next = false;
				call_builder_next = false;
				build_on_next = false;
			} else if (a.equals(DBG_WARM)) {
				lang_jar_next = false;
				lang_tbl_next = false;
				proj_dir_next = false;
				extens_next = false;
				lang_name_next = false;
				lang_startsymb_next = false;
				dbg_warmups_next = true;
				call_builder_next = false;
				build_on_next = false;
			} else if (a.equals(PARSE_ONLY)) {
				lang_jar_next = false;
				lang_tbl_next = false;
				proj_dir_next = false;
				extens_next = false;
				lang_name_next = false;
				parse_only = true;
				lang_startsymb_next = false;
				dbg_warmups_next = false;
				call_builder_next = false;
				build_on_next = false;
			} else if (a.equals(MOD_DAEMON)) {
				lang_jar_next = false;
				lang_tbl_next = false;
				proj_dir_next = false;
				extens_next = false;
				lang_name_next = false;
				daemon = true;
				lang_startsymb_next = false;
				dbg_warmups_next = false;
				call_builder_next = false;
				build_on_next = false;
			} else if (a.equals(CALL_BUILDER)) {
				lang_jar_next = false;
				lang_tbl_next = false;
				proj_dir_next = false;
				extens_next = false;
				lang_name_next = false;
				lang_startsymb_next = false;
				dbg_warmups_next = false;
				call_builder_next = true;
				build_on_next = false;
			} else if (a.equals(BUILD_ON)) {
				lang_jar_next = false;
				lang_tbl_next = false;
				proj_dir_next = false;
				extens_next = false;
				lang_name_next = false;
				lang_startsymb_next = false;
				dbg_warmups_next = false;
				call_builder_next = false;
				build_on_next = true;
			} else {
				if (lang_jar_next) {
					language_jars.add(a);
				} else if (lang_tbl_next) {
					language_tbl = a;
				} else if (proj_dir_next) {
					project_dir = a;
				} else if (extens_next) {
					extens.add(a);
				} else if (lang_name_next) {
					langname = a;
				} else if (lang_startsymb_next) {
					language_startsymb = a;
				} else if (dbg_warmups_next) {
					warmups = Integer.parseInt(a);
				} else if (call_builder_next) {
					builderName = a;
				} else if (build_on_next) {
					buildOnTarget = a;
				}
			}
		}
		if (language_jars.isEmpty()) {
			throw new IllegalArgumentException("Missing --lang-jar argument");
		} else if (language_tbl == null) {
			throw new IllegalArgumentException("Missing --lang-tbl argument");
		} else if (project_dir == null) {
			throw new IllegalArgumentException("Missing --proj-dir argument");
		} else if (extens.isEmpty()) {
			throw new IllegalArgumentException("Missing --extens argument");
		} else if (langname == null) {
			throw new IllegalArgumentException("Missing --lang-name argument");
		} else if (language_startsymb == null) {
			throw new IllegalArgumentException("Missing --start-symbol argument");
		} else if (builderName != null && buildOnTarget == null) {
			throw new IllegalArgumentException("Missing --build-on argument");
		} else if (builderName == null && buildOnTarget != null) {
			throw new IllegalArgumentException("Missing --builder argument");
		} else if (builderName != null && warmups > 0) {
			throw new IllegalArgumentException("Cannot warm up when calling a builder");
		} else if (parse_only && builderName != null) {
			throw new IllegalArgumentException("Parse only is incompatible with running a builder");
		}

		LaunchConfiguration config = new LaunchConfiguration();
		final Collection<File> jars = new LinkedList<File>();
		for (String fn : language_jars) {
			jars.add(new File(fn));
		}
		config.languages.add(new AdHocJarBasedLanguage(langname, extens.toArray(new String[extens.size()]),
				language_startsymb, new File(language_tbl), "editor-analyze", jars.toArray(new File[jars.size()])));
		config.as_daemon = daemon;
		config.project_dir = project_dir;
		config.doParseOnly = parse_only;
		config.doPreAnalysisBuild = builderName != null;
		config.preAnalysisBuilder = builderName;
		if (buildOnTarget != null)
			config.builderTarget = new File(buildOnTarget);
		config.doAnalyze = !parse_only;
		config.invariant();
		return config;
	}
}
