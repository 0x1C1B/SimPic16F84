package org.ai2ra.hso.simpic16f84.ui.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Interface model representing the content of the Pic16F84 STATUS register. Primarily
 * used for representing such data inside a JavaFX component.
 *
 * @author 0x1C1B
 */

public class StatusRegister {

    private IntegerProperty irpFlag;
    private IntegerProperty rp1Flag;
    private IntegerProperty rp0Flag;
    private IntegerProperty toFlag;
    private IntegerProperty pdFlag;
    private IntegerProperty zeroFlag;
    private IntegerProperty digitCarryFlag;
    private IntegerProperty carryFlag;

    public StatusRegister() {

        this.irpFlag = new SimpleIntegerProperty();
        this.rp1Flag = new SimpleIntegerProperty();
        this.rp0Flag = new SimpleIntegerProperty();
        this.toFlag = new SimpleIntegerProperty();
        this.pdFlag = new SimpleIntegerProperty();
        this.zeroFlag = new SimpleIntegerProperty();
        this.digitCarryFlag = new SimpleIntegerProperty();
        this.carryFlag = new SimpleIntegerProperty();
    }

    public int getIrpFlag() {

        return irpFlag.get();
    }

    public IntegerProperty irpFlagProperty() {

        return irpFlag;
    }

    public void setIrpFlag(int irpFlag) {

        this.irpFlag.set(irpFlag);
    }

    public int getRp1Flag() {

        return rp1Flag.get();
    }

    public IntegerProperty rp1FlagProperty() {

        return rp1Flag;
    }

    public void setRp1Flag(int rp1Flag) {

        this.rp1Flag.set(rp1Flag);
    }

    public int getRp0Flag() {

        return rp0Flag.get();
    }

    public IntegerProperty rp0FlagProperty() {

        return rp0Flag;
    }

    public void setRp0Flag(int rp0Flag) {

        this.rp0Flag.set(rp0Flag);
    }

    public int getToFlag() {

        return toFlag.get();
    }

    public IntegerProperty toFlagProperty() {

        return toFlag;
    }

    public void setToFlag(int toFlag) {
        this.toFlag.set(toFlag);
    }

    public int getPdFlag() {

        return pdFlag.get();
    }

    public IntegerProperty pdFlagProperty() {

        return pdFlag;
    }

    public void setPdFlag(int pdFlag) {

        this.pdFlag.set(pdFlag);
    }

    public int getZeroFlag() {

        return zeroFlag.get();
    }

    public IntegerProperty zeroFlagProperty() {

        return zeroFlag;
    }

    public void setZeroFlag(int zeroFlag) {

        this.zeroFlag.set(zeroFlag);
    }

    public int getDigitCarryFlag() {

        return digitCarryFlag.get();
    }

    public IntegerProperty digitCarryFlagProperty() {

        return digitCarryFlag;
    }

    public void setDigitCarryFlag(int digitCarryFlag) {

        this.digitCarryFlag.set(digitCarryFlag);
    }

    public int getCarryFlag() {

        return carryFlag.get();
    }

    public IntegerProperty carryFlagProperty() {

        return carryFlag;
    }

    public void setCarryFlag(int carryFlag) {

        this.carryFlag.set(carryFlag);
    }
}
