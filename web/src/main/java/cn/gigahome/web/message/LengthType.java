package cn.gigahome.web.message;

public enum LengthType {
    /**
     * 无符号整型，最大值255
     */
    UNSIGNED_BYTE,
    /**
     * 双字节整型,最大值65535,MSB LSB编码
     */
    DOUBLE_BYTE,
    /**
     * 变长字节整型,前一字节的最高位为1时，下一字节也包含在内，最多4个字节
     */
    VARIANT_BYTE
}
