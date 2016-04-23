package nces.nces_generation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import nces.misc.Grid;
import nces.misc.Num;
import nces.structure.Arc;
import nces.structure.Arc.ArcType;
import nces.structure.IOConnection;
import nces.structure.IOConnection.IOType;
import nces.structure.NCES;
import nces.structure.NamedAndPositionedObject;
import nces.structure.Place;
import nces.structure.Transition;

import org.apache.commons.lang3.tuple.Pair;

public class AutomatonToNCES {	
	private final static String AUTOMATON_PATH = "../../EFSM-tools/qbf/automaton.gv";
	private final static String OUTPUT_PATH = AUTOMATON_PATH + ".xml";
	private final static List<String> EVENTS = Arrays.asList("closed", "open");
	private final static List<String> ACTIONS = Arrays.asList(
			"abovehh", "aboveh", "aboveth", "abovesp",
			"belowth", "belowsp", "belowl", "belowll", "sensordry", "sensorwet"
	);
	
	private static class BinaryOutput {
		final Place pFalse;
		final Place pTrue;
		final IOConnection cFalse;
		final IOConnection cTrue;
		
		BinaryOutput(Grid grid, Num num, NCES plant, String outputName, int outputX, Num outputNum) {
			cFalse = new IOConnection(outputX, outputNum, num, "not_" + outputName, IOType.OUTPUT_CONDITION, plant);
			cTrue = new IOConnection(outputX, outputNum, num, outputName, IOType.OUTPUT_CONDITION, plant);

			pFalse = new Place(grid, "p_" + cFalse.name, num, true, plant);
			pTrue = new Place(grid, "p_" + cTrue.name, num, false, plant);
			
			new Arc(pFalse, cFalse, ArcType.CONDARC, 0, plant);
			new Arc(pTrue, cTrue, ArcType.CONDARC, 0, plant);
		}
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		final int inputX = -600;
		final int outputX = -inputX;
		final Grid grid = new Grid(inputX, outputX, 10, new Random(), 0);
		final Num num = new Num();
		final Num inputNum = new Num();
		final Num outputNum = new Num();
		final NCES plant = new NCES(inputX, outputX, "heuristic_plant");
		//Arc.POINTS_RECT = false;
		
		final IOConnection trigger = new IOConnection(inputX, inputNum, num, "trigger", IOType.INPUT_EVENT, plant);
		final Map<String, IOConnection> inputs = new LinkedHashMap<>();
		for (String input : EVENTS) {
			inputs.put(input, new IOConnection(inputX, inputNum, num, input, IOType.INPUT_CONDITION, plant));
		}
		final Map<String, BinaryOutput> outputs = new LinkedHashMap<>();
		for (String output : ACTIONS) {
			outputs.put(output, new BinaryOutput(grid, num, plant, output, outputX, outputNum));
		}
		final Place init = new Place(grid, "p_init", num, true, plant);
		
		final Map<String, List<String>> actions = new LinkedHashMap<>();
		final Map<String, List<Pair<String, String>>> transitions = new LinkedHashMap<>();
		actions.put("init", new ArrayList<>());
		transitions.put("init", new ArrayList<>());
		
		try (Scanner sc = new Scanner(new File(AUTOMATON_PATH))) {
			while (sc.hasNextLine()) {
				final String line = sc.nextLine();
				final String tokens[] = line.split(" +");
				if (!line.contains(";")) {
					continue;
				}
				if (line.contains("->")) {
					final String from = tokens[1];
					final String to = tokens[3].replaceAll(";", "");
					final String event = from.equals("init") ? null : tokens[6].replaceAll("[;\\]\"]", "");
					transitions.get(from).add(Pair.of(to, event));
				} else {
					final String from = tokens[1];
					transitions.put(from, new ArrayList<>());
					if (from.equals("init") || from.equals("node")) {
						continue;
					}
					actions.put(from, Arrays.asList(line.split("\"")[1].split(":")[1].trim().split(", ")));
				}
				System.out.println(line);
			}
		}

		System.out.println(actions);
		System.out.println(transitions);
		
		final Map<String, Place> places = new LinkedHashMap<>();
		places.put("init", init);
		for (String state : actions.keySet()) {
			if (!state.equals("init")) {
				places.put(state, new Place(grid, "p_" + state, num, false, plant));
			}
		}
		
		for (Map.Entry<String, List<Pair<String, String>>> transition : transitions.entrySet()) {
			final String from = transition.getKey();
			for (Pair<String, String> p : transition.getValue()) {
				final String to = p.getLeft();
				final String event = p.getRight();
				final Transition t = new Transition(grid, num, false,
						plant, String.join("_", "t", from, event, to));
				final NamedAndPositionedObject[] conditions = event == null
						? new NamedAndPositionedObject[0]
						: new NamedAndPositionedObject[] { inputs.get(event) };
				plant.arcPair(places.get(from), t, places.get(to), 0, conditions);
				if (event != null) {
					new Arc(trigger, t, ArcType.EVARC, 0, plant);
				}
				for (String action : ACTIONS) {
					final BinaryOutput bo = outputs.get(action);
					final boolean hasFrom = actions.get(from).contains(action);
					final boolean hasTo = actions.get(to).contains(action);
					if (!hasFrom && hasTo) {
						plant.arcPair(bo.pFalse, t, bo.pTrue, 0);
					} else if (hasFrom && !hasTo) {
						plant.arcPair(bo.pTrue, t, bo.pFalse, 0);
					}
				}
			}
		}
		
		try (PrintWriter pw = new PrintWriter(new File(OUTPUT_PATH))) {
			pw.println(plant);
			System.out.println("Done.");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
