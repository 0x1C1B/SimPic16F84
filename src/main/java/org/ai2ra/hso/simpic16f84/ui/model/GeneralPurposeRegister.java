package org.ai2ra.hso.simpic16f84.ui.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Interface model representing a <i>General Purpose Register</i> of the Pic16F84. Primarily
 * used for representing such data inside a JavaFX component.
 *
 * @author 0x1C1B
 */

public class GeneralPurposeRegister {

    private IntegerProperty address;
    private IntegerProperty value;

    public GeneralPurposeRegister() {

        this.address = new SimpleIntegerProperty();
        this.value = new SimpleIntegerProperty();
    }

    public int getAddress() {

        return address.get();
    }

    public IntegerProperty addressProperty() {

        return address;
    }

    public void setAddress(int address) {
        this.address.set(address);
    }

    public int getValue() {

        return value.get();
    }

    public IntegerProperty valueProperty() {

        return value;
    }

    public void setValue(int value) {

        this.value.set(value);
    }
}
