package cn.gigahome.web.message;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import org.apache.tomcat.util.buf.HexUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MessageTest {
    public static void main(String[] args) {
        ObjectModel model = createModel();
        Message message = createMessage();
        // Encode message
        ByteBuf buf = MessageEncoder.encodeMessage(message, model);
        String messageJSon = JSON.toJSONString(message);
        System.out.println(messageJSon.getBytes(StandardCharsets.UTF_8).length);
        System.out.println(messageJSon);
        if (buf.readableBytes() > 0) {
            String hexString = HexUtils.toHexString(Arrays.copyOfRange(buf.array(), 0, buf.readableBytes()));
            System.out.println(hexString);
            System.out.println(hexString.length() / 2);
        }

        // Decode message
        ByteBuf buffer = buf.copy();
        Message decodedMessage = MessageDecoder.decodeMessage(buffer, model);
        System.out.println(JSON.toJSONString(decodedMessage));
    }


    private static Message createMessage() {
        Message message = new Message();
        message.setVersion((short) 1);
        message.setMethodIdentifier((short) 6);
        message.setTimestamp(System.currentTimeMillis());

        MessageArgument<String> location = new MessageArgument<>();
        location.setIdentifier((short) 7);
        location.setValue("深圳市南山区嘉璐路245号");

        MessageArgument<Byte> speed = new MessageArgument<>();
        speed.setIdentifier((short) 8);
        speed.setValue((byte) 6);

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
        speed.setDataType(DataType.UNSIGNED_BYTE);

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
