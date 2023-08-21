package Inet;

import Cards.Card;

import java.io.Serializable;

public class PackageData implements Serializable {
    private PackageDataType type;
    private String message;
    private int money;

    public PackageData(PackageDataType type, int money) {
        this();
        this.type = type;
        this.money = money;
    }

    public PackageData(PackageDataType type, String message) {
        this();
        this.type = type;
        this.message = message;
    }

    public PackageData(PackageDataType type) {
        this();
        this.type = type;
    }

    public PackageData() {
        this.type = PackageDataType.Error;
        this.message = null;
        this.money = -1;
    }

    public PackageDataType getType() { return type; }

    public String getMessage() { return message; }

    public int getMoney() { return money; }
}
