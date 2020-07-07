package cn.gigahome.web.message;

import java.util.List;

public class Message {
    private short version;

    private long timestamp;

    private short methodIdentifier;

    private List<MessageArgument> args;

    public short getVersion() {
        return version;
    }

    public void setVersion(short version) {
        this.version = version;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public short getMethodIdentifier() {
        return methodIdentifier;
    }

    public void setMethodIdentifier(short methodIdentifier) {
        this.methodIdentifier = methodIdentifier;
    }

    public List<MessageArgument> getArgs() {
        return args;
    }

    public void setArgs(List<MessageArgument> args) {
        this.args = args;
    }
}
