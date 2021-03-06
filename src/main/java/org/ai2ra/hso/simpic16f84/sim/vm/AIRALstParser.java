package org.ai2ra.hso.simpic16f84.sim.vm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Utility class for parsing a LST file. Basically it extracts the machine instructions
 * in form of integers. This implementation is depended to a <b>custom</b> LST file
 * version used at the University of Applied Science Offenburg. It may not work for
 * other kind of formats.
 *
 * @author Freddy1096
 */

public class AIRALstParser implements LstParser<Short> {

    /**
     * Parses the machine instructions from a given LST file. Important to note is, that
     * this parser <b>only</b> works for a specific LST file syntax. For more details
     * see testing LST files or method implementation.
     *
     * @param file The LST file path
     * @return Returns the machine instructions as array of integers
     * @throws IOException           Thrown if the given file couldn't be opened
     * @throws NumberFormatException Thrown if the LST file is malformed, instruction couldn't be parsed
     */

	@Override
    public Short[] parse(File file) throws IOException {
		
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
				
			converter.add(testLine.substring(5, 9));
		}
		inputFile.close();
		
		/* Array that will output the machine code 
		 */
        Short[] output = new Short[converter.size()];
		
		/* Loop that decodes the Strings of the Arraylist into an int and saves it into the output Array.
		 * The input string is decoded from Hex to int.
		 * The index of the output Array corresponds to the index of the machine code.
		*/
		for (int i = 0; i < converter.size(); i++) {
            output[i] = Short.decode("0x" + converter.get(i));
		}
		
		return output;
	}
}
