package org.ai2ra.hso.simpic16f84.sim.vm.exec;

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
}