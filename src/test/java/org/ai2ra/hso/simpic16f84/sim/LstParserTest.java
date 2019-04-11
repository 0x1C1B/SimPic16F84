package org.ai2ra.hso.simpic16f84.sim;

import static org.ai2ra.hso.simpic16f84.sim.LstParser.parse;
import static org.junit.Assert.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import javafx.stage.FileChooser;
import org.junit.Test;

import javax.swing.*;

public class LstParserTest {

	public LstParserTest() {
	}

	@Test
	public void test() throws IOException {

		JFileChooser chooser = new JFileChooser();
		int rueckgabe = chooser.showOpenDialog(null);
		File tester;
		if(rueckgabe == JFileChooser.APPROVE_OPTION)
			tester = chooser.getSelectedFile();
		else return;

		int[] testee = parse(tester);


		System.out.println(Arrays.toString(testee));
	}






}
