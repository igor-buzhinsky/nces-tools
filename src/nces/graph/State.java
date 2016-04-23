package nces.graph;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

public class State {
	private final int[] marking;
	
	public State(List<Integer> marking) {
		this.marking = ArrayUtils.toPrimitive(marking.toArray(new Integer[marking.size()]));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < marking.length; i++) {
			if (i != 0) {
				sb.append(", ");
			}
			sb.append(i + ":" + marking[i]);
		}
		return "State[" + sb.toString() + "]";
	}
	
	public int size() {
		return marking.length;
	}
	
	public int get(int index) {
		return marking[index];
	}
	
	public List<Integer> markingCopy() {
		return Arrays.asList(ArrayUtils.toObject(marking));
	}
}