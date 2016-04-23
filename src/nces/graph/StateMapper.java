package nces.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

public class StateMapper {
	private final List<Pair<List<Integer>, List<Integer>>> transitionDescriptions = new ArrayList<>();
	
	public void addModelTransition(List<Integer> from, List<Integer> to) {
		transitionDescriptions.add(Pair.of(from, to));
	}
	
	public State map(State s, Collection<Integer> modelTransitions) {
		final List<Integer> newMarking = s.markingCopy();
		
		for (Integer trans : modelTransitions) {
			final List<Integer> from = transitionDescriptions.get(trans).getLeft();
			final List<Integer> to = transitionDescriptions.get(trans).getRight();
			if (from.stream().allMatch(n -> s.get(n) > 0)) {
				from.forEach(k -> newMarking.set(k, s.get(k) - 1));
				to.forEach(k -> newMarking.set(k, s.get(k) + 1));
			}
		}
		
		return new State(newMarking);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < transitionDescriptions.size(); i++) {
			if (i != 0) {
				sb.append(", ");
			}
			final Pair<List<Integer>, List<Integer>> p = transitionDescriptions.get(i);
			sb.append(i + "=" + p.getLeft() + "->" + p.getRight());
		}
		return "[" + sb.toString() + "]";
	}
}