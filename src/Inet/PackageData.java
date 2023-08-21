package Inet;

public class PackageData {
    public static final int REGISTER = 0, BET = 1, HITORSTAND = 2, FINISH = 3;
    private int type;
    private String data;

    public PackageData(int type, String data) {
        this.type = type;
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public int getType() {
        return type;
    }
}
