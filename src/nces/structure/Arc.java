package nces.structure;


public class Arc extends NcesObject {
	public final NamedAndPositionedObject start;
	public final NamedAndPositionedObject end;
	public final String timeValue;
	public final ArcType type;
	
	public enum ArcType {
		ARC, CONDARC, EVARC;
		
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}
	
	private String timeValue(int time) {
		return time == 0 ? "" : (time + ";" + "-2");
	}
	
	public Arc(NamedAndPositionedObject start, NamedAndPositionedObject end, ArcType type, int time, NCES nces) {
		if (start == null || end == null) {
			throw new NullPointerException();
		}
		if (type == ArcType.ARC && (start instanceof IOConnection || end instanceof IOConnection)) {
			throw new AssertionError();
		}
		if (type == ArcType.CONDARC && start instanceof Transition) {
			throw new AssertionError();
		}
		if (type == ArcType.EVARC && (start instanceof Place || end instanceof Place)) {
			throw new AssertionError();
		}
		this.start = start;
		this.end = end;
		this.type = type;
		timeValue = timeValue(time);
		nces.addArc(this);
	}
	
	public static boolean POINTS_RECT = true;
	
	private String pointsLine() {
		return "<Point Num=" + quote(1) + " X=" + quote(start.x()) + " Y=" + quote(start.y()) + " />"
			+ "<Point Num=" + quote(2) + " X=" + quote(end.x()) + " Y=" + quote(end.y()) + " />";
	}
	
	private String points() {
		return POINTS_RECT ? pointsRect() : pointsLine();
	}
	
	private String pointsRect() {
		return "<Point Num=" + quote(1) + " X=" + quote(start.x()) + " Y=" + quote(start.y()) + " />"
			+ "<Point Num=" + quote(2) + " X=" + quote(0) + " Y=" + quote(start.y()) + " />"
			+ "<Point Num=" + quote(3) + " X=" + quote(0) + " Y=" + quote(end.y()) + " />"
			+ "<Point Num=" + quote(4) + " X=" + quote(end.x()) + " Y=" + quote(end.y()) + " />";
	}
	
	@Override
	public String toString() {
		return "<" + type + " StartPoint=" + quote(start.name()) + " EndPoint=" + quote(end.name())
			+ " ArcWeight=" + quote(1) + " TimeValue=" + quote(timeValue)
			+ " Comment=" + quote("_") + ">"
			+ points() + "</" + type + ">";
	}
}
