package org.ai2ra.hso.simpic16f84.sim;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class LstParser {
		
	public static int[] parse(File file) throws IOException {
		
		FileReader input = new FileReader(file);
		BufferedReader inputFile = new BufferedReader(input);
		ArrayList<String> converter = new ArrayList<>();
		String testLine;
		
		/* loop that checks every line of a given LST file for machine language 
		 * and saves it into an ArrayList as a String. 
		*/
		while ((testLine = inputFile.readLine()) != null) {
			
			if( testLine.startsWith(" ")) {
				continue;
			}
				
			converter.add(testLine.substring(5, 8));
		}
		inputFile.close();
		
		/* Array that will output the machine code 
		 */
		int[] output = new int[converter.size()];
		
		/* Loop that decodes the Strings of the Arraylist into an int and saves it into the output Array.
		 * The input string is decoded from Hex to int.
		 * The index of the output Array corresponds to the index of the machine code.
		*/
		for (int i = 0; i < converter.size(); i++) {
			int insert = Integer.decode("0x" + converter.get(i));
			output[i] = insert;
		}
		
		return output;
	}
	
	
	
}
