package com.jfirer.se2;

public interface JfireSE
{
    byte NULL                     = 0;
    byte NOT_NULL                 = 1;
    byte NAME_ID_CONTENT_TRACK    = 2;
    byte NAME_ID_CONTENT_UN_TRACK = 3;
    byte ID_INSTANCE_ID           = 4;
    byte id_content_track         = 5;
    byte id_content_un_track      = 6;
    byte instance_id              = 7;
    byte content_track            = 8;
    byte content_un_track         = 9;

    static JfireSEConfig supportRefTracking(boolean support)
    {
        return new JfireSEConfig().setRefTracking(support);
    }

    static JfireSEConfig useCompile()
    {
        return new JfireSEConfig().useCompile();
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
