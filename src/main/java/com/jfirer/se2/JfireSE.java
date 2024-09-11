package com.jfirer.se2;

public interface JfireSE
{
    byte NULL                     = 0;
    byte NOT_NULL                 = 1;
    byte NAME_ID_CONTENT_TRACK    = 2;
    byte NAME_ID_CONTENT_UN_TRACK = 3;
    byte ID_INSTANCE_ID           = 4;
    byte ID_CONTENT_TRACK         = 5;
    byte ID_CONTENT_UN_TRACK      = 6;
    byte INSTANCE_ID              = 7;
    byte CONTENT_TRACK            = 8;
    byte CONTENT_UN_TRACK         = 9;

    static JfireSEConfig supportRefTracking(boolean support)
    {
        return new JfireSEConfig().setRefTracking(support);
    }

    static JfireSEConfig staticRegisterClass(Class<?> clazz)
    {
        return new JfireSEConfig().staticRegisterClass(clazz);
    }

    static JfireSE build()
    {
        return new JfireSEConfig().build();
    }

    byte[] write(Object instance);

    Object read(byte[] bytes);
}
