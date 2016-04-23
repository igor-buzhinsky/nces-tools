package nces.test_generation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import nces.structure.Arc;
import nces.structure.Arc.ArcType;
import nces.structure.ModuleInstance;
import nces.structure.NCES;
import nces.structure.Property;

public class CreateTrafficLightsTests extends CreateTests {
	private static final int MAX_TESTS = 10;
	
	static class TestTuple {
		final boolean t1intense;
		final boolean t2intense;
		final boolean manual;
		final boolean t1redMan;
		final boolean t1yellowMan;
		final boolean t1greenMan;
		final boolean t2redMan;
		final boolean t2yellowMan;
		final boolean t2greenMan;
		final boolean expectedT1red;
		final boolean expectedT1yellow;
		final boolean expectedT1green;
		final boolean expectedT2red;
		final boolean expectedT2yellow;
		final boolean expectedT2green;
		
		public TestTuple(boolean t1intense, boolean t2intense, boolean manual,
				boolean t1redMan, boolean t1yellowMan, boolean t1greenMan,
				boolean t2redMan, boolean t2yellowMan, boolean t2greenMan,
				boolean expectedT1red, boolean expectedT1yellow,
				boolean expectedT1green, boolean expectedT2red,
				boolean expectedT2yellow, boolean expectedT2green) {
			this.t1intense = t1intense;
			this.t2intense = t2intense;
			this.manual = manual;
			this.t1redMan = t1redMan;
			this.t1yellowMan = t1yellowMan;
			this.t1greenMan = t1greenMan;
			this.t2redMan = t2redMan;
			this.t2yellowMan = t2yellowMan;
			this.t2greenMan = t2greenMan;
			this.expectedT1red = expectedT1red;
			this.expectedT1yellow = expectedT1yellow;
			this.expectedT1green = expectedT1green;
			this.expectedT2red = expectedT2red;
			this.expectedT2yellow = expectedT2yellow;
			this.expectedT2green = expectedT2green;
		}
	}
	
	private final static String PATH_IN = "../../mywork/traffic-lights/test_stub.xml";
	private final static String PATH_OUT = "../../mywork/traffic-lights/test_out.xml";
	
	private static String testSuiteToString(List<List<TestTuple>> tests) {
		if (tests.size() > MAX_TESTS) {
			throw new AssertionError("tests.size() > " + MAX_TESTS + " is not supported for the current nondet_splitter!");
		}
		final NCES dummyNCES = new NCES(0, 10, "dummy");
		final StringBuilder sb = new StringBuilder();
		int x = -130;
		final int y = -1000 + 220;
		final ModuleInstance splitter = new ModuleInstance(x, y, "Init", "nondet_splitter");
		final ModuleInstance finished = new ModuleInstance(490, 512, "Finished", "bool_var");

		sb.append(splitter.toString());
		for (int i = 0; i < tests.size(); i++) {
			x -= 240;
			sb.append(testToString(tests.get(i), splitter, dummyNCES, i + 1, x, y));
		}
		for (int i = tests.size(); i < MAX_TESTS; i++) {
			sb.append("    " + new Arc(new Property(splitter, "start" + (i + 1)), new Property(finished, "assign_true"), ArcType.EVARC, 0, dummyNCES) + "\n");
		}
		
		return sb.toString();
	}
	
	private static String testToString(List<TestTuple> test, ModuleInstance splitter, NCES nces, int testIndex, int startX, int startY) {
		final List<ModuleInstance> moduleInstances = new ArrayList<>();
		
		final int x = startX;
		int y = startY;
		for (int i = 0; i < test.size(); i++) {
			moduleInstances.add(new ModuleInstance(x, y, "T" + testIndex + "_E" + (i + 1), "test_element"));
			y += 220;
		}
		
		final ModuleInstance comparator = new ModuleInstance(490, -262, "Comparator", "comparator");
		final ModuleInstance controller = new ModuleInstance(490, -593, "Controller", "controller");
		final ModuleInstance passed = new ModuleInstance(640, 360, "Passed", "switch");
		final ModuleInstance failed = new ModuleInstance(490, 360, "Failed", "switch");
		final ModuleInstance t1intense = new ModuleInstance(220, -634, "T1Intense", "bool_var");
		final ModuleInstance t2intense = new ModuleInstance(220, -468, "T2Intense", "bool_var");
		
		final ModuleInstance manual = new ModuleInstance(220, 0, "Manual", "bool_var");
		final ModuleInstance t1redMan = new ModuleInstance(220, 0, "T1RedMan", "bool_var");
		final ModuleInstance t1yellowMan = new ModuleInstance(220, 0, "T1YellowMan", "bool_var");
		final ModuleInstance t1greenMan = new ModuleInstance(220, 0, "T1GreenMan", "bool_var");
		final ModuleInstance t2redMan = new ModuleInstance(220, 0, "T2RedMan", "bool_var");
		final ModuleInstance t2yellowMan = new ModuleInstance(220, 0, "T2YellowMan", "bool_var");
		final ModuleInstance t2greenMan = new ModuleInstance(220, 0, "T2GreenMan", "bool_var");
		
		final ModuleInstance expectedT1red = new ModuleInstance(220, -468, "ExpectedT1Red", "bool_var");
		final ModuleInstance expectedT1yellow = new ModuleInstance(220, -132, "ExpectedT1Yellow", "bool_var");
		final ModuleInstance expectedT1green = new ModuleInstance(220, 32, "ExpectedT1Green", "bool_var");
		final ModuleInstance expectedT2red = new ModuleInstance(220, 192, "ExpectedT2Red", "bool_var");
		final ModuleInstance expectedT2yellow = new ModuleInstance(220, 361, "ExpectedT2Yellow", "bool_var");
		final ModuleInstance expectedT2green = new ModuleInstance(220, 524, "ExpectedT2Green", "bool_var");

		final TestTuple dummyTuple = new TestTuple(false, false, false, false, false, false,
				false, false, false, false, false, false, false, false, false);
		
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < test.size(); i++) {
			final ModuleInstance prevInst = i > 0 ? moduleInstances.get(i - 1) : null;
			final ModuleInstance inst = moduleInstances.get(i);
			sb.append("    " + inst.toString() + "\n");
			sb.append("    " + new Arc(new Property(comparator, "match"), new Property(inst, "match"), ArcType.CONDARC, 0, nces) + "\n");
			sb.append("    " + new Arc(new Property(comparator, "not_match"), new Property(inst, "not_match"), ArcType.CONDARC, 0, nces) + "\n");
			sb.append("    " + new Arc(new Property(comparator, "done"), new Property(inst, "compared"), ArcType.CONDARC, 0, nces) + "\n");
			sb.append("    " + new Arc(new Property(controller, "finished_cycle"), new Property(inst, "cycle_end"), ArcType.EVARC, 0, nces) + "\n");
			sb.append("    " + new Arc(new Property(inst, "set_values"), new Property(comparator, "reset"), ArcType.EVARC, 0, nces) + "\n");
			sb.append("    " + new Arc(new Property(inst, "compare"), new Property(comparator, "exec"), ArcType.EVARC, 0, nces) + "\n");
			sb.append("    " + new Arc(new Property(inst, "failed"), new Property(failed, "switch"), ArcType.EVARC, 0, nces) + "\n");
			if (prevInst != null) {
				sb.append("    " + new Arc(new Property(prevInst, "passed"), new Property(inst, "exec"), ArcType.EVARC, 0, nces) + "\n");
			}
			
			final TestTuple prevTuple = i > 0 ? test.get(i - 1) : dummyTuple;
			final TestTuple tuple = test.get(i);
			if (tuple.t1intense != prevTuple.t1intense) {
				sb.append("    " + new Arc(new Property(inst, "set_values"), varSetter(t1intense, tuple.t1intense), ArcType.EVARC, 0, nces) + "\n");
			}
			if (tuple.t2intense != prevTuple.t2intense) {
				sb.append("    " + new Arc(new Property(inst, "set_values"), varSetter(t2intense, tuple.t2intense), ArcType.EVARC, 0, nces) + "\n");
			}
			if (tuple.manual != prevTuple.manual) {
				sb.append("    " + new Arc(new Property(inst, "set_values"), varSetter(manual, tuple.manual), ArcType.EVARC, 0, nces) + "\n");
			}
			if (tuple.t1redMan != prevTuple.t1redMan) {
				sb.append("    " + new Arc(new Property(inst, "set_values"), varSetter(t1redMan, tuple.t1redMan), ArcType.EVARC, 0, nces) + "\n");
			}
			if (tuple.t1yellowMan != prevTuple.t1yellowMan) {
				sb.append("    " + new Arc(new Property(inst, "set_values"), varSetter(t1yellowMan, tuple.t1yellowMan), ArcType.EVARC, 0, nces) + "\n");
			}
			if (tuple.t1greenMan != prevTuple.t1greenMan) {
				sb.append("    " + new Arc(new Property(inst, "set_values"), varSetter(t1greenMan, tuple.t1greenMan), ArcType.EVARC, 0, nces) + "\n");
			}
			if (tuple.t2redMan != prevTuple.t2redMan) {
				sb.append("    " + new Arc(new Property(inst, "set_values"), varSetter(t2redMan, tuple.t2redMan), ArcType.EVARC, 0, nces) + "\n");
			}
			if (tuple.t2yellowMan != prevTuple.t2yellowMan) {
				sb.append("    " + new Arc(new Property(inst, "set_values"), varSetter(t2yellowMan, tuple.t2yellowMan), ArcType.EVARC, 0, nces) + "\n");
			}
			if (tuple.t2greenMan != prevTuple.t2greenMan) {
				sb.append("    " + new Arc(new Property(inst, "set_values"), varSetter(t2greenMan, tuple.t2greenMan), ArcType.EVARC, 0, nces) + "\n");
			}
			if (tuple.expectedT1red != prevTuple.expectedT1red) {
				sb.append("    " + new Arc(new Property(inst, "set_values"), varSetter(expectedT1red, tuple.expectedT1red), ArcType.EVARC, 0, nces) + "\n");
			}
			if (tuple.expectedT1yellow != prevTuple.expectedT1yellow) {
				sb.append("    " + new Arc(new Property(inst, "set_values"), varSetter(expectedT1yellow, tuple.expectedT1yellow), ArcType.EVARC, 0, nces) + "\n");
			}
			if (tuple.expectedT1green != prevTuple.expectedT1green) {
				sb.append("    " + new Arc(new Property(inst, "set_values"), varSetter(expectedT1green, tuple.expectedT1green), ArcType.EVARC, 0, nces) + "\n");
			}
			if (tuple.expectedT2red != prevTuple.expectedT2red) {
				sb.append("    " + new Arc(new Property(inst, "set_values"), varSetter(expectedT2red, tuple.expectedT2red), ArcType.EVARC, 0, nces) + "\n");
			}
			if (tuple.expectedT2yellow != prevTuple.expectedT2yellow) {
				sb.append("    " + new Arc(new Property(inst, "set_values"), varSetter(expectedT2yellow, tuple.expectedT2yellow), ArcType.EVARC, 0, nces) + "\n");
			}
			if (tuple.expectedT2green != prevTuple.expectedT2green) {
				sb.append("    " + new Arc(new Property(inst, "set_values"), varSetter(expectedT2green, tuple.expectedT2green), ArcType.EVARC, 0, nces) + "\n");
			}
		}
		sb.append("    " + new Arc(new Property(splitter, "start" + testIndex), new Property(moduleInstances.get(0), "exec"), ArcType.EVARC, 0, nces) + "\n");
		sb.append("    " + new Arc(new Property(moduleInstances.get(moduleInstances.size() - 1), "passed"), new Property(passed, "switch"), ArcType.EVARC, 0, nces) + "\n");

		return sb.toString();
	}
	
	private static TestTuple tupleFromColors(boolean t1intense, boolean t2intense, String firstColor, String secondColor) {
		boolean t1red = false, t1yellow = false, t1green = false;
		boolean t2red = false, t2yellow = false, t2green = false;
		switch (firstColor) {
		case "red": t1red = true; break;
		case "yellow": t1yellow = true; break;
		case "green": t1green = true; break;
		default: throw new AssertionError();
		}
		switch (secondColor) {
		case "red": t2red = true; break;
		case "yellow": t2yellow = true; break;
		case "green": t2green = true; break;
		default: throw new AssertionError();
		}
		return new TestTuple(t1intense, t2intense, false,
				false, false, false, false, false, false,
				t1red, t1yellow, t1green, t2red, t2yellow, t2green);
	}
	
	private static List<TestTuple> testAndFail(boolean t1intese, boolean t2intense, int delay1, int delay2, int repeat) {
		final List<TestTuple> test = new ArrayList<>();
		for (int j = 0; j < repeat; j++) {
			// only the values at the beginning should matter
			test.add(tupleFromColors(t1intese, t2intense, "green", "red")); 
			for (int i = 0; i < delay1 - 1; i++) {
				test.add(tupleFromColors(RND.nextBoolean(), RND.nextBoolean(), "green", "red")); 
			}
			test.add(tupleFromColors(RND.nextBoolean(), RND.nextBoolean(), "yellow", "red"));
			test.add(tupleFromColors(RND.nextBoolean(), RND.nextBoolean(), "red", "red"));
			for (int i = 0; i < delay2; i++) {
				test.add(tupleFromColors(RND.nextBoolean(), RND.nextBoolean(), "red", "green"));
			}
			test.add(tupleFromColors(RND.nextBoolean(), RND.nextBoolean(), "red", "yellow"));
			test.add(tupleFromColors(RND.nextBoolean(), RND.nextBoolean(), "red", "red"));
		}
		test.add(tupleFromColors(false, true, "red", "red")); // THIS ONE MUST FAIL
		return test;
	}
	
	public static void main(String[] args) {
		Arc.POINTS_RECT = false;
		final List<TestTuple> manTest = new ArrayList<>(Arrays.asList(
				new TestTuple(false, false, true, true, false, false, false, false, false, true, false, false, false, false, false),
				new TestTuple(false, false, true, false, true, false, false, false, false, false, true, false, false, false, false),
				new TestTuple(false, false, true, false, false, true, false, false, false, false, false, true, false, false, false),
				new TestTuple(false, false, true, false, false, false, true, false, false, false, false, false, true, false, false),
				new TestTuple(false, false, true, false, false, false, false, true, false, false, false, false, false, true, false),
				new TestTuple(false, false, true, false, false, false, false, false, true, false, false, false, false, false, true)
		));
		manTest.addAll(testAndFail(false, false, 23, 23, 1));
		
		final List<List<TestTuple>> tests = Arrays.asList(
				testAndFail(false, true, 16, 30, 2),
				testAndFail(true, false, 30, 16, 2),
				testAndFail(false, false, 23, 23, 2),
				manTest
		);
		
		// remove the error (in case we wish to get a proper test)
		tests.forEach(t -> t.remove(t.size() - 1));
		
		create(PATH_IN, PATH_OUT, testSuiteToString(tests));
		System.out.println(testSuiteToNuSMV(tests));
	}
	
	private static String testSuiteToNuSMV(List<List<TestTuple>> testSuite) {
		final List<Field> inputs = new ArrayList<>();
		final List<Field> outputs = new ArrayList<>();
		for (Field f : TestTuple.class.getDeclaredFields()) {
			if (f.getType().equals(boolean.class) | f.getType().equals(Boolean.class)) {
				if (f.getName().startsWith("expected")) {
					outputs.add(f);
				} else {
					inputs.add(f);
				}
			}
		}
		
		final StringBuilder sb = new StringBuilder();
		sb.append("-- CTLSPEC AG (element_passed);\n");
		sb.append("-- CTLSPEC AF (finished & passed_so_far);\n");
		sb.append("MODULE TEST_SUITE(" + String.join(", ",
				outputs.stream().map(f -> f.getName()).collect(Collectors.toList())) + ")\n");
		sb.append("VAR\n");
		final int maxSteps = testSuite.stream().mapToInt(t -> t.size()).max().getAsInt();
		sb.append("    test: 0.." + (testSuite.size() - 1) + ";\n");
		sb.append("    step: 0.." + maxSteps + ";\n");
		sb.append("    passed_so_far: boolean;\n");
		sb.append("ASSIGN\n");
		sb.append("    next(test) := test;\n");
		sb.append("    init(step) := 0;\n");
		sb.append("    next(step) := (finished | step = " + maxSteps + ") ? step : (step + 1);\n");
		sb.append("    init(passed_so_far) := element_passed;\n");
		sb.append("    next(passed_so_far) := passed_so_far & next(element_passed);\n");
		sb.append("DEFINE\n");
		sb.append("    finished := case\n");
		for (int i = 0; i < testSuite.size(); i++) {
			sb.append("        test = " + i + ": step = " + testSuite.get(i).size() + ";\n");
		}
		sb.append("    esac;\n");

		for (Field f : inputs) {
			sb.append("    " + f.getName() + " := case\n");
			for (int i = 0; i < testSuite.size(); i++) {
				for (int j = 0; j < testSuite.get(i).size(); j++) {
					try {
						sb.append("        test = " + i + " & step = " + j
								+ ": " + f.get(testSuite.get(i).get(j)).toString().toUpperCase() + ";\n");
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
			sb.append("        TRUE: FALSE;\n");
			sb.append("    esac;\n");
		}
		sb.append("    element_passed := case\n");
		for (int i = 0; i < testSuite.size(); i++) {
			for (int j = 0; j < testSuite.get(i).size(); j++) {
				final List<String> constraints = new ArrayList<>();
				for (Field f : outputs) {
					try {
						constraints.add(((Boolean) (f.get(testSuite.get(i).get(j))) ? "" : "!") + f.getName());
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
				}
				sb.append("        test = " + i + " & step = " + j
						+ ": " + String.join(" & ", constraints) + ";\n");
			}
		}
		sb.append("        TRUE: TRUE;\n");
		sb.append("    esac;\n");
		
		return sb.toString();
	}
}
