package cn.gigahome.web.message;

import io.netty.buffer.ByteBuf;

public interface MessageDecoder {
    Message decodeMessage(ByteBuf buf, ObjectModel model);
}
