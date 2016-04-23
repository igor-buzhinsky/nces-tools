package nces;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import nces.graph.ReachibilityGraph;
import nces.graph.State;
import nces.graph.StateMapper;

public class Rg2Smv {
	public static void main(String[] args) throws FileNotFoundException {
		if (args.length != 3) {
			System.out.println("Version: 0.2 (18.04.2016), author: Igor Buzhinsky (Aalto University)");
			System.out.println("Usage: java -jar rg2smv.jar <reachability graph (.arc)> <flattened NCES (.pnt)> <output (.smv)>");
			System.out.println("Rg2smv converts reachability graphs produced by ViVe (http://www.fb61499.com/valid.html) to the NuSMV format");
			System.exit(1);
		}
		final String arcFilename = args[0];
		final String pntFilename = args[1];
		final String smvFilename = args[2];

		final ReachibilityGraph g = new ReachibilityGraph();
		int currentState = -1;
		int modelTransitionNumber = 0;
		try (Scanner sc = new Scanner(new File(arcFilename))) {
			while (sc.hasNextLine()) {
				final String line = sc.nextLine();
				if (line.toLowerCase().contains("state nr.")) {
					final String[] tokens = line.split("\\s+");
					final int num = Integer.parseInt(tokens[2]);
					currentState = num - 1;
				} else if (line.startsWith("==")) {
					String[] tokens = line.split("s");
					int finalState = Integer.parseInt(tokens[tokens.length - 1].trim()) - 1;
					String[] transitions = line.split("\\{")[1].split("}")[0].split(",");
					List<Integer> intTransitions = Arrays.stream(transitions)
							.map(s -> Integer.parseInt(s.replace("t", "")) - 1).collect(Collectors.toList());
					modelTransitionNumber = Math.max(modelTransitionNumber,
							intTransitions.stream().mapToInt(x -> x).max().getAsInt() + 1);
					g.addEdge(currentState, finalState, intTransitions);
					continue;
				}
			}
		}
		
		final List<Integer> initialMarking = new ArrayList<>();
		final List<String> placeNames = new ArrayList<>();
		final List<List<Integer>> transitionsToPlaces = new ArrayList<>();
		final List<List<Integer>> transitionsFromPlaces = new ArrayList<>();
		int state = 0;
		try (Scanner sc = new Scanner(new File(pntFilename))) {
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
					if (number != placeNames.size()) {
						throw new AssertionError();
					}
				} else {
					break;
				}
			}
		}
		final State initState = new State(initialMarking);
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
			}
			for (int to : transitionsToPlaces.get(i)) {
				transitionsTo.get(to).add(i);
			}
		}
		
		final StateMapper sm = new StateMapper();
		for (int i = 0; i < modelTransitionNumber; i++) {
			sm.addModelTransition(transitionsFrom.get(i), transitionsTo.get(i));
		}
		g.assignStates(initState, sm);
		
		final String smv = g.toSmvString();
		try (PrintWriter pw = new PrintWriter(smvFilename)) {
			pw.println(smv);
		}
	}
}
