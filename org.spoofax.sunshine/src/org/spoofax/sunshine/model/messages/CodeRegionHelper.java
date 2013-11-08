package org.spoofax.sunshine.model.messages;

import java.util.Arrays;

public class CodeRegionHelper {

	public static final char DAMAGE = '^';
	public static final char BLANK = ' ';
	public static final char TAB = '\t';
	public static final char NEWLINE = '\n';

	public static String[] getAffectedLines(String input, int beginLine, int endLine) {
		return Arrays.copyOfRange(input.split("\\r?\\n"), beginLine - 1, endLine);
	}

	public static String[] weaveDamageLines(String[] lines, int beginColumn, int endColumn) {
		String[] damagedLines = new String[lines.length * 2];
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			damagedLines[i] = line;
			int beginOffset = i == 0 ? beginColumn - 1 : 0;
			int endOffset = i + 1 == lines.length ? endColumn - 1 : line.length();
			char[] damageChars = line.toCharArray();
			for (int j = 0; j < damageChars.length; j++) {
				if (beginOffset <= j && endOffset >= j) {
					damageChars[j] = DAMAGE;
				} else {
					char dc = damageChars[j];
					if (dc != TAB && dc != BLANK) {
						dc = BLANK;
					}
					damageChars[j] = dc;
				}
			}
			damagedLines[i + 1] = new String(damageChars);
		}
		return damagedLines;
		// for (int i = 0; i < damagedLines.length; i++) {
		// if (i % 2 == 0) {
		// damagedLines[i] = indentation + affectedLines[i / 2];
		// } else {
		// int io = (i - 1) / 2;
		// if (row - 1 <= i && endrow - 1 >= i) {
		// String affectedLine = affectedLines[(i - 1) / 2];
		// int beginDamageOffset = 0;
		// int endDamageOffset = affectedLine.length();
		// if (row - 1 == i) {
		// beginDamageOffset = column;
		// }
		// if (endrow - 1 == i) {
		// endDamageOffset = endcolumn;
		// }
		// char[] damageChars = affectedLine.toCharArray();
		// for (int j = 0; j < damageChars.length; j++) {
		// if (j >= beginDamageOffset && j <= endDamageOffset) {
		// damageChars[j] = DAMAGE;
		// } else {
		// char cc = damageChars[j];
		// if (cc != TAB && cc != BLANK) {
		// damageChars[j] = BLANK;
		// }
		// }
		// }
		// // in damage region
		// damagedLines[i] = indentation + new String(damageChars) + "| foo";
		// } else {
		// damagedLines[i] = EMPTY + "| foo";
		// }
		// }
		// }
		// StringBuilder sb = new StringBuilder();
		// for (String s : damagedLines) {
		// sb.append(s + NEWLINE);
		// }
		// return sb.toString();
	}
}
