package com.jfirer.se2.serializer.impl;

import com.jfirer.se2.ByteArray;
import com.jfirer.se2.classinfo.RefTracking;
import com.jfirer.se2.serializer.Serializer;

public abstract class BoxedTypeSerializer implements Serializer
{
    public static class StringSerializer extends BoxedTypeSerializer
    {
        @Override
        public void writeBytes(ByteArray byteArray, Object instance)
        {
            byteArray.writeString((String) instance);
        }

        @Override
        public Object read(ByteArray byteArray, RefTracking refTracking)
        {
            String s = byteArray.readString();
            if (refTracking != null)
            {
                refTracking.addTracking(s);
            }
            return s;
        }
    }
}
