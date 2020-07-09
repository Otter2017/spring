package cn.gigahome.web.message;

import java.util.List;

public class ObjectMethod {
    private int methodIdentifier;

    private String name;

    private String description;

    private MethodCallType callType;

    private List<Parameter> inputArguments;

    private List<Parameter> outputArguments;

    public int getMethodIdentifier() {
        return methodIdentifier;
    }

    public void setMethodIdentifier(int methodIdentifier) {
        this.methodIdentifier = methodIdentifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MethodCallType getCallType() {
        return callType;
    }

    public void setCallType(MethodCallType callType) {
        this.callType = callType;
    }

    public List<Parameter> getInputArguments() {
        return inputArguments;
    }

    public void setInputArguments(List<Parameter> inputArguments) {
        this.inputArguments = inputArguments;
    }

    public List<Parameter> getOutputArguments() {
        return outputArguments;
    }

    public void setOutputArguments(List<Parameter> outputArguments) {
        this.outputArguments = outputArguments;
    }

    public Parameter getInputArgument(short argIdentifier) {
        if (inputArguments != null) {
            for (Parameter parameter : inputArguments) {
                if (argIdentifier == parameter.getIdentifier()) {
                    return parameter;
                }
            }
        }
        return null;
    }
}
