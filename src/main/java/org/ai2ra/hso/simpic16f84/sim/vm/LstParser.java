package org.ai2ra.hso.simpic16f84.sim.vm;

import java.io.File;
import java.io.IOException;

/**
 * Utility class for parsing a LST file. Basically it extracts the machine instructions
 * in form of integers.
 *
 * @author Freddy1096
 */

public interface LstParser {

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

    int[] parse(File file) throws IOException;
}
