package org.ai2ra.hso.simpic16f84.sim.vm.exec;

import org.ai2ra.hso.simpic16f84.sim.mem.StackMemory;
import org.ai2ra.hso.simpic16f84.sim.vm.Instruction;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LiteralExecutionUnitTest {

    @Mock InstructionExecutor executor;

    private LiteralExecutionUnit executionUnit;

    @Before
    public void setUp() throws Exception {

        executionUnit = new LiteralExecutionUnit(executor);
    }

    @Test
    public void executeADDLW() {

        when(executor.getWorkingRegister()).thenReturn((byte) 0);

        executionUnit.executeADDLW(new Instruction(Instruction.OperationCode.ADDLW, 5));

        verify(executor).checkZeroFlag(5);
        verify(executor).checkCarryFlag(5);
        verify(executor).checkDigitCarryFlag(false);
        verify(executor).setWorkingRegister((byte) 5);
    }

    @Test
    public void executeSUBLW() {

        when(executor.getWorkingRegister()).thenReturn((byte) 3);

        executionUnit.executeSUBLW(new Instruction(Instruction.OperationCode.SUBLW, 7));

        verify(executor).checkZeroFlag(4);
        verify(executor).checkCarryFlag(4);
        verify(executor).checkDigitCarryFlag(true);
        verify(executor).setWorkingRegister((byte) 4);
    }

    @Test
    public void executeANDLW() {

        when(executor.getWorkingRegister()).thenReturn((byte) 3);

        executionUnit.executeANDLW(new Instruction(Instruction.OperationCode.ANDLW, 5));

        verify(executor).checkZeroFlag(1);
        verify(executor).setWorkingRegister((byte) 1);
    }

    @Test
    public void executeMOVLW() {

        executionUnit.executeMOVLW(new Instruction(Instruction.OperationCode.MOVLW, 35));

        verify(executor).setWorkingRegister((byte) 35);
    }

    @Test
    public void executeIORLW() {

        when(executor.getWorkingRegister()).thenReturn((byte) 5);

        executionUnit.executeIORLW(new Instruction(Instruction.OperationCode.IORLW, 10));

        verify(executor).checkZeroFlag((byte) 15);
        verify(executor).setWorkingRegister((byte) 15);
    }

    @Test
    public void executeXORLW() {

        when(executor.getWorkingRegister()).thenReturn((byte) 10);

        executionUnit.executeXORLW(new Instruction(Instruction.OperationCode.XORLW, 10));

        verify(executor).checkZeroFlag((byte) 0);
        verify(executor).setWorkingRegister((byte) 0);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void executeRETLW() {

        executor.stack = (StackMemory<Integer>) mock(StackMemory.class);
        when(executor.stack.pop()).thenReturn(0x02);

        executionUnit.executeRETLW(new Instruction(Instruction.OperationCode.RETLW, 22));

        verify(executor).setProgramCounter(0x02);
        verify(executor).setWorkingRegister((byte) 22);
    }
}