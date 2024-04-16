package com.jfirer.se.serializer;

import com.jfirer.se.ByteArray;

public interface Serializer
{
    void writeBytes(ByteArray byteArray, Object instance);

    Object readBytes(ByteArray byteArray);
}
