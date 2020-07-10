package cn.gigahome.web.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

public class ByteMessageEncoder implements MessageEncoder {

    @Override
    public ByteBuf encodeMessage(Message message, ObjectModel model) {
        ByteBuf byteBuf = Unpooled.buffer(3);
        byteBuf.writeByte(message.getVersion());
        byteBuf.writeLong(message.getTimestamp());
        short methodIdentifier = message.getMethodIdentifier();
        ObjectMethod method = model.getMethod(methodIdentifier);
        if (method == null) {
            throw new IllegalArgumentException(ErrorType.METHOD_NOT_FOUND);
        }
        byteBuf.writeByte(methodIdentifier);
        // fixed header
        for (MessageArgument messageArgument : message.getArgs()) {
            short argIdentifier = messageArgument.getIdentifier();
            byteBuf.writeByte(messageArgument.getIdentifier());
            Parameter argument = method.getInputArgument(argIdentifier);
            if (argument == null) {
                throw new IllegalArgumentException(ErrorType.ARGUMENT_NOT_FOUND + argIdentifier);
            }
            DataType dataType = argument.getDataType();
            Object data = messageArgument.getValue();
            LengthType lengthType = argument.getLengthType();
            encodeData(byteBuf, data, dataType, lengthType);
        }
        return byteBuf;
    }

    private void encodeData(ByteBuf buf, Object data, DataType dataType, LengthType lengthType) {
        switch (dataType) {
            case UNSIGNED_BYTE:
            case BYTE:
            case BOOLEAN:
            case ENUM: {
                byte value = (byte) data;
                buf.writeByte(value);
                break;
            }
            case SHORT: {
                short value = (short) data;
                buf.writeShort(value);
                break;
            }
            case INTEGER: {
                int value = (int) data;
                buf.writeInt(value);
                break;
            }
            case LONG: {
                long value = (long) data;
                buf.writeLong(value);
                break;
            }
            case FLOAT: {
                float value = (float) data;
                buf.writeFloat(value);
                break;
            }
            case DOUBLE: {
                double value = (double) data;
                buf.writeDouble(value);
                break;
            }
            case STRING: {
                String value = (String) data;
                encodeString(buf, value, lengthType);
                break;
            }
            default: {
                throw new IllegalArgumentException(ErrorType.UNSUPPORTED_DATA_TYPE + dataType);
            }
        }
    }

    private void encodeString(ByteBuf buf, String content, LengthType lengthType) {
        int contentLength = 0;
        byte[] contentBytes = null;
        if (content != null) {
            contentBytes = content.getBytes(StandardCharsets.UTF_8);
            contentLength = contentBytes.length;
        }
        switch (lengthType) {
            case UNSIGNED_BYTE: {
                if (contentLength > 255) {
                    throw new RuntimeException(ErrorType.LENGTH_TOO_BIG + contentLength);
                }
                buf.writeByte(contentLength & 0xff);
                break;
            }
            case DOUBLE_BYTE: {
                if (contentLength > 65535) {
                    throw new RuntimeException(ErrorType.LENGTH_TOO_BIG + contentLength);
                }
                buf.writeShort(contentLength & 0xffff);
                break;
            }
            case VARIANT_BYTE: {
                if (contentLength > 268435455) {
                    throw new RuntimeException(ErrorType.LENGTH_TOO_BIG);
                }
                writeVariableLengthInt(buf, contentLength);
                break;
            }
            default: {
                throw new IllegalArgumentException(ErrorType.UNSUPPORTED_LENGTH_TYPE + lengthType);
            }
        }
        if (contentBytes != null)
            buf.writeBytes(contentBytes);
    }

    private void writeVariableLengthInt(ByteBuf buf, int num) {
        do {
            int digit = num % 128;
            num /= 128;
            if (num > 0) {
                digit |= 0x80;
            }
            buf.writeByte(digit);
        } while (num > 0);
    }
}
