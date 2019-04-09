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

                // LITERAL OPERATIONS

                {0x3E25, new int[] {0x3E00, 0x25}}, // ADDLW 0x25
                {0x3930, new int[] {0x3900, 0x30}}, // ANDLW 0x30
                {0x3821, new int[] {0x3800, 0x21}}, // IORLW 0x21
                {0x3011, new int[] {0x3000, 0x11}}, // MOVLW 0x11
                {0x34FF, new int[] {0x3400, 0xFF}}, // RETLW 0xFF
                {0x3C00, new int[] {0x3C00, 0x00}}, // SUBLW 0x00
                {0x3A11, new int[] {0x3A00, 0x11}}, // XORLW 0x11

                // BIT-ORIENTED FILE REGISTER OPERATION

                {0x1283, new int[] {0x1000, 0x05, 0x03}}, // BCF 0x03, 0x05
                {0x158F, new int[] {0x1400, 0x03, 0x0F}}, // BSF 0x0F, 0x03
                {0x192E, new int[] {0x1800, 0x02, 0x2E}}, // BTFSC 0x2E, 0x02
                {0x1C01, new int[] {0x1C00, 0x00, 0x01}}, // BTFSS 0x01, 0x00

                // CONTROL OPERATIONS

                {0x0063, new int[] {0x0063}}, // SLEEP
                {0x0008, new int[] {0x0008}}, // RETURN
                {0x0009, new int[] {0x0009}}, // RETFIE
                {0x0064, new int[] {0x0064}}, // CLRWDT

                // JUMP OPERATIONS

                {0x2805, new int[] {0x2800, 0x05}}, // GOTO 0x05
                {0x20FF, new int[] {0x2000, 0xFF}}, // CALL 0xFF

                // BYTE-ORIENTED OPERATIONS

                {0x0730, new int[] {0x0700, 0x00, 0x30}}, // ADDWF
                {0x0186, new int[] {0x0180, 0x06}}, // CLRF
                {0x008C, new int[] {0x0080, 0x0C}} // MOVWF
        });
    }

    @Parameterized.Parameter public int instruction;
    @Parameterized.Parameter(1) public int[] expected;

    @Test public void testDecode() {

        int[] decoded = InstructionDecoder.decode(instruction);

        assertEquals(String.format("Number of arguments doesn't match [0x%04X]", instruction),
                expected.length, decoded.length);

        assertArrayEquals(String.format("Decoded instruction is invalid [0x%04X]", instruction),
                expected, decoded);
    }
}