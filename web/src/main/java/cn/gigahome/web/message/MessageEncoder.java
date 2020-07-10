package cn.gigahome.web.message;

import io.netty.buffer.ByteBuf;

public interface MessageEncoder {
    ByteBuf encodeMessage(Message message, ObjectModel model);
}
