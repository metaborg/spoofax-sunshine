/**
 * 
 */
package org.spoofax.sunshine;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.spoofax.sunshine.framework.language.AdHocJarBasedLanguage;
import org.spoofax.sunshine.framework.messages.IMessage;
import org.spoofax.sunshine.framework.services.AnalysisService;
import org.spoofax.sunshine.framework.services.FileMonitoringService;
import org.spoofax.sunshine.framework.services.LanguageService;
import org.spoofax.sunshine.framework.services.MessageService;
import org.spoofax.sunshine.framework.services.ParseService;
import org.spoofax.sunshine.framework.services.QueableAnalysisService;

/**
 * @author Vlad Vergu
 * 
 */
public class Sunshine {

	private final static String LANG_JAR = "--lang-jar";
	private final static String LANG_TBL = "--lang-tbl";
	private final static String PROJ_DIR = "--proj-dir";
	private final static String PARSE_ONLY = "--pao";
	private final static String EXT_ENS = "--extens";
	private final static String LANG_NAME = "--lang-name";
	private final static String MOD_DAEMON = "--daemon";
	private static final String observer_fun = "editor-analyze";

	private final List<String> extens = new LinkedList<String>();
	private final List<String> language_jars = new LinkedList<String>();;
	private String language_tbl;
	private String langname;
	private String project_dir;
	private boolean parse_only;

	private boolean daemon;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final Sunshine front = new Sunshine();
		front.parseArgs(args);
		front.initialize();
//		front.warmup();
		front.work();
	}

	private void warmup() {
		System.out.println("Warming up");
		long begin = 0;
		long end = 0;
		for (int i = 10; i > 0; i--) {
			begin = System.currentTimeMillis();
			final Collection<File> files = FileMonitoringService.INSTANCE().getChangesNoPersist();
			if (parse_only) {
				parse(files);
			} else {
				analyze(files);
			}
			MessageService.INSTANCE().clearMessages();
			end = System.currentTimeMillis();
		}
		MessageService.INSTANCE().clearMessages();
		System.out.println("Warm up completed. Last duration: " + (end - begin) + " ms");
	}

	private void work() {
		Scanner sc = new Scanner(System.in);
		do {
			final Collection<File> files = FileMonitoringService.INSTANCE().getChanges();
			System.out.println("Processing " + files.size() + " changed files:");
			for (File file : files) {
				System.out.println("\t " + file.getPath());
			}
			if (parse_only) {
				parse(files);
			} else {
				analyze(files);
			}
			final Collection<IMessage> msgs = MessageService.INSTANCE().getMessages();
			System.out.println("Completed. " + msgs.size() + " messages produced.");
			for (IMessage msg : msgs) {
				System.err.println(msg);
			}
			MessageService.INSTANCE().clearMessages();
		} while (daemon && sc.nextLine() != null);
	}

	private void parse(Collection<File> files) {
		for (File f : files) {
			ParseService.INSTANCE().parse(f);
		}
	}

	private void analyze(Collection<File> files) {
		QueableAnalysisService.INSTANCE().enqueueAnalysis(files);
		QueableAnalysisService.INSTANCE().analyzeQueue();
	}

	private void initialize() {
		Environment.INSTANCE().setProjectDir(new File(project_dir));
		final Collection<File> jars = new LinkedList<File>();
		for (String fn : language_jars) {
			jars.add(new File(fn));
		}
		final AdHocJarBasedLanguage lang = new AdHocJarBasedLanguage(langname, extens.toArray(new String[0]), "Start",
				new File(language_tbl), observer_fun, jars.toArray(new File[0]));
		LanguageService.INSTANCE().registerLanguage(lang);
	}

	private void parseArgs(String[] args) throws IllegalArgumentException {
		boolean lang_jar_next = false;
		boolean lang_tbl_next = false;
		boolean proj_dir_next = false;
		boolean extens_next = false;
		boolean lang_name_next = false;

		List<String> lang_jars = new LinkedList<String>();
		String lang_tbl = null;
		String projdir = null;

		for (String a : args) {
			if (a.equals(LANG_JAR)) {
				lang_jar_next = true;
				lang_tbl_next = false;
				proj_dir_next = false;
				extens_next = false;
				lang_name_next = false;
			} else if (a.equals(LANG_TBL)) {
				lang_jar_next = false;
				lang_tbl_next = true;
				proj_dir_next = false;
				extens_next = false;
				lang_name_next = false;
			} else if (a.equals(LANG_NAME)) {
				lang_jar_next = false;
				lang_tbl_next = false;
				proj_dir_next = false;
				extens_next = false;
				lang_name_next = true;
			} else if (a.equals(PROJ_DIR)) {
				lang_jar_next = false;
				lang_tbl_next = false;
				proj_dir_next = true;
				extens_next = false;
				lang_name_next = false;
			} else if (a.equals(EXT_ENS)) {
				lang_jar_next = false;
				lang_tbl_next = false;
				proj_dir_next = false;
				extens_next = true;
				lang_name_next = false;
			} else if (a.equals(PARSE_ONLY)) {
				lang_jar_next = false;
				lang_tbl_next = false;
				proj_dir_next = false;
				extens_next = false;
				lang_name_next = false;
				this.parse_only = true;
			} else if (a.equals(MOD_DAEMON)) {
				lang_jar_next = false;
				lang_tbl_next = false;
				proj_dir_next = false;
				extens_next = false;
				lang_name_next = false;
				this.daemon = true;
			} else {
				if (lang_jar_next) {
					lang_jars.add(a);
				} else if (lang_tbl_next) {
					lang_tbl = a;
				} else if (proj_dir_next) {
					projdir = a;
				} else if (extens_next) {
					extens.add(a);
				} else if (lang_name_next) {
					langname = a;
				}
			}
		}
		if (lang_jars.isEmpty()) {
			throw new IllegalArgumentException("Missing --lang-jar argument");
		} else if (lang_tbl == null) {
			throw new IllegalArgumentException("Missing --lang-tbl argument");
		} else if (projdir == null) {
			throw new IllegalArgumentException("Missing --proj-dir argument");
		} else if (extens.isEmpty()) {
			throw new IllegalArgumentException("Missing --extens argument");
		} else if (langname == null) {
			throw new IllegalArgumentException("Missing --lang-name argument");
		}

		this.language_jars.addAll(lang_jars);
		this.language_tbl = lang_tbl;
		this.project_dir = projdir;

		System.out.println("Parameters:");
		System.out.println("\t JARS: " + this.language_jars);
		System.out.println("\t TBL: " + this.language_tbl);
		System.out.println("\t PROJ: " + this.project_dir);
		System.out.println("\t DAEMON: " + this.daemon);
		System.out.println("---------------------------------");
	}
}
