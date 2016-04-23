package nces.structure;

public class ModuleInstance implements NamedAndPositionedObject {
	public final int x;
	public final int y;
	public final String name;
	public final String type;
	
	public ModuleInstance(int x, int y, String name, String type) {
		this.x = x;
		this.y = y;
		this.name = name;
		this.type = type;
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
		return "<ModuleInstance Name=\"" + name + "\" Type=\"" + type + "\" X=\"" + x + "\" Y=\"" + y + "\" Width=\"200\" Comment=\"_\" />";
	}
}
