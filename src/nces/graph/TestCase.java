package nces.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TestCase {
	private final List<Transition> transitions;
	private final Set<Integer> coveredGoals;
	
	public TestCase(List<Transition> transitions, Set<Integer> coveredGoals) {
		this.transitions = transitions;
		this.coveredGoals = coveredGoals;
	}

	public List<Transition> getTransitions() {
		return Collections.unmodifiableList(transitions);
	}

	public Set<Integer> getCoveredGoals() {
		return Collections.unmodifiableSet(coveredGoals);
	}
	
	@Override
	public String toString() {
		return transitions + ", covered: " + coveredGoals;
	}
	
	public static List<TestCase> minimizeTestSuite(List<TestCase> testSuite) {
		final List<TestCase> nonDominant = new ArrayList<>();
		l: for (int i = 0; i < testSuite.size(); i++) {
			final TestCase tc = testSuite.get(i);
			for (int j = 0; j < testSuite.size(); j++) {
				if (j != i && testSuite.get(j).coveredGoals.containsAll(tc.coveredGoals)) {
					continue l;
				}
			}
			nonDominant.add(tc);
		}
		return nonDominant;
	}
}
