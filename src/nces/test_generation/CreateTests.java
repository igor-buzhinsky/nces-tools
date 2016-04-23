package nces.test_generation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import nces.structure.ModuleInstance;
import nces.structure.Property;

public abstract class CreateTests {
	protected static final Random RND = new Random(100);

	protected static Property varSetter(ModuleInstance var, boolean value) {
		return new Property(var, value ? "assign_true" : "assign_false");
	}
	
	protected static void create(String pathIn, String pathOut, String testString) {
		List<String> lines = null;
		try (BufferedReader s = new BufferedReader(new FileReader(new File(pathIn)))) {
			lines = s.lines().collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		lines.set(1, lines.get(1).replace("test_stub", "test_out"));
		final String firstLines = String.join("\n", lines.subList(0, lines.size() - 3));
		final String lastLines = String.join("\n", lines.subList(lines.size() - 3, lines.size()));
		
		try (PrintWriter pw = new PrintWriter(new File(pathOut))) {
			pw.println(firstLines);
			pw.println(testString);
			pw.println(lastLines);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		System.out.println("Done.");
	}
}
