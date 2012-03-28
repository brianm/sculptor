package org.skife.galaxy;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormattingTest
{
    @Test
    public void testFoo() throws Exception
    {
        String f = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        System.out.println(f);
    }
}
