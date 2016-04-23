package nces.test_generation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import nces.graph.ReachibilityGraph;
import nces.graph.State;
import nces.graph.StateMapper;
import nces.graph.TestCase;

public class ReadGraphCylinder {
	private final static String PREFIX
		//= "TEST_Cyl_ClosLoop_INIT";
		= "Stage3_AllInputs";
	
	public static void main(String[] args) throws FileNotFoundException {
		final ReachibilityGraph g = new ReachibilityGraph();
		int currentState = -1;
		int modelTransitionNumber = 0;
		try (Scanner sc = new Scanner(new File(PREFIX + ".arc"))) {
			while (sc.hasNextLine()) {
				final String line = sc.nextLine();
				if (line.contains("State Nr.")) {
					final int num = Integer.parseInt(line.split("\\s+")[3]);
					currentState = num - 1;
				} else if (line.startsWith("==")) {
					String[] tokens = line.split(" ");
					int finalState = Integer.parseInt(tokens[tokens.length - 1]) - 1;
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
		final List<List<Integer>> transitionsToPlaces = new ArrayList<>();
		final List<List<Integer>> transitionsFromPlaces = new ArrayList<>();
		try (Scanner sc = new Scanner(new File(PREFIX + ".pnt"))) {
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				if (line.contains("PRE,POST")) {
					continue; 
				} else if (line.contains("@")) {
					break;
				}
				final int marking = Integer.parseInt(line.split(" +")[2]);
				initialMarking.add(marking);
				if (!line.contains(",")) {
					line += ", ";
				}
				
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
			}
		}
		State initState = new State(initialMarking);
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
			for (int from : transitionsToPlaces.get(i)) {
				transitionsTo.get(from).add(i);
				System.out.println("trans " + from + " -> place " + i);
			}
			for (int to : transitionsFromPlaces.get(i)) {
				transitionsTo.get(to).add(i);
				System.out.println("place " + i + " -> trans " + to);
			}
		}
		System.out.println(transitionsFrom);
		System.out.println(transitionsTo);
		
		StateMapper sm = new StateMapper();
		for (int i = 0; i < modelTransitionNumber; i++) {
			sm.addModelTransition(transitionsFrom.get(i), transitionsTo.get(i));
		}
		System.out.println(sm.toString());
		g.assignStates(initState, sm);
		System.out.println(g);
		
		// Coverage test suite
		List<Integer> placesToCover = new ArrayList<>();
		for (int i = 0; i < initialMarking.size(); i++) {
			placesToCover.add(i);
		}
		List<TestCase> testSuite = g.generateCoverageTestSuite(new LinkedHashSet<>(placesToCover));
		for (TestCase testCase : testSuite) {
			System.out.println(testCase);
		}
	}
}
