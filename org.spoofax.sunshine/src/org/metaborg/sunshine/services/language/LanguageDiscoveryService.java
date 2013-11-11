/**
 * 
 */
package org.metaborg.sunshine.services.language;

import static org.metaborg.sunshine.esvutil.ESVReader.attachedFiles;
import static org.metaborg.sunshine.esvutil.ESVReader.builderIsMeta;
import static org.metaborg.sunshine.esvutil.ESVReader.builderIsOnSource;
import static org.metaborg.sunshine.esvutil.ESVReader.builderName;
import static org.metaborg.sunshine.esvutil.ESVReader.builderTarget;
import static org.metaborg.sunshine.esvutil.ESVReader.builders;
import static org.metaborg.sunshine.esvutil.ESVReader.extensions;
import static org.metaborg.sunshine.esvutil.ESVReader.languageName;
import static org.metaborg.sunshine.esvutil.ESVReader.parseTableName;
import static org.metaborg.sunshine.esvutil.ESVReader.startSymbol;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.sunshine.Environment;
import org.metaborg.sunshine.drivers.SunshineLanguageArguments;
import org.metaborg.sunshine.drivers.SunshineMainArguments;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.io.binary.TermReader;

/**
 * 
 * Singleton service for automatic discovery and registration of {@link ALanguage}. Given a location
 * on the filesystem this service searches for the packed ESV files and attempts to load languages
 * based on the information given in those ESV files. Languages that are discovered are registered
 * into the {@link LanguageService}. Although it is a singleton this service is basically stateless.
 * 
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class LanguageDiscoveryService {
	private static final Logger logger = LogManager.getLogger(LanguageDiscoveryService.class
			.getName());

	private final static String[] ESV_EXTENS = new String[] { "packed.esv" };

	private static LanguageDiscoveryService INSTANCE;

	private LanguageDiscoveryService() {
	}

	public static final LanguageDiscoveryService INSTANCE() {
		if (INSTANCE == null) {
			INSTANCE = new LanguageDiscoveryService();
		}
		return INSTANCE;
	}

	/**
	 * Look for languages inside the given location on the filesystem. Discovered languages are
	 * configured based on the information in the packed ESV files and the configured languages are
	 * automatically loaded onto the {@link LanguageService}.
	 * 
	 * This method expects a particular structure of the repository:
	 * <code>location/[DIR]+/NAME.packed.esv</code>
	 * 
	 * The file paths mentioned in the ESV file are treated as relative to the directory containing
	 * the ESV file.
	 * 
	 * @param location
	 * @return a collection of {@link ALanguage} that have been discovered.
	 */
	public Collection<ALanguage> discover(File location) {
		return discover(location, false);
	}

	/**
	 * @see #discover(File).
	 * 
	 * @param location
	 * @param skipLoading
	 *            If <code>true</code> then language are returned but are not loaded into the
	 *            {@link LanguageService}.
	 * @return
	 */
	public Collection<ALanguage> discover(File location, boolean skipLoading) {
		logger.debug("Auto-discovering languages at location {}", location.getAbsolutePath());
		assert location != null;
		if (!location.exists() || !location.isDirectory() || !location.canRead()) {
			logger.fatal(
					"Language source location is does not exist, is not a directory or cannot be read: {}",
					location.getAbsolutePath());
			throw new RuntimeException(
					"Language source location is does not exist, is not a directory or cannot be read: "
							+ location.getAbsolutePath());
		}

		Collection<ALanguage> languages = new LinkedList<ALanguage>();

		Iterator<File> esvs = FileUtils.iterateFiles(location, ESV_EXTENS, true);
		while (esvs.hasNext()) {
			File esv = esvs.next();
			try {
				logger.debug("Loading ESV {}", esv.getAbsolutePath());
				PushbackInputStream input = new PushbackInputStream(new FileInputStream(esv), 100);
				IStrategoAppl document = tryReadESV(input);
				ALanguage language = languageFromEsv(document, esv.getParentFile().getParentFile());
				languages.add(language);
			} catch (IOException e) {
				throw new RuntimeException("Failed to load language", e);
			}
		}
		if (!skipLoading)
			LanguageService.INSTANCE().registerLanguage(languages);
		return languages;
	}

	/**
	 * Read the textual representation of the ESV ATerm present on the input stream.
	 * 
	 * @see org.strategoxt.imp.runtime.dynamicloading.DescriptorFactory#tryReadTerm for original
	 *      implementation
	 * 
	 * @param input
	 * @return
	 * @throws IOException
	 */
	private static IStrategoAppl tryReadESV(PushbackInputStream input) throws IOException {
		logger.trace("Reading ESV");
		byte[] buffer = new byte[6];
		int bufferSize = input.read(buffer);
		if (bufferSize != -1)
			input.unread(buffer, 0, bufferSize);
		if ((bufferSize == 6 && new String(buffer).equals("Module"))) {
			TermReader reader = new TermReader(
					new TermFactory().getFactoryWithStorageType(IStrategoTerm.MUTABLE));
			return (IStrategoAppl) reader.parseFromStream(input);
		} else {
			return null;
		}
	}

	/**
	 * Creates an {@link ALanguage} from the given {@link SunshineLanguageArguments}.
	 * 
	 * @param args
	 * @return
	 */
	public ALanguage languageFromArguments(SunshineLanguageArguments args) {
		String[] extens = args.extens.toArray(new String[args.extens.size()]);

		int numJars = args.jars.size();
		int numCtrees = args.ctrees.size();
		File[] compilerFiles = new File[numJars + numCtrees];
		for (int i = 0; i < (numJars + numCtrees); i++) {
			if (i < numJars)
				compilerFiles[i] = new File(args.jars.get(i));
			else
				compilerFiles[i] = new File(args.ctrees.get(i - numJars));
		}
		SunshineMainArguments mainArgs = Environment.INSTANCE().getMainArguments();
		ALanguage language = new Language(args.lang, extens, args.ssymb, new File(args.tbl),
				args.observer, compilerFiles);
		language.registerBuilder(mainArgs.builder, mainArgs.builder, mainArgs.buildonsource, false);
		return language;
	}

	/**
	 * Creates a {@link ALanguage} from the given ATerm contents of the packed ESV file describing
	 * it.
	 * 
	 * @param document
	 * @return
	 */
	public ALanguage languageFromEsv(IStrategoAppl document, File basepath) {
		logger.trace("Configuring language from ESV");
		String name = languageName(document);
		String[] extens = extensions(document);
		Set<File> codefiles = attachedFiles(document, basepath);
		String startsymb = startSymbol(document);
		File parsetbl = new File(basepath, parseTableName(document));
		// String observer = observerFunction(document);
		logger.warn("Using default cli observer {}", "analysis-default-cmd");
		String observer = "analysis-default-cmd";

		Collection<IStrategoAppl> builders = builders(document);

		ALanguage language = new Language(name, extens, startsymb, parsetbl, observer,
				codefiles.toArray(new File[codefiles.size()]));

		for (IStrategoAppl action : builders) {
			language.registerBuilder(builderName(action), builderTarget(action),
					builderIsOnSource(action), builderIsMeta(action));
		}

		return language;
	}

}
