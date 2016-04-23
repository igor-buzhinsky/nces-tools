package nces.nces_generation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import nces.misc.Grid;
import nces.misc.Num;
import nces.structure.Arc;
import nces.structure.Arc.ArcType;
import nces.structure.IOConnection;
import nces.structure.IOConnection.IOType;
import nces.structure.NCES;
import nces.structure.Place;
import nces.structure.Transition;

public class CreateLIC100Plant {	
	private static class Complexity {
		final int lowest;
		final int llLevel;
		final int lLevel;
		final int initialLevel;
		final int hLevel;
		final int hhLevel;
		final int highest;
		final int step;
		final int setpoint;
		final int hystHigh;
		final int hystLow;
		final int binarySensorPosition;
		final int smallLossSteps;
		final int largeLossSteps;
		final int smallSupplySteps;
		final int largeSupplySteps;
		
		Complexity(int lowest, int llLevel, int lLevel,
				int initialLevel, int hLevel, int hhLevel, int highest,
				int step, int setpoint, int hystHigh, int hystLow,
				int binarySensorPosition, int smallLossSteps,
				int largeLossSteps, int smallSupplySteps, int largeSupplySteps) {
			this.lowest = lowest;
			this.llLevel = llLevel;
			this.lLevel = lLevel;
			this.initialLevel = initialLevel;
			this.hLevel = hLevel;
			this.hhLevel = hhLevel;
			this.highest = highest;
			this.step = step;
			this.setpoint = setpoint;
			this.hystHigh = hystHigh;
			this.hystLow = hystLow;
			this.binarySensorPosition = binarySensorPosition;
			this.smallLossSteps = smallLossSteps;
			this.largeLossSteps = largeLossSteps;
			this.smallSupplySteps = smallSupplySteps;
			this.largeSupplySteps = largeSupplySteps;
		}		
	}
	
	static final Complexity REDUCED = new Complexity(0,   2,   4,   6,   12,   14,  16, 1,   8, 2, 2,   14, 1, 2, 2, 3);
	static final Complexity FULL	= new Complexity(80, 90, 100, 190, 260, 270, 280, 2, 190, 5, 5, 270, 1, 2, 2, 3);
	private static final Complexity C = REDUCED;
	
	static class Level {
		private final int minLevel;
		private final int maxLevel;

		private final Place p;
		private final Place pNot;
	
		boolean between(int level) {
			return level >= minLevel && level < maxLevel;
		}
		
		Level(int minLevel, int maxLevel, String name, Grid grid, Num num, NCES plant, int initialLevel, IOConnection yes, IOConnection no) {
			this.minLevel = minLevel;
			this.maxLevel = maxLevel;
			boolean initial = between(initialLevel);
			p = new Place(grid, "p_" + name, num, initial, plant);
			pNot = new Place(grid, "p_not_" + name, num, !initial, plant);
			new Arc(p, yes, ArcType.CONDARC, 0, plant);
			new Arc(pNot, no, ArcType.CONDARC, 0, plant);
			System.out.println("level " + name + ": [" + minLevel + ", " + (maxLevel - 1) + "]");
		}
	}
	
	static class BinaryLevels {
		final Place pSensorWet; // 0
		final Place pSensorDry; // 1
		
		BinaryLevels(Grid grid, Num num, NCES plant, int initialLevel, int binarySensorPosition, IOConnection yes, IOConnection no) {
			pSensorWet = new Place(grid, "p_sensor_wet", num, initialLevel >= binarySensorPosition, plant);
			pSensorDry = new Place(grid, "p_sensor_dry", num, initialLevel < binarySensorPosition, plant);
			new Arc(pSensorDry, yes, ArcType.CONDARC, 0, plant);
			new Arc(pSensorWet, no, ArcType.CONDARC, 0, plant);
			System.out.println("binary boundary: " + binarySensorPosition);
		}
	}
	
	private final static String PATH = "../../mywork/lic100/plant.xml";
	
	private static Level properLevel(List<Level> levels, int waterLevel) {
		for (Level l : levels) {
			if (l.between(waterLevel)) {
				return l;
			}
		}
		throw new AssertionError();
	}
	
	public static void main(String[] args) {
		final int inputX = -900;
		final int outputX = -inputX;
		final Grid grid = new Grid(inputX, outputX, 16, new Random(), 0);
		final Num num = new Num();
		final Num inputNum = new Num();
		final Num outputNum = new Num();
		final NCES plant = new NCES(inputX, outputX, "plant");
		
		// interface
		final IOConnection valveOpen = new IOConnection(inputX, inputNum, num, "valve_open", IOType.INPUT_CONDITION, plant);
		final IOConnection valveClosed = new IOConnection(inputX, inputNum, num, "valve_closed", IOType.INPUT_CONDITION, plant);
				
		final IOConnection aboveHH = new IOConnection(outputX, outputNum, num, "above_hh", IOType.OUTPUT_CONDITION, plant);
		final IOConnection notAboveHH = new IOConnection(outputX, outputNum, num, "not_above_hh", IOType.OUTPUT_CONDITION, plant);
		final IOConnection aboveH = new IOConnection(outputX, outputNum, num, "above_h", IOType.OUTPUT_CONDITION, plant);
		final IOConnection notAboveH = new IOConnection(outputX, outputNum, num, "not_above_h", IOType.OUTPUT_CONDITION, plant);
		final IOConnection aboveThreshold = new IOConnection(outputX, outputNum, num, "above_threshold", IOType.OUTPUT_CONDITION, plant);
		final IOConnection notAboveThreshold = new IOConnection(outputX, outputNum, num, "not_above_threshold", IOType.OUTPUT_CONDITION, plant);
		final IOConnection aboveSetpoint = new IOConnection(outputX, outputNum, num, "above_setpoint", IOType.OUTPUT_CONDITION, plant);
		final IOConnection notAboveSetpoint = new IOConnection(outputX, outputNum, num, "not_above_setpoint", IOType.OUTPUT_CONDITION, plant);
		final IOConnection belowSetpoint = new IOConnection(outputX, outputNum, num, "below_setpoint", IOType.OUTPUT_CONDITION, plant);
		final IOConnection notBelowSetpoint = new IOConnection(outputX, outputNum, num, "not_below_setpoint", IOType.OUTPUT_CONDITION, plant);
		final IOConnection belowThreshold = new IOConnection(outputX, outputNum, num, "below_threshold", IOType.OUTPUT_CONDITION, plant);
		final IOConnection notBelowThreshold = new IOConnection(outputX, outputNum, num, "not_below_threshold", IOType.OUTPUT_CONDITION, plant);
		final IOConnection belowL = new IOConnection(outputX, outputNum, num, "below_l", IOType.OUTPUT_CONDITION, plant);
		final IOConnection notBelowL = new IOConnection(outputX, outputNum, num, "not_below_l", IOType.OUTPUT_CONDITION, plant);
		final IOConnection belowLL = new IOConnection(outputX, outputNum, num, "below_ll", IOType.OUTPUT_CONDITION, plant);
		final IOConnection notBelowLL = new IOConnection(outputX, outputNum, num, "not_below_ll", IOType.OUTPUT_CONDITION, plant);
		final IOConnection sensorWet = new IOConnection(outputX, outputNum, num, "sensor_wet", IOType.OUTPUT_CONDITION, plant);
		final IOConnection sensorDry = new IOConnection(outputX, outputNum, num, "sensor_dry", IOType.OUTPUT_CONDITION, plant);
		
		final Level lAboveHH = new Level(C.hhLevel, C.highest + C.step, "above_hh", grid, num, plant, C.initialLevel, aboveHH, notAboveHH);
		final Level lAboveH = new Level(C.hLevel, C.hhLevel, "above_h", grid, num, plant, C.initialLevel, aboveH, notAboveH);
		final Level lAboveThreshold = new Level(C.setpoint + C.hystHigh, C.hLevel, "above_threshold", grid, num, plant, C.initialLevel, aboveThreshold, notAboveThreshold);
		final Level lAboveSetpoint = new Level(C.setpoint, C.setpoint + C.hystHigh, "above_setpoint", grid, num, plant, C.initialLevel, aboveSetpoint, notAboveSetpoint);
		final Level lBelowSetpoint = new Level(C.setpoint - C.hystLow, C.setpoint, "below_setpoint", grid, num, plant, C.initialLevel, belowSetpoint, notBelowSetpoint);
		final Level lBelowThreshold = new Level(C.lLevel, C.setpoint - C.hystLow, "below_threshold", grid, num, plant, C.initialLevel, belowThreshold, notBelowThreshold);
		final Level lBelowL = new Level(C.llLevel, C.lLevel, "below_l", grid, num, plant, C.initialLevel, belowL, notBelowL);
		final Level lBelowLL = new Level(C.lowest, C.llLevel, "below_ll", grid, num, plant, C.initialLevel, belowLL, notBelowLL);
		final List<Level> levels = Arrays.asList(
				lAboveHH, lAboveH, lAboveThreshold, lAboveSetpoint,
				lBelowSetpoint, lBelowThreshold, lBelowL, lBelowLL
		);

		final BinaryLevels binaryLevels = new BinaryLevels(grid, num, plant, C.initialLevel, C.binarySensorPosition, sensorDry, sensorWet);
		grid.nextLine();
		
		// (only) positive level places
		final Map<Integer, Place> levelToPlace = new HashMap<>();
		for (int level = C.lowest; level <= C.highest; level += C.step) {
			 final Place p = new Place(grid, "p_level_" + level, num, level == C.initialLevel, plant);
			 levelToPlace.put(level, p);
		}
		grid.nextLine();
		
		// water level transitions
		final Function<Integer, Place> properBinaryPlace = lvl -> 
			lvl < C.binarySensorPosition ? binaryLevels.pSensorDry : binaryLevels.pSensorWet;
		for (int currentLevel = C.lowest; currentLevel <= C.highest; currentLevel += C.step) {
			final Place currentPlace = levelToPlace.get(currentLevel);
			final Level currentLvl = properLevel(levels, currentLevel);
			final Place currentBinaryPlace = properBinaryPlace.apply(currentLevel);
			for (String supply : new String[] { "small", "large", "off" }) {
				for (String loss : new String[] { "small", "large" }) {
					final Transition t = new Transition(grid, num, false, plant, "t_" + currentLevel + "_" + loss + "_" + supply);
					final int targetLevel = currentLevel
							+ (supply.equals("small") ? C.smallSupplySteps : supply.equals("large") ? C.largeSupplySteps : 0) * C.step
							- (loss.equals("small") ? C.smallLossSteps : C.largeLossSteps) * C.step;
					final int refinedTargetLevel = Math.max(Math.min(targetLevel, C.highest), C.lowest);
					final Place targetPlace = levelToPlace.get(refinedTargetLevel);
					final Level targetLvl = properLevel(levels, refinedTargetLevel);
					final Place targetBinaryPlace = properBinaryPlace.apply(refinedTargetLevel);
					
					new Arc(supply.equals("off") ? valveClosed : valveOpen, t, ArcType.CONDARC, 0, plant);
					plant.arcPair(currentPlace, t, targetPlace, 1);
					if (currentLvl != targetLvl) {
						plant.arcPair(currentLvl.p, t, targetLvl.p, 1);
						plant.arcPair(targetLvl.pNot, t, currentLvl.pNot, 1);
					}
					if (currentBinaryPlace != targetBinaryPlace) {
						plant.arcPair(currentBinaryPlace, t, targetBinaryPlace, 1);
					}
					System.out.println("transition: " + t.name + " -> " + refinedTargetLevel);
				}
			}
		}
		
		try (PrintWriter pw = new PrintWriter(new File(PATH))) {
			pw.println(plant);
			System.out.println("Done.");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
