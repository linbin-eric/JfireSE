package com.jfirer.se2.serializer;

import com.jfirer.se2.ByteArray;

public interface Serializer
{
    void writeBytes(ByteArray byteArray, Object instance);

    Object read(ByteArray stream);
}
