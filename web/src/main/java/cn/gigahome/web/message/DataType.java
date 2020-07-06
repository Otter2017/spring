package cn.gigahome.web.message;

public enum DataType {
    UNSIGNED_BYTE("无符号字节", 1),
    BYTE("字节", 1),
    INTEGER("整型", 4),
    LONG("长整型", 8),
    FLOAT("浮点型", 4),
    DOUBLE("双精度浮点型", 8),
    STRING("字符串", 2);

    private String name;

    private int length;

    DataType(String name, int length) {
        this.name = name;
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
