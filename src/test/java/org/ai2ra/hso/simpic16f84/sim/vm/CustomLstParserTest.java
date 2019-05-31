package org.ai2ra.hso.simpic16f84.sim.vm;

import static org.junit.Assert.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

public class CustomLstParserTest {

    private LstParser<Short> parser;

	@Before
	public void setUp() throws Exception {

		parser = new CustomLstParser();
	}

	@Test
	public void testTPicSim1() throws IOException, URISyntaxException {

		Path path = Paths.get(getClass().getResource("/LstFiles/TPicSim1.LST").toURI());

        Short[] instructions = parser.parse(path.toFile());
        assertArrayEquals(instructions, new Short[]{0x3011, 0x3930, 0x380D, 0x3C3D, 0x3A20, 0x3E25, 0x2806});
	}

	@Test
	public void testTPicSim12() throws IOException, URISyntaxException {

		Path path = Paths.get(getClass().getResource("/LstFiles/TPicSim2.LST").toURI());

        Short[] instructions = parser.parse(path.toFile());
        assertArrayEquals(instructions, new Short[]{0x3011, 0x2006, 0x0000, 0x2008, 0x0000, 0x2800, 0x3E25, 0x0008, 0x3477, 0x2809});
	}




}
