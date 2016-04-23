package nces.test_generation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import nces.graph.ReachibilityGraph;
import nces.graph.State;
import nces.graph.StateMapper;
import nces.graph.TestCase;

public class ReadGraphLIC100 {
	static class Setting {
		final String prefix;
		final boolean isPositive;
		final List<Integer> undesiredPlaces;
		
		Setting(String prefix, boolean isPositive,
				List<Integer> undesiredPlaces) {
			this.prefix = prefix;
			this.isPositive = isPositive;
			this.undesiredPlaces = undesiredPlaces;
		}
	}
	
	final static Setting CLOSED_LOOP_COVERAGE
		= new Setting("/home/buzhinsky/repos/mywork/lic100/system", true, Collections.emptyList());
	final static Setting OPEN_LOOP_COVERAGE
		= new Setting("/home/buzhinsky/repos/mywork/lic100/system_nondet_plant", true, Collections.emptyList());
	final static Setting NEGATIVE
		= new Setting("/home/buzhinsky/repos/mywork/lic100/system_nondet_controller", false,
		Arrays.asList(19, 20, 33, 34, 35));

	private final static Setting SETTING
		//= CLOSED_LOOP_COVERAGE;
		//= OPEN_LOOP_COVERAGE;
		= NEGATIVE;
	
	public static void main(String[] args) throws FileNotFoundException {
		final ReachibilityGraph g = new ReachibilityGraph();
		int currentState = -1;
		int modelTransitionNumber = 0;
		try (Scanner sc = new Scanner(new File(SETTING.prefix + ".arc"))) {
			while (sc.hasNextLine()) {
				final String line = sc.nextLine();
				if (line.toLowerCase().contains("state nr.")) {
					final int num = Integer.parseInt(line.split("\\s+")[3]);
					currentState = num - 1;
				} else if (line.startsWith("==")) {
					String[] tokens = line.split("s");
					int finalState = Integer.parseInt(tokens[tokens.length - 1].trim()) - 1;
					String[] transitions = line.split(":\\{")[1].split("}")[0].split(",");
					List<Integer> intTransitions = Arrays.stream(transitions)
							.map(s -> Integer.parseInt(s) - 1).collect(Collectors.toList());
					modelTransitionNumber = Math.max(modelTransitionNumber,
							intTransitions.stream().mapToInt(x -> x).max().getAsInt() + 1);
					g.addEdge(currentState, finalState, intTransitions);
					continue;
				}
			}
		}
		System.out.println(g);
		
		final List<Integer> initialMarking = new ArrayList<>();
		final List<String> placeNames = new ArrayList<>();
		final List<List<Integer>> transitionsToPlaces = new ArrayList<>();
		final List<List<Integer>> transitionsFromPlaces = new ArrayList<>();
		int state = 0;
		try (Scanner sc = new Scanner(new File(SETTING.prefix + ".pnt"))) {
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				if (line.contains("PRE,POST") || line.startsWith("pl-nr.")) {
					continue; 
				} else if (line.contains("@")) {
					state++;
					continue;
				}
				if (state == 0) {
					final int marking = Integer.parseInt(line.split(" +")[2]);
					initialMarking.add(marking);
					if (line.trim().split(" +").length == 2) { // no to and from places
						continue;
					} else if (!line.contains(",")) {
						line += ", ";
					}
					System.out.println(line);
	
					transitionsToPlaces.add(
							Arrays.stream(line.split(",")[0].split("   ")[1].trim().split(" +"))
							.filter(s -> !s.isEmpty())
							.map(s -> Integer.parseInt(s) - 1).collect(Collectors.toList())
					);
					transitionsFromPlaces.add(
							Arrays.stream(line.split(",")[1].trim().split(" "))
							.filter(s -> !s.isEmpty())
							.map(s -> Integer.parseInt(s) - 1).collect(Collectors.toList())
					);
				} else if (state == 1) {
					final String[] tokens = line.split(" +");
					int number = Integer.parseInt(tokens[0].substring(0, tokens[0].length() - 1));
					final String name = tokens[1].trim();
					placeNames.add(name);
					System.out.println(number + " " + name);
					if (number != placeNames.size()) {
						throw new AssertionError();
					}
				} else {
					break;
				}
			}
		}
		final State initState = new State(initialMarking);
		System.out.println(initState);
		System.out.println(transitionsToPlaces);
		System.out.println(transitionsFromPlaces);
		for (List<Integer> l : transitionsToPlaces) {
			for (Integer t : l) {
				modelTransitionNumber = Math.max(modelTransitionNumber, t + 1);
			}
		}
		for (List<Integer> l : transitionsFromPlaces) {
			for (Integer t : l) {
				modelTransitionNumber = Math.max(modelTransitionNumber, t + 1);
			}
		}
		
		final List<List<Integer>> transitionsFrom = new ArrayList<>();
		final List<List<Integer>> transitionsTo = new ArrayList<>();

		for (int i = 0; i < modelTransitionNumber; i++) {
			transitionsFrom.add(new ArrayList<>());
			transitionsTo.add(new ArrayList<>());
		}
		
		for (int i = 0; i < transitionsToPlaces.size(); i++) {
			for (int from : transitionsFromPlaces.get(i)) {
				transitionsFrom.get(from).add(i);
				System.out.println("trans " + from + " -> place " + i);
			}
			for (int to : transitionsToPlaces.get(i)) {
				transitionsTo.get(to).add(i);
				System.out.println("place " + i + " -> trans " + to);
			}
		}
		System.out.println(transitionsFrom);
		System.out.println(transitionsTo);
		
		final StateMapper sm = new StateMapper();
		for (int i = 0; i < modelTransitionNumber; i++) {
			sm.addModelTransition(transitionsFrom.get(i), transitionsTo.get(i));
		}
		System.out.println(sm.toString());
		g.assignStates(initState, sm);
		System.out.println("Reachable places: " + g.reachablePlaces());
		//System.out.println(g);
		
		// Coverage / Negative test suite
		final List<Integer> placesToCover = new ArrayList<>();
		if (SETTING.isPositive) {
			for (int i = 0; i < initialMarking.size(); i++) {
				placesToCover.add(i);
			}
		} else {
			placesToCover.addAll(SETTING.undesiredPlaces.stream().map(x -> x - 1).collect(Collectors.toList()));
		}
		final List<TestCase> testSuite = g.generateCoverageTestSuite(new LinkedHashSet<>(placesToCover));
		for (TestCase testCase : testSuite) {
			System.out.println(testCase);
		}
		System.out.println(testSuite.size() + " tests.");
	}
}
