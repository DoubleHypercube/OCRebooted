package li.cil.oc.common;

public final class Slot {
    public static final String None = li.cil.oc.api.driver.item.Slot.None;
    public static final String Any = li.cil.oc.api.driver.item.Slot.Any;
    public static final String Filtered = "filtered";
    public static final String Card = li.cil.oc.api.driver.item.Slot.Card;
    public static final String ComponentBus = li.cil.oc.api.driver.item.Slot.ComponentBus;
    public static final String Container = li.cil.oc.api.driver.item.Slot.Container;
    public static final String CPU = li.cil.oc.api.driver.item.Slot.CPU;
    public static final String EEPROM = "eeprom";
    public static final String Floppy = li.cil.oc.api.driver.item.Slot.Floppy;
    public static final String HDD = li.cil.oc.api.driver.item.Slot.HDD;
    public static final String Memory = li.cil.oc.api.driver.item.Slot.Memory;
    public static final String RackMountable = li.cil.oc.api.driver.item.Slot.RackMountable;
    public static final String Tablet = li.cil.oc.api.driver.item.Slot.Tablet;
    public static final String Tool = "tool";
    public static final String Upgrade = li.cil.oc.api.driver.item.Slot.Upgrade;

    public static final String[] All = {
        Card, ComponentBus, Container, CPU, EEPROM, Floppy, HDD, Memory, RackMountable, Tablet, Tool, Upgrade
    };

    // Keep the Scala object's Java-callable accessors for addon compatibility.
    public static String None() { return None; }
    public static String Any() { return Any; }
    public static String Filtered() { return Filtered; }
    public static String Card() { return Card; }
    public static String ComponentBus() { return ComponentBus; }
    public static String Container() { return Container; }
    public static String CPU() { return CPU; }
    public static String EEPROM() { return EEPROM; }
    public static String Floppy() { return Floppy; }
    public static String HDD() { return HDD; }
    public static String Memory() { return Memory; }
    public static String RackMountable() { return RackMountable; }
    public static String Tablet() { return Tablet; }
    public static String Tool() { return Tool; }
    public static String Upgrade() { return Upgrade; }
    public static String[] All() { return All; }

    private Slot() {
    }
}
