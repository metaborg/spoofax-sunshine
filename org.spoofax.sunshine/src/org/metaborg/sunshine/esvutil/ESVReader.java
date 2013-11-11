package org.metaborg.sunshine.esvutil;

import static org.spoofax.interpreter.terms.IStrategoTerm.APPL;
import static org.spoofax.interpreter.terms.IStrategoTerm.STRING;
import static org.spoofax.terms.Term.tryGetName;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Term reading utility class, extending {@link org.spoofax.interpreter.core.Tools} with new methods
 * specific for reading descriptors.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 */
public class ESVReader extends Tools {
	private ESVReader() {
		// TermReader cannot be constructed
	}

	public static IStrategoAppl findTerm(IStrategoTerm term, String constructor) {
		if (term.getTermType() == IStrategoTerm.APPL && cons(term).equals(constructor))
			return (IStrategoAppl) term;

		IStrategoTerm[] subterms = term.getAllSubterms();
		for (int i = subterms.length - 1; i >= 0; i--) {
			IStrategoAppl result = findTerm(subterms[i], constructor);
			if (result != null)
				return result;
		}

		return null;
	}

	public static ArrayList<IStrategoAppl> collectTerms(IStrategoAppl term, String... constructors) {
		ArrayList<IStrategoAppl> results = new ArrayList<IStrategoAppl>();
		for (String constructor : constructors) {
			collectTerms(term, constructor, results);
		}
		return results;
	}

	private static void collectTerms(IStrategoTerm term, String constructor,
			ArrayList<IStrategoAppl> results) {
		if (term.getTermType() == IStrategoTerm.APPL && cons(term).equals(constructor))
			results.add((IStrategoAppl) term);

		// TODO: optimize: use TermVisitor, avoid indexed access to long lists
		for (int i = 0; i < term.getSubtermCount(); i++) {
			collectTerms(termAt(term, i), constructor, results);
		}
	}

	public static String termContents(IStrategoTerm t) {
		if (t == null)
			return null;

		String result;

		if (t.getTermType() == STRING) {
			result = asJavaString(t);
		} else if (t.getSubtermCount() == 1 && "Values".equals(tryGetName(t))) {
			return concatTermStrings(listAt(t, 0));
		} else if (t.getTermType() == APPL && t.getSubtermCount() == 1
				&& termAt(t, 0).getTermType() == STRING) {
			result = asJavaString(termAt(t, 0));
		} else if (t.getTermType() == APPL && t.getSubtermCount() == 1) {
			return termContents(termAt(t, 0));
		} else {
			return null;
		}

		if (result.startsWith("\"") && result.endsWith("\"") && result.length() > 1)
			result = result.substring(1, result.length() - 1).replace("\\\\", "\"");

		return result;
	}

	public static String concatTermStrings(IStrategoList values) {
		StringBuilder results = new StringBuilder();

		if (values.getSubtermCount() > 0)
			results.append(termContents(termAt(values, 0)));

		for (int i = 1; i < values.getSubtermCount(); i++) {
			results.append(',');
			results.append(termContents(termAt(values, i)));
		}
		return results.toString();
	}

	public static int parseIntAt(IStrategoTerm t, int index) {
		return Integer.parseInt(termContents(t.getSubterm(index)));
	}

	public static String cons(IStrategoTerm t) {
		if (t == null || t.getTermType() != APPL)
			return null;
		return ((IStrategoAppl) t).getConstructor().getName();
	}

	public static String observerFunction(IStrategoAppl document) {
		IStrategoAppl observer = findTerm(document, "SemanticObserver");
		String observerFunction = termContents(termAt(observer, 0));
		return observerFunction;
	}

	public static String startSymbol(IStrategoAppl document) {
		IStrategoAppl result = findTerm(document, "StartSymbols");
		if (result == null)
			return null;

		return termContents(termAt(termAt(result, 0), 0));
	}

	public static String parseTableName(IStrategoAppl document) {
		String file = getProperty(document, "Table", getProperty(document, "LanguageName"));
		if (!file.endsWith(".tbl"))
			file += ".tbl";
		return file;
	}

	public static Set<File> attachedFiles(IStrategoAppl document, File basepath) {
		Set<File> attachedFiles = new LinkedHashSet<File>(); // (linked: must maintain jar order)

		for (IStrategoAppl s : collectTerms(document, "SemanticProvider")) {
			attachedFiles.add(new File(basepath, termContents(s)));
		}

		return attachedFiles;
	}

	public static String languageName(IStrategoAppl document) {
		return getProperty(document, "LanguageName");
	}

	public static String[] extensions(IStrategoAppl document) {
		return getProperty(document, "Extensions").split(",");
	}

	public static Collection<IStrategoAppl> builders(IStrategoAppl document) {
		return collectTerms(document, "Action");
	}

	public static String builderName(IStrategoAppl action) {
		assert action.getName().equals("Action");
		assert action.getSubtermCount() == 3;

		return asJavaString(action.getSubterm(0).getSubterm(0)).replace("\\", "").replace("\"", "");
	}

	public static String builderTarget(IStrategoAppl action) {
		assert action.getConstructor().getName().equals("Action");
		assert action.getConstructor().getArity() == 3;
		return asJavaString(action.getSubterm(1).getSubterm(0));
	}

	public static boolean builderIsOpenEditor(IStrategoAppl action) {
		assert action.getConstructor().getName().equals("Action");
		assert action.getConstructor().getArity() == 3;

		return builderAnnos(action).contains("OpenEditor");
	}

	public static boolean builderIsMeta(IStrategoAppl action) {
		assert action.getConstructor().getName().equals("Action");
		assert action.getConstructor().getArity() == 3;
		return builderAnnos(action).contains("Meta");
	}

	public static boolean builderIsOnSource(IStrategoAppl action) {
		assert action.getConstructor().getName().equals("Action");
		assert action.getConstructor().getArity() == 3;

		return builderAnnos(action).contains("Source");
	}

	public static Collection<String> builderAnnos(IStrategoAppl action) {
		assert action.getConstructor().getName().equals("Action");
		assert action.getConstructor().getArity() == 3;
		Collection<String> annos = new LinkedList<>();
		IStrategoList annoterm = (IStrategoList) action.getSubterm(2);
		for (IStrategoTerm anno : annoterm) {
			annos.add(((IStrategoAppl) anno).getName());
		}
		return annos;
	}

	public static String getProperty(IStrategoAppl document, String name) {
		return getProperty(document, name, null);
	}

	public static String getProperty(IStrategoAppl document, String name, String defaultValue) {
		IStrategoAppl result = findTerm(document, name);
		if (result == null)
			return defaultValue;

		return termContents(result);
	}

}
