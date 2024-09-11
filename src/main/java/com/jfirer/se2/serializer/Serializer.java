package com.jfirer.se2.serializer;

import com.jfirer.se2.ByteArray;
import com.jfirer.se2.classinfo.RefTracking;

public interface Serializer
{
    /**
     * 输出实例的内容本身，不包含标志位
     *
     * @param byteArray
     * @param instance
     */
    void writeBytes(ByteArray byteArray, Object instance);

    /**
     * 逆序列化并且输出对象实例本身。
     * 如果有传入 refTracking 对象，则创建对象实例后，需要首先通过 addTracking 方法将对象实例添加到追踪列表中
     *
     * @param byteArray
     * @param refTracking
     * @return
     */
    Object read(ByteArray byteArray, RefTracking refTracking);
}
