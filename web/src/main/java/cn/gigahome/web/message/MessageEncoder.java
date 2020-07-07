package cn.gigahome.web.message;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;

public class MessageEncoder {
    public static void main(String[] args) {
        testModel();
    }

    private static void testModel() {
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

        Parameter speed = new Parameter();
        speed.setIdentifier(7);
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
    }
}
