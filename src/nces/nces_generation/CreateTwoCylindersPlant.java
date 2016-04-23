package nces.nces_generation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import nces.misc.Grid;
import nces.misc.Num;
import nces.structure.Arc;
import nces.structure.Arc.ArcType;
import nces.structure.IOConnection;
import nces.structure.IOConnection.IOType;
import nces.structure.NCES;
import nces.structure.Place;
import nces.structure.Transition;

public class CreateTwoCylindersPlant {
	private final static String PATH = "../../mywork/two_cylinders/plant.xml";
	private final static int INTERMEDIATE_POSITIONS = 0;
	
	/*private static int positionToSensorPosition(int pos) {
		int div = pos / (INTERMEDIATE_POSITIONS + 1);
		int mod = pos % (INTERMEDIATE_POSITIONS + 1);
		return mod == 0 ? div : -1;
	}*/
	
	private static int sensorPositionToPosition(int pos) {
		return pos * (INTERMEDIATE_POSITIONS + 1);
	}
	
	private static class Cylinder {
		private final IOConnection extend;
		private final IOConnection notExtend;
		private final IOConnection retract;
		private final IOConnection notRetract;
		@SuppressWarnings("unused")
		private final String name;
		private final IOConnection[] positionSensors = new IOConnection[6];
		private final IOConnection[] notPositionSensors = new IOConnection[6];
		private final Place back;
		private final Place stopped;
		private final Place fwd;
		private final Place[] positions = new Place[6 + INTERMEDIATE_POSITIONS * 5];
		private final Place[] notPositions = new Place[6 + INTERMEDIATE_POSITIONS * 5];
		private final Transition[] forwardPosTransitions = new Transition[positions.length - 1];
		
		public Cylinder(int inputX, int outputX, Num inputNum, Num outputNum, Num num, NCES plant, String name, Grid grid) {
			this.name = name;
			
			// input interface: extend and retract commands
			extend = new IOConnection(inputX, inputNum, num, name + "_extend", IOType.INPUT_CONDITION, plant);
			notExtend = new IOConnection(inputX, inputNum, num, "not_" + name + "_extend", IOType.INPUT_CONDITION, plant);
			retract = new IOConnection(inputX, inputNum, num, name + "_retract", IOType.INPUT_CONDITION, plant);
			notRetract = new IOConnection(inputX, inputNum, num, "not_" + name + "_retract", IOType.INPUT_CONDITION, plant);
			
			// places: movement status
			back = new Place(grid, "p_" + name + "_back", num, false, plant);
			stopped = new Place(grid, "p_" + name + "_stopped", num, true, plant);
			fwd = new Place(grid, "p_" + name + "_fwd", num, false, plant);
			
			// change movement status
			final Transition toBackFromStopped = new Transition(grid, num, false, plant, "t_" + name + "_to_back_from_stopped");
			plant.arcPair(stopped, toBackFromStopped, back, 0, retract);
			final Transition toFwdFromStopped = new Transition(grid, num, false, plant, "t_" + name + "_to_fwd_from_stopped");
			plant.arcPair(stopped, toFwdFromStopped, fwd, 0, extend);
			final Transition toBackFromFwd = new Transition(grid, num, false, plant, "t_" + name + "_to_back_from_fwd");
			plant.arcPair(fwd, toBackFromFwd, back, 0, retract);
			final Transition toFwdFromBack = new Transition(grid, num, false, plant, "t_" + name + "_to_fwd_from_back");
			plant.arcPair(back, toFwdFromBack, fwd, 0, extend);
			final Transition toStoppedFromBack = new Transition(grid, num, false, plant, "t_" + name + "_to_stopped_from_back");
			plant.arcPair(back, toStoppedFromBack, stopped, 0, notExtend, notRetract);
			final Transition toStoppedFromFwd = new Transition(grid, num, false, plant, "t_" + name + "_to_stopped_from_fwd");
			plant.arcPair(fwd, toStoppedFromFwd, stopped, 0, notExtend, notRetract);

			// on/off places for positions
			for (int i = 0; i < positions.length; i++) {
				positions[i] = new Place(grid, "p_" + name + "_pos_" + i, num, i == 0, plant);
				notPositions[i] = new Place(grid, "p_" + name + "_not_pos_" + i, num, i != 0, plant);
			}
			
			// backward transitions between these places
			for (int i = 1; i < positions.length; i++) {
				final Transition tFwd = new Transition(grid, num, false, plant, "t_" + name + "_" + (i - 1) + "_to_" + i);
				plant.arcPair(positions[i - 1], tFwd, positions[i], 1);
				plant.arcPair(notPositions[i], tFwd, notPositions[i - 1], 1);
				new Arc(fwd, tFwd, ArcType.CONDARC, 0, plant);
				
				final Transition tBack = new Transition(grid, num, false, plant, "t_" + name + "_" + i + "_to_" + (i - 1));
				plant.arcPair(positions[i], tBack, positions[i - 1], 1);
				plant.arcPair(notPositions[i - 1], tBack, notPositions[i], 1);
				new Arc(back, tBack, ArcType.CONDARC, 0, plant);
			
				// for future event connections
				forwardPosTransitions[i - 1] = tFwd;
			}
			
			// output interface: 0..5 position sensors
			for (int i = 0; i <= 5; i++) {
				positionSensors[i] = new IOConnection(outputX, outputNum, num,
						name + "_at_" + i, IOType.OUTPUT_CONDITION, plant);
				notPositionSensors[i] = new IOConnection(outputX, outputNum, num,
						"not_" + name + "_at_" + i, IOType.OUTPUT_CONDITION, plant);
				final int index = sensorPositionToPosition(i);
				new Arc(positions[index], positionSensors[i], ArcType.CONDARC, 0, plant);
				new Arc(notPositions[index], notPositionSensors[i], ArcType.CONDARC, 0, plant);
			}
		}
	}
	
	// the cube can be delivered if contiguous intermediate positions are occupied neither by cubes not by the cylinders
	private static class CubeDelivery {
		private final Transition tRed;
		private final Transition tBlue;
		
		public CubeDelivery(CubeLine cubeLine, int sensorPosition, Grid grid, Num num, NCES plant) {
			tRed = new Transition(grid, num, false, plant, "t_" + cubeLine.name + "_" + sensorPosition + "_red_delivery");
			tBlue = new Transition(grid, num, false, plant, "t_" + cubeLine.name + "_" + sensorPosition + "_blue_delivery");
			final int index = sensorPositionToPosition(sensorPosition);
			
			final List<Place> deliveryConditions = new ArrayList<>();
			final int index1 = sensorPositionToPosition(1);
			final int index2 = sensorPositionToPosition(2);
			final int index3 = sensorPositionToPosition(3);
			System.out.println("* Delivery " + cubeLine.name + " " + sensorPosition + " depends on");
			if (sensorPosition == 1) {
				for (int i = 1; i < cubeLine.cylinder.positions.length; i++) {
					deliveryConditions.add(cubeLine.cylinder.notPositions[i]);
				}
				for (int i = index1 + 1; i < index2; i++) {
					deliveryConditions.add(cubeLine.none[i]);
				}
			} else if (sensorPosition == 2) {
				for (int i = index1 + 1; i < cubeLine.cylinder.positions.length; i++) {
					deliveryConditions.add(cubeLine.cylinder.notPositions[i]);
				}
				for (int i = index1 + 1; i < index2; i++) {
					deliveryConditions.add(cubeLine.none[i]);
				}
				for (int i = index2 + 1; i < index3; i++) {
					deliveryConditions.add(cubeLine.none[i]);
				}
			} else {
				throw new AssertionError();
			}
			deliveryConditions.stream().forEach(c -> System.out.println(c.name));
			final Place[] deliveryConditionsArray = deliveryConditions.toArray(new Place[deliveryConditions.size()]);
			
			plant.arcPair(cubeLine.none[index], tRed, cubeLine.red[index], 1, deliveryConditionsArray);
			plant.arcPair(cubeLine.none[index], tBlue, cubeLine.blue[index], 1, deliveryConditionsArray);
		}
	}
	
	private static class CubeLine {
		private final String name;
		
		// cubes can continuously get off the platform
		private final Place[] red = new Place[sensorPositionToPosition(6)];
		private final Place[] blue = new Place[sensorPositionToPosition(6)];
		private final Place[] none = new Place[sensorPositionToPosition(6)];
		@SuppressWarnings("unused")
		private final CubeDelivery delivery1;
		@SuppressWarnings("unused")
		private final CubeDelivery delivery2;
		private final IOConnection[] redSensors = new IOConnection[6];
		private final IOConnection[] blueSensors = new IOConnection[6];
		private final IOConnection[] noneSensors = new IOConnection[6];
		private final Cylinder cylinder;
		
		public CubeLine(String name, Grid grid, Num num, NCES plant, int outputX, Num outputNum, Cylinder cylinder) {
			this.name = name;
			this.cylinder = cylinder;
			
			for (int i = sensorPositionToPosition(1); i < none.length; i++) {
				if (i != sensorPositionToPosition(3)) {
					red[i] = new Place(grid, "p_" + name + "_red_cube_pos_" + i, num, false, plant);
					blue[i] = new Place(grid, "p_" + name + "_blue_cube_pos_" + i, num, false, plant);
					none[i] = new Place(grid, "p_" + name + "_none_cube_pos_" + i, num, true, plant);
				}
			}
			
			delivery1 = new CubeDelivery(this, 1, grid, num, plant);
			delivery2 = new CubeDelivery(this, 2, grid, num, plant);
			
			for (int i = 1; i <= 5; i++) {
				if (i != 3) {
					redSensors[i] = new IOConnection(outputX, outputNum, num,
							name + "_red_at_" + i, IOType.OUTPUT_CONDITION, plant);
					blueSensors[i] = new IOConnection(outputX, outputNum, num,
							name + "_blue_at_" + i, IOType.OUTPUT_CONDITION, plant);
					noneSensors[i] = new IOConnection(outputX, outputNum, num,
							name + "_none_at_" + i, IOType.OUTPUT_CONDITION, plant);
					final int index = sensorPositionToPosition(i);
					new Arc(red[index], redSensors[i], ArcType.CONDARC, 0, plant);
					new Arc(blue[index], blueSensors[i], ArcType.CONDARC, 0, plant);
					new Arc(none[index], noneSensors[i], ArcType.CONDARC, 0, plant);
				}
			}
		}
		
		public void addMiddlePlaces(Place pMiddleRed, Place pMiddleBlue, Place pMiddleNone) {
			final int index = sensorPositionToPosition(3);
			red[index] = pMiddleRed;
			blue[index] = pMiddleBlue;
			none[index] = pMiddleNone;
		}
		
		public void addMiddleSensors(IOConnection middleRed, IOConnection middleBlue, IOConnection middleNone) {
			final int index = 3;
			redSensors[index] = middleRed;
			blueSensors[index] = middleBlue;
			noneSensors[index] = middleNone;
		}
		
		public void createCubeTransitions(NCES plant, Grid grid, Num num) {
			final Transition[] blueFwd = new Transition[none.length];
			final Transition[] redFwd = new Transition[none.length];
			
			final int index1 = sensorPositionToPosition(1);
			for (int i = index1 + 1; i < none.length; i++) {
				System.out.println("* Cube transitions from " + (i - 1) + " to " + i);
				blueFwd[i] = new Transition(grid, num, false, plant, "t_" + name + "_blue_" + (i - 1) + "_to_" + i);
				plant.arcPair(blue[i - 1], blueFwd[i], blue[i], 0);
				plant.arcPair(none[i], blueFwd[i], none[i - 1], 0);
				
				redFwd[i] = new Transition(grid, num, false, plant, "t_" + name + "_red_" + (i - 1) + "_to_" + i);
				plant.arcPair(red[i - 1], redFwd[i], red[i], 0);
				plant.arcPair(none[i], redFwd[i], none[i - 1], 0);
				
				// cylinders move cubes
				new Arc(cylinder.forwardPosTransitions[i - 1 - index1], blueFwd[i], ArcType.EVARC, 0, plant);
				new Arc(cylinder.forwardPosTransitions[i - 1 - index1], redFwd[i], ArcType.EVARC, 0, plant);
				
				// cubes move cubes
				if (i - index1 >= index1 + 1) {
					new Arc(blueFwd[i - index1], blueFwd[i], ArcType.EVARC, 0, plant);
					System.out.println(blueFwd[i - index1].name + " triggers " + blueFwd[i].name + " etc.");
					new Arc(redFwd[i - index1], blueFwd[i], ArcType.EVARC, 0, plant);
					new Arc(blueFwd[i - index1], redFwd[i], ArcType.EVARC, 0, plant);
					new Arc(redFwd[i - index1], redFwd[i], ArcType.EVARC, 0, plant);
				}
			}
		}
	}
	
	public static void main(String[] args) {
		Arc.POINTS_RECT = false;
		
		final int inputX = -900;
		final int outputX = -inputX;
		final Grid grid = new Grid(inputX, outputX, 14, new Random(), 0);
		final Num num = new Num();
		final Num inputNum = new Num();
		final Num outputNum = new Num();
		final NCES plant = new NCES(inputX, outputX, "plant");
		
		// cylinders
		final Cylinder vcyl = new Cylinder(inputX, outputX, inputNum, outputNum, num, plant, "vcyl", grid);
		grid.nextLine();
		final Cylinder hcyl = new Cylinder(inputX, outputX, inputNum, outputNum, num, plant, "hcyl", grid);
		grid.nextLine();
		//final Cylinder[] cylinders = new Cylinder[] { vcyl, hcyl };
		
		// cube lines
		final CubeLine vline = new CubeLine("vline", grid, num, plant, outputX, outputNum, vcyl);
		grid.nextLine();
		final CubeLine hline = new CubeLine("hline", grid, num, plant, outputX, outputNum, hcyl);
		grid.nextLine();
		final CubeLine[] cubeLines = new CubeLine[] { vline, hline };
		
		// middle part of the cube lines
		final int middleIndex = sensorPositionToPosition(3);
		final Place pMiddleRed = new Place(grid, "p_middle_red_cube_pos_" + middleIndex, num, false, plant);
		final Place pMiddleBlue = new Place(grid, "p_middle_blue_cube_pos_" + middleIndex, num, false, plant);
		final Place pMiddleNone = new Place(grid, "p_middle_none_cube_pos_" + middleIndex, num, true, plant);
		
		final IOConnection middleRed = new IOConnection(outputX, outputNum, num,
				"middle_red_at_3", IOType.OUTPUT_CONDITION, plant);
		final IOConnection middleBlue = new IOConnection(outputX, outputNum, num,
				"middle_blue_at_3", IOType.OUTPUT_CONDITION, plant);
		final IOConnection middleNone = new IOConnection(outputX, outputNum, num,
				"middle_none_at_3", IOType.OUTPUT_CONDITION, plant);
		new Arc(pMiddleRed, middleRed, ArcType.CONDARC, 0, plant);
		new Arc(pMiddleBlue, middleBlue, ArcType.CONDARC, 0, plant);
		new Arc(pMiddleNone, middleNone, ArcType.CONDARC, 0, plant);
		grid.nextLine();
		
		for (CubeLine l : cubeLines) {
			l.addMiddlePlaces(pMiddleRed, pMiddleBlue, pMiddleNone);
			l.addMiddleSensors(middleRed, middleBlue, middleNone);
			l.createCubeTransitions(plant, grid, num);
			grid.nextLine();
		}
		
		try (PrintWriter pw = new PrintWriter(new File(PATH))) {
			pw.println(plant);
			System.out.println("Done.");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
