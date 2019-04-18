package org.ai2ra.hso.simpic16f84.sim.mem;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;

public class EepromMemory<T> implements ObservableMemory<T> {


    private T[] memory;
    private PropertyChangeSupport changes;

    @SuppressWarnings("unchecked")
    public EepromMemory(int size) {

        this.memory = (T[]) new Object[size];
        changes = new PropertyChangeSupport(this);

    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changes.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changes.removePropertyChangeListener(listener);
    }

    @Override
    public T get(int address) throws MemoryIndexOutOfBoundsException {

        if (memory.length == 0 || address > memory.length || address < 0){

            throw new MemoryIndexOutOfBoundsException();

        }
        return memory[address];

    }

    @Override
    public T[] fetch() {

        if (memory.length == 0){

            throw new MemoryIndexOutOfBoundsException();

        }
        return Arrays.copyOf(memory, memory.length);
    }


    public void set(T toSet, int address) {

        if (memory.length == 0 || address > memory.length || address < 0){

            throw new MemoryIndexOutOfBoundsException();

        }else{
            T beforeSet = memory[address];
            this.memory[address] = toSet;
            changes.firePropertyChange(String.format("memory[%d]", address), beforeSet, toSet);

        }

    }


    public void reset(){

        T[] copy = fetch();
        Arrays.fill(memory, null);
        changes.firePropertyChange("memory", copy, memory );

    }
}


