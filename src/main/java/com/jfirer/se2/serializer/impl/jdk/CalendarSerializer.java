package com.jfirer.se2.serializer.impl.jdk;

import com.jfirer.se2.ByteArray;
import com.jfirer.se2.classinfo.RefTracking;
import com.jfirer.se2.serializer.Serializer;

import java.util.Calendar;

public class CalendarSerializer implements Serializer
{
    @Override
    public void writeBytes(ByteArray byteArray, Object instance)
    {
        byteArray.writeVarLong(((Calendar) instance).getTimeInMillis());
    }

    @Override
    public Object read(ByteArray byteArray, RefTracking refTracking)
    {
        long     l        = byteArray.readVarLong();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(l);
        return calendar;
    }
}
