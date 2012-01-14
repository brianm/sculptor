package org.skife.galaxy.http;

import com.sampullara.mustache.Mustache;
import com.sampullara.mustache.MustacheBuilder;
import com.sampullara.mustache.MustacheContext;
import com.sampullara.mustache.MustacheException;
import com.sampullara.mustache.ObjectHandler6;
import com.sampullara.mustache.Scope;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class MustacheTest
{
    @Test
    public void testFoo() throws Exception
    {
        Mustache m = new MustacheBuilder().parse("hello, {{ name }}", "test.ms");
        StringWriter buf = new StringWriter();
        m.execute(buf, new Scope(new Object()
        {
            public String getName() { return "Brian";}
        }));

        assertThat(buf.getBuffer().toString(), equalTo("hello, Brian"));
    }

    @Test
    public void testPartials() throws Exception
    {
        MustacheBuilder builder = new MustacheBuilder(new ClasspathRelativeMustacheContext(MustacheTest.class));

        Mustache m = builder.parseFile("testPartials.html");
        StringWriter out = new StringWriter();
        m.execute(out, new Scope(new Object()
        {
            String hello = "bonjour";
            String world = "monde";
        }));
        assertThat(out.toString(), equalTo("!!! bonjour monde !!!"));
    }

    private static class  ClasspathRelativeMustacheContext implements MustacheContext
    {
        private final String base;

        public ClasspathRelativeMustacheContext(Class<?> base) {

            this.base = base.getName().replaceAll("\\.", "/") + "/";
        }

        @Override
        public BufferedReader getReader(String name) throws MustacheException
        {
            StringBuilder b = new StringBuilder(base).append(name);
            InputStream in = MustacheTest.class.getClassLoader().getResourceAsStream(b.toString());
            return new BufferedReader(new InputStreamReader(in));
        }
    }
}
