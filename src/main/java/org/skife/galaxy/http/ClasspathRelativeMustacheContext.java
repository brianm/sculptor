package org.skife.galaxy.http;

import com.sampullara.mustache.MustacheContext;
import com.sampullara.mustache.MustacheException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ClasspathRelativeMustacheContext implements MustacheContext
{
    private final String base;

    public ClasspathRelativeMustacheContext(Class<?> base) {

        this.base = base.getName().replaceAll("\\.", "/") + "/";
    }

    @Override
    public BufferedReader getReader(String name) throws MustacheException
    {
        StringBuilder b = new StringBuilder(base).append(name);
        InputStream in = ClasspathRelativeMustacheContext.class.getClassLoader().getResourceAsStream(b.toString());
        return new BufferedReader(new InputStreamReader(in));
    }

}
