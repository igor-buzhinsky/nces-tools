package nces.graph;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Transition {
	public final int from;
	public final int to;
	private final List<Integer> modelEdges;
	
	public Transition(int from, int to, List<Integer> modelEdges) {
		this.from = from;
		this.to = to;
		this.modelEdges = modelEdges;
	}
	
	public Collection<Integer> modelEdges() {
		return Collections.unmodifiableList(modelEdges);
	}

	@Override
	public String toString() {
		return "[" + from + "->"
				+ modelEdges + "->" + to + "]";
	}
}