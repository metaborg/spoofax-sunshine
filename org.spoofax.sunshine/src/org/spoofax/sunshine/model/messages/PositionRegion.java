package org.spoofax.sunshine.model.messages;

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
