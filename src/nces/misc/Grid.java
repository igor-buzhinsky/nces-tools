package nces.misc;

import java.util.Random;

public class Grid {
	private int count = 0;
	private final int inputX;
	private final int outputX;
	private final int itemsInRow;
	private final Random rnd;
	private final int jitter;
	private static final int SCALE = 70;
	
	public Grid(int inputX, int outputX, int itemsInRow, Random rnd, int jitter) {
		this.inputX = inputX;
		this.outputX = outputX;
		this.itemsInRow = itemsInRow;
		this.rnd = rnd;
		this.jitter = jitter;
	}
	
	public int x() {
		return (int) (inputX + (0.1 + (count % itemsInRow)) * (outputX - inputX) / (itemsInRow + 0.1))
				+ rnd.nextInt(jitter * 2 + 1) - jitter;
	}
	
	public int y() {
		return (1 + count / itemsInRow) * SCALE
				+ ((count % itemsInRow) % 2 == 0 ? 0 : (SCALE / 2))
				+ rnd.nextInt(jitter * 2 + 1) - jitter;
	}
	
	public void increment() {
		count++;
	}
	
	public void nextLine() {
		while (count % itemsInRow != 0) {
			count++;
		}
	}
}
