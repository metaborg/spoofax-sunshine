package org.spoofax.sunshine.model.messages;

import org.spoofax.jsglr.client.imploder.IToken;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class PositionRegion extends ARegion {

	private int row, column, endrow, endcolumn;

	public PositionRegion(int row, int column, int endrow, int endcolumn) {
		this.row = row;
		this.column = column;
		this.endrow = endrow;
		this.endcolumn = endcolumn;
	}

	public static PositionRegion fromTokens(IToken left, IToken right) {
		boolean leftDone = false, rightDone = false;
		int leftLine = 0, leftColumn = 0, rightLine = 0, rightColumn = 0;

		char[] input = left.getTokenizer().getInput().toCharArray();
		int currentLine = 0;
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
			if (!rightDone && i == right.getStartOffset()) {
				rightLine = currentLine;
				rightColumn = currentColumn;
			}
			if (rightDone && leftDone) {
				break;
			}
		}
		return new PositionRegion(leftLine, leftColumn, rightLine, rightColumn);
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

	@Override
	public boolean equals(Object o) {
		if (o instanceof PositionRegion) {
			PositionRegion or = (PositionRegion) o;
			return row == or.row && column == or.column && endrow == or.endrow
					&& endcolumn == or.endcolumn;
		}
		return false;
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

}
