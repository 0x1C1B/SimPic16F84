package org.ai2ra.hso.simpic16f84.ui.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Interface model representing a <i>Special Function Register</i> of the Pic16F84. Primarily
 * used for representing such data inside a JavaFX component.
 *
 * @author 0x1C1B
 */

public class SpecialFunctionRegister {

    private StringProperty name;
    private IntegerProperty value;

    public SpecialFunctionRegister() {

        this.name = new SimpleStringProperty();
        this.value = new SimpleIntegerProperty();
    }

    public String getName() {

        return name.get();
    }

    public StringProperty nameProperty() {

        return name;
    }

    public void setName(String name) {

        this.name.set(name);
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
