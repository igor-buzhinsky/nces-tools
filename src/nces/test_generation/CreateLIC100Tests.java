package nces.test_generation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nces.structure.Arc;
import nces.structure.Arc.ArcType;
import nces.structure.ModuleInstance;
import nces.structure.NCES;
import nces.structure.Property;

public class CreateLIC100Tests extends CreateTests {
	private static final int MAX_TESTS = 10;
	
	static class TestTuple {
		final boolean aboveHH;
		final boolean aboveH;
		final boolean aboveThreshold;
		final boolean aboveSetpoint;
		final boolean belowSetpoint;
		final boolean belowThreshold;
		final boolean belowL;
		final boolean belowLL;
		final boolean sensorDry;
		final boolean expectedValveOpen;
		final boolean expectedAlarmOn;
		
		public TestTuple(boolean aboveHH, boolean aboveH,
				boolean aboveThreshold, boolean aboveSetpoint,
				boolean belowSetpoint, boolean belowThreshold, boolean belowL,
				boolean belowLL, boolean sensorDry, boolean expectedValveOpen,
				boolean expectedAlarmOn) {
			this.aboveHH = aboveHH;
			this.aboveH = aboveH;
			this.aboveThreshold = aboveThreshold;
			this.aboveSetpoint = aboveSetpoint;
			this.belowSetpoint = belowSetpoint;
			this.belowThreshold = belowThreshold;
			this.belowL = belowL;
			this.belowLL = belowLL;
			this.sensorDry = sensorDry;
			this.expectedValveOpen = expectedValveOpen;
			this.expectedAlarmOn = expectedAlarmOn;
		}
	}
	
	private final static String PATH_IN = "../../mywork/lic100/test_stub.xml";
	private final static String PATH_OUT = "../../mywork/lic100/test_out.xml";
	
	private static String testSuiteToString(List<List<TestTuple>> tests) {
		if (tests.size() > MAX_TESTS) {
			throw new AssertionError("tests.size() > " + MAX_TESTS + " is not supported for the current nondet_splitter!");
		}
		final NCES dummyNCES = new NCES(0, 10, "dummy");
		final StringBuilder sb = new StringBuilder();
		int x = -420;
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
		
		final ModuleInstance aboveHH = new ModuleInstance(220, 0, "AboveHH", "bool_var");
		final ModuleInstance aboveH = new ModuleInstance(220, 0, "AboveH", "bool_var");
		final ModuleInstance aboveThreshold = new ModuleInstance(220, 0, "AboveThreshold", "bool_var");
		final ModuleInstance aboveSetpoint = new ModuleInstance(220, 0, "AboveSetpoint", "bool_var");
		final ModuleInstance belowSetpoint = new ModuleInstance(220, 0, "BelowSetpoint", "bool_var");
		final ModuleInstance belowThreshold = new ModuleInstance(220, 0, "BelowThreshold", "bool_var");
		final ModuleInstance belowL = new ModuleInstance(220, 0, "BelowL", "bool_var");
		final ModuleInstance belowLL = new ModuleInstance(220, 0, "BelowLL", "bool_var");
		final ModuleInstance sensorDry = new ModuleInstance(220, 0, "SensorDry", "bool_var");

		final ModuleInstance expectedValveOpen = new ModuleInstance(220, 0, "ExpectedValveOpen", "bool_var");
		final ModuleInstance expectedAlarmOn = new ModuleInstance(220, 0, "ExpectedAlarmOn", "bool_var");

		final TestTuple dummyTuple = new TestTuple(false, false, false, false, false, false,
				false, false, false, false, false);
		
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
			if (tuple.aboveHH != prevTuple.aboveHH) {
				sb.append("    " + new Arc(new Property(inst, "set_values"), varSetter(aboveHH, tuple.aboveHH), ArcType.EVARC, 0, nces) + "\n");
			}
			if (tuple.aboveH != prevTuple.aboveH) {
				sb.append("    " + new Arc(new Property(inst, "set_values"), varSetter(aboveH, tuple.aboveH), ArcType.EVARC, 0, nces) + "\n");
			}
			if (tuple.aboveThreshold != prevTuple.aboveThreshold) {
				sb.append("    " + new Arc(new Property(inst, "set_values"), varSetter(aboveThreshold, tuple.aboveThreshold), ArcType.EVARC, 0, nces) + "\n");
			}
			if (tuple.aboveSetpoint != prevTuple.aboveSetpoint) {
				sb.append("    " + new Arc(new Property(inst, "set_values"), varSetter(aboveSetpoint, tuple.aboveSetpoint), ArcType.EVARC, 0, nces) + "\n");
			}
			if (tuple.belowSetpoint != prevTuple.belowSetpoint) {
				sb.append("    " + new Arc(new Property(inst, "set_values"), varSetter(belowSetpoint, tuple.belowSetpoint), ArcType.EVARC, 0, nces) + "\n");
			}
			if (tuple.belowThreshold != prevTuple.belowThreshold) {
				sb.append("    " + new Arc(new Property(inst, "set_values"), varSetter(belowThreshold, tuple.belowThreshold), ArcType.EVARC, 0, nces) + "\n");
			}
			if (tuple.belowL != prevTuple.belowL) {
				sb.append("    " + new Arc(new Property(inst, "set_values"), varSetter(belowL, tuple.belowL), ArcType.EVARC, 0, nces) + "\n");
			}
			if (tuple.belowLL != prevTuple.belowLL) {
				sb.append("    " + new Arc(new Property(inst, "set_values"), varSetter(belowLL, tuple.belowLL), ArcType.EVARC, 0, nces) + "\n");
			}
			if (tuple.sensorDry != prevTuple.sensorDry) {
				sb.append("    " + new Arc(new Property(inst, "set_values"), varSetter(sensorDry, tuple.sensorDry), ArcType.EVARC, 0, nces) + "\n");
			}
			if (tuple.expectedValveOpen != prevTuple.expectedValveOpen) {
				sb.append("    " + new Arc(new Property(inst, "set_values"), varSetter(expectedValveOpen, tuple.expectedValveOpen), ArcType.EVARC, 0, nces) + "\n");
			}
			if (tuple.expectedAlarmOn != prevTuple.expectedAlarmOn) {
				sb.append("    " + new Arc(new Property(inst, "set_values"), varSetter(expectedAlarmOn, tuple.expectedAlarmOn), ArcType.EVARC, 0, nces) + "\n");
			}
		}
		sb.append("    " + new Arc(new Property(splitter, "start" + testIndex), new Property(moduleInstances.get(0), "exec"), ArcType.EVARC, 0, nces) + "\n");
		sb.append("    " + new Arc(new Property(moduleInstances.get(moduleInstances.size() - 1), "passed"), new Property(passed, "switch"), ArcType.EVARC, 0, nces) + "\n");

		return sb.toString();
	}

	private static TestTuple tuple(String level, boolean valveOpen, boolean alarmOn) {
		switch (level) {
		case "hh+": 	   return new TestTuple(true, false, false, false, false, false, false, false, false, valveOpen, alarmOn);
		case "h+": 		   return new TestTuple(false, true, false, false, false, false, false, false, true, valveOpen, alarmOn);
		case "threshold+": return new TestTuple(false, false, true, false, false, false, false, false, true, valveOpen, alarmOn);
		case "setpoint+":  return new TestTuple(false, false, false, true, false, false, false, false, true, valveOpen, alarmOn);
		case "setpoint-":  return new TestTuple(false, false, false, false, true, false, false, false, true, valveOpen, alarmOn);
		case "threshold-": return new TestTuple(false, false, false, false, false, true, false, false, true, valveOpen, alarmOn);
		case "l-": 		   return new TestTuple(false, false, false, false, false, false, true, false, true, valveOpen, alarmOn);
		case "ll-": 	   return new TestTuple(false, false, false, false, false, false, false, true, true, valveOpen, alarmOn);
		default: throw new AssertionError();
		}
	}
	
	public static void main(String[] args) {
		Arc.POINTS_RECT = false;
		
		final List<TestTuple> test1 = new ArrayList<>(Arrays.asList(
				tuple("setpoint+", false, false), tuple("setpoint-", false, false),
				tuple("setpoint+", false, false), tuple("setpoint-", false, false),
				tuple("setpoint-", true, false) // must fail
		));
		
		// overflow: can do nothing
		final List<TestTuple> test2 = new ArrayList<>(Arrays.asList(
				tuple("setpoint+", false, false), tuple("threshold+", false, false)
		));
		
		// underflow: must open the valve; then keep; then close after overflow
		final List<TestTuple> test3 = new ArrayList<>(Arrays.asList(
				tuple("setpoint-", false, false), tuple("threshold-", true, false),
				tuple("setpoint-", true, false), tuple("setpoint+", true, false), tuple("h+", false, false)
		));
		
		// bi2 alarm test
		final List<TestTuple> test4 = new ArrayList<>(Arrays.asList(
				tuple("threshold-", true, false), tuple("hh+", false, true)
		));
		
		final List<List<TestTuple>> tests = Arrays.asList(
				test1, test2, test3, test4
		);
		
		create(PATH_IN, PATH_OUT, testSuiteToString(tests));
	}
}
