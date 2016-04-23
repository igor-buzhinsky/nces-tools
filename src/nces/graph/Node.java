package nces.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Node {
	public final int number;
	private Optional<State> state = Optional.empty();
	private final List<Transition> transitions = new ArrayList<>();
	
	public Node(int number) {
		this.number = number;
	}
	
	public Collection<Transition> transitions() {
		return Collections.unmodifiableList(transitions);
	}
	
	public void addEdge(int to, List<Integer> modelEdges) {
		transitions.add(new Transition(number, to, modelEdges));
	}

	@Override
	public String toString() {
		return "Node[" + number + ", " + transitions
				+ (hasState() ? ", " + getState() : "") + "]";
	}
	
	public void setState(State state) {
		this.state = Optional.of(state);
	}
	
	public State getState() {
		return state.get();
	}
	
	public boolean hasState() {
		return state.isPresent();
	}
}