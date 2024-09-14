package org.example.festest;

import com.jfirer.se2.JfireSE;
import org.junit.Assert;
import org.junit.Test;

public class BaseTest
{
    @Test
    public void test()
    {
        JfireSE   jfireSE   = JfireSE.config().refTracking().build();
        User      user      = new User();
        user.setAge(123);
        user.setName("aaa");
        Home home = new Home();
        home.setAddress("ssss");
        home.setUser(user);
        user.setHome(home);
        byte[] serialize = jfireSE.serialize(user);
        User another = (User) jfireSE.deSerialize(serialize);
        Assert.assertEquals(user.getAge(), another.getAge());
        Assert.assertEquals(user.getName(), another.getName());
        Home home1 = user.getHome();
        Assert.assertEquals(home.getAddress(), home1.getAddress());
    }
}
