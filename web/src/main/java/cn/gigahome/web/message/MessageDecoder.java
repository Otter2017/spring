package cn.gigahome.web.message;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MessageDecoder {
    public static Message decodeMessage(ByteBuf buf, ObjectModel model) {
        if (buf == null || buf.readableBytes() < 4) {
            throw new IllegalArgumentException("消息格式不符合规范");
        }
        short version = buf.readUnsignedByte();
        long timestamp = buf.readLong();
        short methodIdentifier = buf.readUnsignedByte();
        ObjectMethod method = model.getMethod(methodIdentifier);
        if (method == null) {
            throw new IllegalArgumentException(ErrorType.METHOD_NOT_FOUND);
        }
        Message message = new Message();
        message.setVersion(version);
        message.setTimestamp(timestamp);
        message.setMethodIdentifier(methodIdentifier);
        List<MessageArgument> messageArguments = new ArrayList<>();
        while (buf.readableBytes() > 0) {
            short argumentIdentifier = buf.readUnsignedByte();
            Parameter argument = method.getInputArgument(argumentIdentifier);
            if (argument == null) {
                throw new IllegalArgumentException(ErrorType.ARGUMENT_NOT_FOUND + argumentIdentifier);
            }
            DataType dataType = argument.getDataType();
            LengthType lengthType = argument.getLengthType();
            Object argumentValue = decodeArgumentValue(buf, dataType, lengthType);
            MessageArgument messageArgument = new MessageArgument();
            messageArgument.setIdentifier(argumentIdentifier);
            messageArgument.setValue(argumentValue);
            messageArguments.add(messageArgument);
        }
        message.setArgs(messageArguments);
        return message;
    }

    private static Object decodeArgumentValue(ByteBuf buf, DataType dataType, LengthType lengthType) {
        switch (dataType) {
            case UNSIGNED_BYTE:
            case ENUM: {
                return buf.readUnsignedByte();
            }
            case BYTE: {
                return buf.readByte();
            }
            case BOOLEAN: {
                return buf.readBoolean();
            }
            case DOUBLE: {
                return buf.readDouble();
            }
            case FLOAT: {
                return buf.readFloat();
            }
            case SHORT: {
                return buf.readShort();
            }
            case LONG: {
                return buf.readLong();
            }
            case INTEGER: {
                return buf.readInt();
            }
            case STRING: {
                return decodeString(buf, lengthType);
            }
            default: {
                throw new IllegalArgumentException(ErrorType.UNSUPPORTED_DATA_TYPE + dataType);
            }
        }
    }

    private static String decodeString(ByteBuf buf, LengthType lengthType) {
        int contentLength;
        switch (lengthType) {
            case UNSIGNED_BYTE: {
                contentLength = buf.readUnsignedByte();
                break;
            }
            case DOUBLE_BYTE: {
                contentLength = buf.readUnsignedShort();
                break;
            }
            case VARIANT_BYTE: {
                contentLength = readVariantLength(buf);
                break;
            }
            default: {
                throw new IllegalArgumentException(ErrorType.UNSUPPORTED_LENGTH_TYPE + lengthType);
            }
        }
        byte[] contentBytes = new byte[contentLength];
        buf.readBytes(contentBytes);
        return new String(contentBytes, StandardCharsets.UTF_8);
    }

    private static int readVariantLength(ByteBuf buf) {
        int remainingLength = 0;
        int multiplier = 1;
        short digit;
        int loops = 0;
        do {
            digit = buf.readUnsignedByte();
            remainingLength += (digit & 127) * multiplier;
            multiplier *= 128;
            loops++;
        } while ((digit & 128) != 0 && loops < 4);
        return remainingLength;
    }
}
