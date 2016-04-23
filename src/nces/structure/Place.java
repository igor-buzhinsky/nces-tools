package nces.structure;

import nces.misc.Grid;
import nces.misc.Num;

public class Place extends NcesObject implements NamedAndPositionedObject {
	public final int x;
	public final int y;
	public final String name;
	public final int num;
	public final int locNum;
	public final boolean marked;
	
	public Place(int x, int y, String name, int num, int locNum, boolean marked) {
		this.x = x;
		this.y = y;
		this.name = name;
		this.num = num;
		this.locNum = locNum;
		this.marked = marked;
	}
	
	public Place(Grid grid, String name, Num num, boolean marked, NCES nces) {
		this.x = grid.x();
		this.y = grid.y();
		grid.increment();
		this.name = name;
		this.num = num.num();
		this.locNum = num.num();
		num.increment();
		this.marked = marked;
		nces.addPlace(this);
	}
	
	@Override
	public String toString() {
		return "<place X=" + quote(x) + " Y=" + quote(y) + " Diameter=" + quote(30.0)
				+ " Name=" + quote(name) + " Num=" + quote(num) + " LocNum=" + quote(locNum)
				+ " Mark=" + quote(marked ? 1 : 0) + " Clock=" + quote(0)
				+ " Capacity=" + quote(1) + " Comment=" + quote("_")
				+ ">" + COMMENT_BOX + "</place>";
	}

	@Override
	public String name() {
		return name;
	}
	

	@Override
	public int x() {
		return x;
	}

	@Override
	public int y() {
		return y;
	}
}
