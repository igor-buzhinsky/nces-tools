package nces.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class ReachibilityGraph {
	private final List<Node> nodes = new ArrayList<>();
	
	public ReachibilityGraph() {
		ensureNodes(0);
	}
	
	private void ensureNodes(int index) {
		while (nodes.size() < index + 1) {
			nodes.add(new Node(nodes.size()));
		}
	}
	
	public void addEdge(int from, int to, List<Integer> modelEdges) {
		ensureNodes(from);
		ensureNodes(to);
		nodes.get(from).addEdge(to, modelEdges);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("ReachibilityGraph\n");
		for (Node node : nodes) {
			sb.append(node + "\n");
		}
		return sb.toString();
	}
	
	public void assignStates(State initialState, StateMapper sm) {
		nodes.get(0).setState(initialState);
		
		// bfs
		final Deque<Node> queue = new LinkedList<>();
		queue.addLast(nodes.get(0));
		while (!queue.isEmpty()) {
			final Node node = queue.removeFirst();
			for (Transition t : node.transitions()) {
				final Node dest = nodes.get(t.to);
				if (!dest.hasState()) {
					State newState = sm.map(node.getState(), t.modelEdges());
					dest.setState(newState);
					queue.add(dest);
				}
			}
		}
	}

	public List<TestCase> generateCoverageTestSuite(Set<Integer> placesToCover) {
		final List<TestCase> testSuite = new ArrayList<>();
		final Set<Integer> missedGoals = new LinkedHashSet<>();
		final Set<Integer> remainingGoals = new LinkedHashSet<>(placesToCover);
		while (!remainingGoals.isEmpty()) {
			int target = remainingGoals.iterator().next();
			final Optional<TestCase> testCase = coverBfs(target, placesToCover, remainingGoals);
			if (!testCase.isPresent()) {
				missedGoals.add(target);
				remainingGoals.remove(target);
			} else {
				testSuite.add(testCase.get());
			}
		}
		System.out.println("Missed places: " + missedGoals.stream().map(x -> x + 1).collect(Collectors.toList()));
		return TestCase.minimizeTestSuite(testSuite);
	}
	
	private Optional<TestCase> coverBfs(int target, Set<Integer> placesToCover, Set<Integer> remainingGoals) {
		class QueueElement {
			final Transition transition;
			final QueueElement previous;
			
			QueueElement(Transition transition, QueueElement previous) {
				this.transition = transition;
				this.previous = previous;
			}
		}
		
		final Set<Integer> visited = new HashSet<>();
		final Deque<QueueElement> queue = new LinkedList<>();
		queue.addLast(new QueueElement(new Transition(-1, 0, Collections.emptyList()), null));
		while (!queue.isEmpty()) {
			final QueueElement element = queue.removeFirst();
			final Transition trans = element.transition;
			final Node node = nodes.get(trans.to);
			final State state = node.getState();
			if (state.get(target) > 0) {
				final List<Transition> path = new ArrayList<>();
				QueueElement current = element;
				do {
					path.add(current.transition);
					current = current.previous;
				} while (current != null);
				final Set<Integer> coveredGoals = new TreeSet<>();
				for (Transition t : path) {
					final State tState = nodes.get(t.to).getState();
					for (int i : placesToCover) {
						if (tState.get(i) > 0) {
							remainingGoals.remove(i);
							coveredGoals.add(i);
						}
					}
				}
				path.remove(path.size() - 1);
				Collections.reverse(path);
				return Optional.of(new TestCase(path, coveredGoals));
			}
			if (!visited.add(node.number)) {
				continue;
			}
			for (Transition t : node.transitions()) {
				queue.addLast(new QueueElement(t, element));
			}
		}
		return Optional.empty();
	}
	
	public Set<Integer> reachablePlaces() {
		final Set<Integer> result = new TreeSet<>();
		for (Node n : nodes) {
			final List<Integer> marking = n.getState().markingCopy();
			for (int i = 0; i < marking.size(); i++) {
				if (marking.get(i) > 0) {
					result.add(i + 1);
				}
			}
		}
		return result;
	}
	
	public String toSmvString() {
		final String nl = System.lineSeparator();
		final StringBuilder sb = new StringBuilder();
		sb.append("MODULE main" + nl);
		sb.append("VAR" + nl);
		sb.append("    state: 1.." + nodes.size() + ";" + nl);
		sb.append("ASSIGN" + nl);
		sb.append("    init(state) := 1;" + nl);
		sb.append("    next(state) := case" + nl);
		final List<Integer> deadlocks = new ArrayList<>();
		for (int i = 0; i < nodes.size(); i++) {
			final Set<Integer> next = new TreeSet<>();
			for (Transition t : nodes.get(i).transitions()) {
				next.add(t.to + 1);
			}
			if (next.isEmpty()) {
				System.err.println("Deadlock in state " + (i + 1) + "! Adding a self-loop.");
				next.add(i + 1);
				deadlocks.add(i + 1);
			}
			sb.append("        state = " + (i + 1) + ": {" + next.toString().replace("[", "").replace("]", "") + "};" + nl);
		}
		sb.append("    esac;" + nl);
		sb.append("DEFINE" + nl);
		final int numPlaces = nodes.get(0).getState().markingCopy().size();
		for (int i = 0; i < numPlaces; i++) {
			final Set<Integer> onPlaces = new TreeSet<>();
			for (int j = 0; j < nodes.size(); j++) {
				if (nodes.get(j).getState().get(i) > 0) {
					onPlaces.add(j + 1);
				}
			}
			final String condition = onPlaces.isEmpty()
					? "FALSE"
					: ("state in " + onPlaces.toString().replace("[", "{").replace("]", "}"));
			sb.append("    p" + (i + 1) + " := " + condition + ";" + nl);
		}
		final String deadlockCondition = deadlocks.isEmpty()
				? "FALSE"
				: ("state in " + deadlocks.toString().replace("[", "{").replace("]", "}"));
		sb.append("    deadlock := " + deadlockCondition + ";" + nl);

		return sb.toString();
	}
}