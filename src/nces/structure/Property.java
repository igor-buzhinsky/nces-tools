package nces.structure;

public class Property implements NamedAndPositionedObject {
	public final int x;
	public final int y;
	public final String name;
	
	public Property(NamedAndPositionedObject base, String name) {
		this.x = base.x();
		this.y = base.y();
		this.name = base.name() + "." + name;
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
