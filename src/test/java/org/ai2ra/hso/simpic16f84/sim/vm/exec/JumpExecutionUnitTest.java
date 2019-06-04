package org.ai2ra.hso.simpic16f84.sim.vm.exec;

import org.ai2ra.hso.simpic16f84.sim.mem.RamMemory;
import org.ai2ra.hso.simpic16f84.sim.mem.StackMemory;
import org.ai2ra.hso.simpic16f84.sim.vm.Instruction;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JumpExecutionUnitTest {

    @Mock InstructionExecutor executor;

    private JumpExecutionUnit executionUnit;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {

        executor.stack = (StackMemory<Integer>) mock(StackMemory.class);
        executor.ram = (RamMemory<Byte>) mock(RamMemory.class);

        executionUnit = new JumpExecutionUnit(executor);
    }

    @Test
    public void executeCALL() {

        when(executor.getProgramCounter()).thenReturn(0x10);
        when(executor.ram.get(RamMemory.SFR.PCLATH)).thenReturn((byte) 0b0000_1000);

        executionUnit.executeCALL(new Instruction(Instruction.OperationCode.CALL, 0x05));

        verify(executor.stack).push(0x010);
        verify(executor).setProgramCounter(0x0805);
    }

    @Test
    public void executeGOTO() {

        when(executor.getProgramCounter()).thenReturn(0x10);
        when(executor.ram.get(RamMemory.SFR.PCLATH)).thenReturn((byte) 0b0000_1000);

        executionUnit.executeGOTO(new Instruction(Instruction.OperationCode.GOTO, 0x05));

        verify(executor).setProgramCounter(0x0805);
    }
}