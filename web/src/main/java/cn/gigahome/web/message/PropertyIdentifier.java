package cn.gigahome.web.message;

public enum PropertyIdentifier {
    NULL(null, null),
    VERSION(DataType.UNSIGNED_BYTE, null),
    TIMESTAMP(DataType.LONG, null),
    METHOD_NAME(DataType.STRING, LengthType.UNSIGNED_BYTE),
    METHOD_IDENTIFIER(DataType.UNSIGNED_BYTE, null);

    private DataType dateType;

    private LengthType lengthType;

    PropertyIdentifier(DataType dataType, LengthType lengthType) {
        this.dateType = dataType;
        this.lengthType = lengthType;
    }

    public DataType getDateType() {
        return dateType;
    }

    public void setDateType(DataType dateType) {
        this.dateType = dateType;
    }

    public LengthType getLengthType() {
        return lengthType;
    }

    public void setLengthType(LengthType lengthType) {
        this.lengthType = lengthType;
    }
}
