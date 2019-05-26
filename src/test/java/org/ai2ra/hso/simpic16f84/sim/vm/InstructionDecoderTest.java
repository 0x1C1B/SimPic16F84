package org.ai2ra.hso.simpic16f84.sim.vm;

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

                {0x3E25, new Instruction(Instruction.OperationCode.ADDLW, 0x25)}, // ADDLW 0x25
                {0x3930, new Instruction(Instruction.OperationCode.ANDLW, 0x30)}, // ANDLW 0x30
                {0x3821, new Instruction(Instruction.OperationCode.IORLW, 0x21)}, // IORLW 0x21
                {0x3011, new Instruction(Instruction.OperationCode.MOVLW, 0x11)}, // MOVLW 0x11
                {0x34FF, new Instruction(Instruction.OperationCode.RETLW, 0xFF)}, // RETLW 0xFF
                {0x3C00, new Instruction(Instruction.OperationCode.SUBLW, 0x00)}, // SUBLW 0x00
                {0x3A11, new Instruction(Instruction.OperationCode.XORLW, 0x11)}, // XORLW 0x11

                // BIT-ORIENTED FILE REGISTER OPERATION

                {0x1283, new Instruction(Instruction.OperationCode.BCF, 0x05, 0x03)}, // BCF 0x03, 0x05
                {0x158F, new Instruction(Instruction.OperationCode.BSF, 0x03, 0x0F)}, // BSF 0x0F, 0x03
                {0x192E, new Instruction(Instruction.OperationCode.BTFSC, 0x02, 0x2E)}, // BTFSC 0x2E, 0x03
                {0x1C01, new Instruction(Instruction.OperationCode.BTFSS, 0x00, 0x01)}, // BTFSS 0x01, 0x05

                // CONTROL OPERATIONS

                {0x0063, new Instruction(Instruction.OperationCode.SLEEP)}, // SLEEP
                {0x0008, new Instruction(Instruction.OperationCode.RETURN)}, // RETURN
                {0x0009, new Instruction(Instruction.OperationCode.RETFIE)}, // RETFIE
                {0x0064, new Instruction(Instruction.OperationCode.CLRWDT)}, // CLRWDT

                // JUMP OPERATIONS

                {0x2805, new Instruction(Instruction.OperationCode.GOTO, 0x05)}, // GOTO 0x05
                {0x20FF, new Instruction(Instruction.OperationCode.CALL, 0xFF)}, // CALL 0xFF

                // BYTE-ORIENTED OPERATIONS

                {0x0730, new Instruction(Instruction.OperationCode.ADDWF, 0x00, 0x30)}, // ADDWF 0x30
                {0x0186, new Instruction(Instruction.OperationCode.CLRF, 0x06)}, // CLRF 0x06
                {0x008C, new Instruction(Instruction.OperationCode.MOVWF, 0x0C)} // MOVWF
        });
    }

    @Parameterized.Parameter public int instruction;
    @Parameterized.Parameter(1) public Instruction expected;

    @Test public void testDecode() {

        Instruction decoded = InstructionDecoder.decode(instruction);

        assertEquals(String.format("Operation code doesn't match [0x%04X]", instruction),
                expected.getOpc(), decoded.getOpc());

        assertEquals(String.format("Number of arguments doesn't match [0x%04X]", instruction),
                expected.getArguments().length, decoded.getArguments().length);

        assertArrayEquals(String.format("Decoded instruction is invalid [0x%04X]", instruction),
                expected.getArguments(), decoded.getArguments());
    }
}