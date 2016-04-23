package nces.structure;

public class NcesObject {
	protected final static String COMMENT_BOX
		= "<CommentBox X=\"0\" Y=\"0\" Width=\"20\" Height=\"20\" />";
	
	protected static String quote(Object s) {
		return "\"" + s + "\"";
	}
}
