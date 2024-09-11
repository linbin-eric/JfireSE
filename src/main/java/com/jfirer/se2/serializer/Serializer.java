package com.jfirer.se2.serializer;

import com.jfirer.se2.ByteArray;

public interface Serializer
{
    /**
     * 输出实例的内容本身，不包含标志位
     *
     * @param byteArray
     * @param instance
     */
    void writeBytes(ByteArray byteArray, Object instance);

    void read(ByteArray byteArray, Object instance);
}
