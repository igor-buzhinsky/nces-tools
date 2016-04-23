package nces.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import nces.misc.Grid;
import nces.misc.Num;
import nces.structure.Arc.ArcType;

public class NCES extends NcesObject {
	private final int inputX;
	private final int outputX;
	private final String name;
	private final List<IOConnection> inputVars = new ArrayList<>();
	private final List<IOConnection> outputVars = new ArrayList<>();
	private final List<IOConnection> inputEvents = new ArrayList<>();
	private final List<IOConnection> outputEvents = new ArrayList<>();
	private final List<Place> places = new ArrayList<>();
	private final List<Transition> transitions = new ArrayList<>();
	private final List<Arc> arcs = new ArrayList<>();
	
	public NCES(int inputX, int outputX, String name) {
		this.inputX = inputX;
		this.outputX = outputX;
		this.name = name;
	}
	
	public void addInputVar(IOConnection connection) {
		inputVars.add(connection);
	}
	
	public void addOutputVar(IOConnection connection) {
		outputVars.add(connection);
	}
	
	public void addInputEvent(IOConnection connection) {
		inputEvents.add(connection);
	}
	
	public void addOutputEvent(IOConnection connection) {
		outputEvents.add(connection);
	}
	
	public void addTransition(Transition t) {
		transitions.add(t);
	}
	
	public void addPlace(Place p) {
		places.add(p);
	}
	
	public void addArc(Arc arc) {
		arcs.add(arc);
	}

	public void arcPair(Place from, Transition t, Place to, int time, NamedAndPositionedObject... conditions) {
		new Arc(from, t, ArcType.ARC, time, this);
		new Arc(t, to, ArcType.ARC, 0, this);
		for (NamedAndPositionedObject cond : conditions) {
			new Arc(cond, t, ArcType.CONDARC, 0, this);
		}
	}
	
	public Transition dummyTransition(Grid grid, Num num) {
		int index = num.num();
		Transition t = new Transition(grid, num, false, this, "t_dummy_" + index);
		Place dummyPlace = new Place(grid, "p_dummy_" + index, num, true, this);
		arcPair(dummyPlace, t, dummyPlace, 0);
		return t;
	}
	
	private String objectsToString(List<?> list, String declaration) {
		return list.isEmpty() ? "" : ("<" + declaration + ">\n"
				+ String.join("\n", list.stream().map(Object::toString).collect(Collectors.toList()))
				+ "\n</" + declaration + ">\n");
	}
	
	@Override
	public String toString() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<FBType xmlns:xsi=" + quote("http://www.w3.org/2001/XMLSchema-instance")
				+ " xsi:noNamespaceSchemaLocation=" + quote("V:\\Verification\\ViVe\\xmlSchema\\NCESModuleNetworkExtended.xsd")
				+ " X=" + quote(0) + " Y=" + quote(0) + " Num=" + quote(0) + " LocNum=" + quote(0)
				+ " Name=" + quote(name) + " Comment=" + quote("_") + " Width=" + quote(232.0)
				+ " Height=" + quote(419.0) + ">\n"
				+ "<VersionInfo Copyright=\"\" FileVersion=\"1\" EditorVersion=\"\" Author=\"\" Date=\"2015-03-22\" Description=\"\" />\n"
				+ "<InterfaceList>\n"
				+ objectsToString(inputEvents, "EventInputs")
				+ objectsToString(outputEvents, "EventOutputs")
				+ objectsToString(inputVars, "InputVars")
				+ objectsToString(outputVars, "OutputVars")
				+ "</InterfaceList>\n"
				+ "<SNS LeftPageBorder=" + quote(inputX + ".0") + " RightPageBorder=" + quote(outputX + ".0") + ">\n"
				+ String.join("\n", places.stream().map(Object::toString).collect(Collectors.toList()))
				+ String.join("\n", transitions.stream().map(Object::toString).collect(Collectors.toList()))
				+ String.join("\n", arcs.stream().map(Object::toString).collect(Collectors.toList()))
				+ "\n</SNS>\n"
				+ "</FBType>\n";
	}
}
