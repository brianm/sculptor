package org.skife.galaxy.http;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.sampullara.mustache.Mustache;
import com.sampullara.mustache.MustacheBuilder;
import com.sampullara.mustache.MustacheContext;
import com.sampullara.mustache.MustacheException;
import com.sampullara.mustache.Scope;
import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.spi.template.ViewProcessor;

import javax.inject.Inject;
import javax.ws.rs.ext.Provider;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.Map;

@Provider
public class MustacheTemplateProcessor implements ViewProcessor<Mustache>
{
    private final LoadingCache<String, Mustache> templates;
    private final ImmutableMap<String, String> prefixes = ImmutableMap.of("layout", "org/skife/galaxy/http/");

    public MustacheTemplateProcessor()
    {

        templates = CacheBuilder.newBuilder()
                                .build(new MustacheCacheLoader(prefixes));
    }

    @Override
    public Mustache resolve(String name)
    {
        //        return templates.getUnchecked(name);
        try {
            return new MustacheCacheLoader(prefixes).load(name);
        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }

    }

    @Override
    public void writeTo(Mustache mustache, Viewable viewable, OutputStream out) throws IOException
    {
        try {
            StringWriter body = new StringWriter();
            mustache.execute(body, new Scope(viewable.getModel()));

            out.write(body.toString().getBytes(Charsets.UTF_8));
        }
        catch (MustacheException e) {
            throw new IllegalStateException(e);
        }
    }

    private static class MustacheCacheLoader extends CacheLoader<String, Mustache>
    {
        private final Map<String, String> prefixes;

        public MustacheCacheLoader(Map<String, String> prefixes)
        {
            this.prefixes = prefixes;
        }

        @Override
        public Mustache load(String key) throws Exception
        {
            return new MustacheBuilder(new MyContext(prefixes)).parseFile(key);
        }
    }

    private static class MyContext implements MustacheContext
    {
        private final Map<String, String> prefixes;

        public MyContext(Map<String, String> prefixes)
        {
            this.prefixes = prefixes;
        }

        @Override
        public BufferedReader getReader(String name) throws MustacheException
        {
            final String path_on_classpath;
            if (name.contains(":")) {
                String[] parts = name.split(":");
                path_on_classpath = prefixes.get(parts[0]) + parts[1];
            }
            else if (name.startsWith("/")) {
                path_on_classpath = name.substring(1);
            }
            else {
                path_on_classpath = name;
            }
            return new BufferedReader(new InputStreamReader(getClass().getClassLoader()
                                                                .getResourceAsStream(path_on_classpath)));
        }
    }
}
