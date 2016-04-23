package nces.structure;

import nces.misc.Grid;
import nces.misc.Num;

public class Transition extends NcesObject implements NamedAndPositionedObject {
	public final int x;
	public final int y;
	public final int num;
	public final int locNum;
	public final String name;
	public final boolean isAnd;
	
	public Transition(int x, int y, int num, int locNum, boolean isAnd) {
		this.x = x;
		this.y = y;
		this.num = num;
		this.locNum = locNum;
		name = "t" + num;
		this.isAnd = isAnd;
	}
	
	public Transition(Grid grid, Num num, boolean isAnd, NCES nces, String name) {
		if (isAnd) {
			throw new RuntimeException("Are you sure?");
		}
		this.x = grid.x();
		this.y = grid.y();
		grid.increment();
		this.num = num.num();
		this.locNum = num.num();
		num.increment();
		this.name = name;
		this.isAnd = isAnd;
		nces.addTransition(this);
	}
	
	@Override
	public String toString() {
		return "<trans X=" + quote(x) + " Y=" + quote(y) + " Width=" + quote(30)
				+ " Height=" + quote(30) + " Num=" + quote(num) + " LocNum=" + quote(locNum)
				+ " Name=" + quote(name) + " Type=" + quote(isAnd ? "AND" : "OR")
				+ " TransInscription=" + quote("_") + " SwitchMode=" + quote("s")
				+ " Comment=" + quote("_")
				+ ">" + COMMENT_BOX + "</trans>";
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
