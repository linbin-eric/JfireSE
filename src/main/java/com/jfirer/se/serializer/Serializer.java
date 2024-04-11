package com.jfirer.se.serializer;

import com.jfirer.se.InternalByteArray;

public interface Serializer
{
    void writeBytes(InternalByteArray byteArray, Object instance);

    Object readBytes(InternalByteArray byteArray);
}
