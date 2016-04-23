package nces.structure;

import nces.misc.Num;

public class IOConnection extends NcesObject implements NamedAndPositionedObject {
	public final int x;
	public final int y;
	public final int num;
	public final int locNum;
	public final String name;
	public final IOType type;
	
	public enum IOType {
		INPUT_EVENT, INPUT_CONDITION, OUTPUT_EVENT, OUTPUT_CONDITION;
		
		public String declaration() {
			return this == INPUT_EVENT || this == OUTPUT_EVENT
					? "Event" : "VarDeclaration";
		}
	}
	
	public IOConnection(int x, Num yNum, Num num, String name, IOType type, NCES nces) {
		this.x = x;
		this.y = yNum.num() * 30;
		yNum.increment();
		this.num = num.num();
		this.locNum = num.num();
		num.increment();
		this.name = name;
		this.type = type;
		switch (type) {
		case INPUT_CONDITION:
			nces.addInputVar(this);
			break;
		case INPUT_EVENT:
			nces.addInputEvent(this);
			break;
		case OUTPUT_CONDITION:
			nces.addOutputVar(this);
			break;
		case OUTPUT_EVENT:
			nces.addOutputEvent(this);
			break;
		}
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

	@Override
	public String toString() {
		return "<" + type.declaration() + " X=" + quote(x) + " Y=" + quote(y)
				+ " Num=" + quote(num) + " LocNum=" + quote(locNum)
				+ " Name=" + quote(name) + " Comment=" + quote("_") + ">"
				+ COMMENT_BOX + "</" + type.declaration() + ">";
	}
}
