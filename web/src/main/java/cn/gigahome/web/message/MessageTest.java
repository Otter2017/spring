package cn.gigahome.web.message;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.tomcat.util.buf.HexUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MessageTest {
    public static void main(String[] args) {
        ObjectModel model = createModel();
        Message message = createMessage();
        ByteBuf buf = encodeMessage(message, model);
        System.out.println(HexUtils.toHexString(buf.array()));
    }

    private static ByteBuf encodeMessage(Message message, ObjectModel model) {
        ByteBuf byteBuf = Unpooled.buffer(3);
        byteBuf.writeByte(message.getVersion());
        byteBuf.writeLong(message.getTimestamp());
        short methodIdentifier = message.getMethodIdentifier();
        ObjectMethod method = getMethod(model, methodIdentifier);
        if (method == null) {
            throw new IllegalArgumentException("未找到指定的方法");
        }
        byteBuf.writeByte(methodIdentifier);
        // fixed header
        for (MessageArgument messageArgument : message.getArgs()) {
            short argIdentifier = messageArgument.getIdentifier();
            byteBuf.writeByte(messageArgument.getIdentifier());
            Parameter argument = getInputArgument(method, argIdentifier);
            if (argument == null) {
                throw new IllegalArgumentException("未找到指定的参数");
            }
            DataType dataType = argument.getDataType();
            Object data = messageArgument.getValue();
            LengthType lengthType = argument.getLengthType();
            encodeData(byteBuf, data, dataType, lengthType);
        }
        return byteBuf;
    }

    private static void encodeData(ByteBuf buf, Object data, DataType dataType, LengthType lengthType) {
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
                throw new IllegalArgumentException("未支持的数据类型：" + dataType);
            }
        }
    }

    private static void encodeString(ByteBuf buf, String content, LengthType lengthType) {
        int contentLength = 0;
        byte[] contentBytes = null;
        if (content != null) {
            contentBytes = content.getBytes(StandardCharsets.UTF_8);
            contentLength = contentBytes.length;
        }
        switch (lengthType) {
            case UNSIGNED_BYTE: {
                if (contentLength > 255) {
                    throw new RuntimeException("字符串长度超过最大限制");
                }
                buf.writeByte((byte) contentLength);
                break;
            }
            case DOUBLE_BYTE: {
                if (contentLength > 65535) {
                    throw new RuntimeException("字符串长度超过最大限制");
                }
                buf.writeShort(contentLength);
                break;
            }
            case VARIANT_BYTE: {
                if (contentLength > 268435455) {
                    throw new RuntimeException("字符串长度超过最大限制");
                }
                writeVariableLengthInt(buf, contentLength);
                break;
            }
            default: {
                throw new IllegalArgumentException("未支持的长度类型：" + lengthType);
            }
        }
        if (contentBytes != null)
            buf.writeBytes(contentBytes);
    }

    private static void writeVariableLengthInt(ByteBuf buf, int num) {
        do {
            int digit = num % 128;
            num /= 128;
            if (num > 0) {
                digit |= 0x80;
            }
            buf.writeByte(digit);
        } while (num > 0);
    }

    private static Parameter getInputArgument(ObjectMethod method, short argIdentifier) {
        if (method != null && method.getInputArguments() != null) {
            for (Parameter parameter : method.getInputArguments()) {
                if (argIdentifier == parameter.getIdentifier()) {
                    return parameter;
                }
            }
        }
        return null;
    }

    private static ObjectMethod getMethod(ObjectModel model, short methodIdentifier) {
        if (model != null) {
            List<ObjectMethod> serviceList = model.getServices();
            if (serviceList != null) {
                for (ObjectMethod method : serviceList) {
                    if (methodIdentifier == method.getMethodIdentifier()) {
                        return method;
                    }
                }
            }

            List<ObjectMethod> eventList = model.getEvents();
            if (eventList != null) {
                for (ObjectMethod method : eventList) {
                    if (methodIdentifier == method.getMethodIdentifier()) {
                        return method;
                    }
                }
            }
        }
        return null;
    }

    private static Message createMessage() {
        Message message = new Message();
        message.setVersion((short) 1);
        message.setMethodIdentifier((short) 6);
        message.setTimestamp(System.currentTimeMillis());

        MessageArgument<String> location = new MessageArgument<>();
        location.setIdentifier((short) 7);
        location.setValue("中心公园");

        MessageArgument<Short> speed = new MessageArgument<>();
        speed.setIdentifier((short) 8);
        speed.setValue((short) 6);

        List<MessageArgument> args = new ArrayList<>();
        args.add(location);
        args.add(speed);

        message.setArgs(args);
        return message;
    }

    private static ObjectModel createModel() {
        ObjectModel model = new ObjectModel();
        model.setVersion(1);
        model.setProductId("x4dla3l3cs8f");

        Parameter height = new Parameter();
        height.setIdentifier(1);
        height.setName("height");
        height.setDescription("身高");
        height.setUnit("cm");
        height.setUnitName("厘米");
        height.setDataType(DataType.FLOAT);

        Parameter weight = new Parameter();
        weight.setIdentifier(2);
        weight.setName("weight");
        weight.setDescription("体重");
        weight.setUnit("kg");
        weight.setUnitName("千克");
        weight.setDataType(DataType.FLOAT);

        Parameter name = new Parameter();
        name.setIdentifier(3);
        name.setName("name");
        name.setDescription("姓名");
        name.setUnit("");
        name.setUnitName("");
        name.setDataType(DataType.STRING);
        name.setLengthType(LengthType.UNSIGNED_BYTE);

        Parameter age = new Parameter();
        age.setIdentifier(4);
        age.setName("age");
        age.setDescription("年龄");
        age.setUnit("year");
        age.setUnitName("岁");
        age.setDataType(DataType.SHORT);

        Parameter idNumber = new Parameter();
        idNumber.setIdentifier(5);
        idNumber.setName("idNumber");
        idNumber.setDescription("身份证号码");
        idNumber.setUnit("");
        idNumber.setUnitName("");
        idNumber.setDataType(DataType.STRING);
        idNumber.setLengthType(LengthType.UNSIGNED_BYTE);

        List<Parameter> properties = new ArrayList<>();
        properties.add(idNumber);
        properties.add(name);
        properties.add(age);
        properties.add(weight);
        properties.add(height);

        model.setProperties(properties);

        ObjectMethod run = new ObjectMethod();
        run.setMethodIdentifier(6);
        run.setName("run");
        run.setDescription("跑步");
        run.setCallType(MethodCallType.UNSYNC);

        Parameter location = new Parameter();
        location.setIdentifier(7);
        location.setName("location");
        location.setDescription("地点");
        location.setUnit("");
        location.setUnitName("");
        location.setDataType(DataType.STRING);
        location.setLengthType(LengthType.UNSIGNED_BYTE);

        Parameter speed = new Parameter();
        speed.setIdentifier(8);
        speed.setName("speed");
        speed.setDescription("速度");
        speed.setUnit("m/s");
        speed.setUnitName("米/秒");
        speed.setDataType(DataType.SHORT);

        List<Parameter> runArgs = new ArrayList<>();
        runArgs.add(location);
        runArgs.add(speed);
        run.setInputArguments(runArgs);

        List<ObjectMethod> events = new ArrayList<>();
        events.add(run);

        model.setEvents(events);

        System.out.println(JSON.toJSONString(model));
        return model;
    }
}
