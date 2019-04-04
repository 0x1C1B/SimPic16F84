package org.ai2ra.hso.simpic16f84.sim;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class InstructionDecoderTest {

    @Parameterized.Parameters public static Collection<Object[]> parameters() {

        return Arrays.asList(new Object[][]{

                {0x3E25, new int[] {0x3E00, 0x25}}, // ADDLW 0x25
                {0x3930, new int[] {0x3900, 0x30}}, // ANDLW 0x30
                {0x3821, new int[] {0x3800, 0x21}}, // IORLW 0x21
                {0x3011, new int[] {0x3000, 0x11}}, // MOVLW 0x11
                {0x34FF, new int[] {0x3400, 0xFF}}, // RETLW 0xFF
                {0x3C00, new int[] {0x3C00, 0x00}}, // SUBLW 0x00
                {0x3A11, new int[] {0x3A00, 0x11}}, // XORLW 0x11
                {0x2805, new int[] {0x2800, 0x05}}, // GOTO 0x05
                {0x1283, new int[] {0x1000, 0x05, 0x03}}, // BCF
                {0x0063, new int[] {0x0063}}, // SLEEP
                {0x0186, new int[] {0x0180, 0x06}}, // CLRF
                {0x0008, new int[] {0x0008}}, // RETURN
                {0x008C, new int[] {0x0080, 0x0C}} // MOVWF
        });
    }

    @Parameterized.Parameter public int instruction;
    @Parameterized.Parameter(1) public int[] expected;

    @Test public void testDecode() {

        int[] decoded = InstructionDecoder.decode(instruction);

        assertEquals("Amount of arguments doesn't match", expected.length, decoded.length);
        assertArrayEquals("Decoded instruction is invalid", expected, decoded);
    }
}