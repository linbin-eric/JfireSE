package com.jfirer.se2.classinfo;

public interface RefTracking
{
    int addTracking(Object instance);

    Object getInstanceById(int instanceId);
}
