package org.metaborg.sunshine.model.messages;

import org.spoofax.jsglr.client.imploder.IToken;

/**
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class CodeRegion {
	public static final String COMMA = ",";
	public static final String COLON = ":";
	public static final String EMPTY = "";

	private int row, column, endrow, endcolumn;
	private String[] affectedLines;

	public CodeRegion(int row, int column, int endrow, int endcolumn, String input) {
		this.row = row;
		this.column = column;
		this.endrow = endrow;
		this.endcolumn = endcolumn;
		if (input != null)
			this.affectedLines = CodeRegionHelper.getAffectedLines(input, row, endrow);
	}

	public static CodeRegion fromTokens(IToken left, IToken right) {
		boolean leftDone = false, rightDone = false;
		int leftLine = 0, leftColumn = 0, rightLine = 0, rightColumn = 0;
		char[] input = left.getTokenizer().getInput().toCharArray();
		int currentLine = 1;
		int currentColumn = 0;
		for (int i = 0; i < input.length; i++) {
			char c = input[i];
			if (c == '\n' || c == '\r') {
				currentLine++;
				currentColumn = 0;
			} else {
				currentColumn++;
			}

			if (!leftDone && i == left.getStartOffset()) {
				leftLine = currentLine;
				leftColumn = currentColumn;
			}
			if (!rightDone && i == right.getEndOffset()) {
				rightLine = currentLine;
				rightColumn = currentColumn;
			}
			if (rightDone && leftDone) {
				break;
			}
		}
		return new CodeRegion(leftLine, leftColumn, rightLine, rightColumn, left.getTokenizer()
				.getInput());
	}

	@Override
	public String toString() {
		final StringBuilder str = new StringBuilder();
		str.append(row);
		str.append(COMMA);
		str.append(column);
		str.append(COLON);
		str.append(endrow);
		str.append(COMMA);
		str.append(endcolumn);
		return str.toString();
	}

	public int getRow() {
		return row;
	}

	public int getColumn() {
		return column;
	}

	public int getEndRow() {
		return endrow;
	}

	public int getEndColumn() {
		return endcolumn;
	}

	public String getDamagedRegion(String indentation) {
		if (affectedLines == null)
			return "" + CodeRegionHelper.NEWLINE;

		String[] damagedLines = CodeRegionHelper.weaveDamageLines(affectedLines, column, endcolumn);
		StringBuilder sb = new StringBuilder();
		for (String dl : damagedLines) {
			sb.append(indentation + dl + CodeRegionHelper.NEWLINE);
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final StringBuilder hashString = new StringBuilder();
		hashString.append(row);
		hashString.append(column);
		hashString.append(endrow);
		hashString.append(endcolumn);
		return hashString.toString().hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof CodeRegion) {
			CodeRegion or = (CodeRegion) o;
			return row == or.row && column == or.column && endrow == or.endrow
					&& endcolumn == or.endcolumn;
		}
		return false;
	}
}
