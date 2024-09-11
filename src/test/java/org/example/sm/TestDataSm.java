package org.example.sm;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TestDataSm
{
    private int     a  = 10;
    private long    b  = 20;
    private Integer a1 = 3;
    private String  c  = "sassa";
}
