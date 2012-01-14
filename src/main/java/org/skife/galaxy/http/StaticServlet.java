package org.skife.galaxy.http;

import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.ByteStreams;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StaticServlet extends HttpServlet
{
    private final LoadingCache<String, byte[]> cache = CacheBuilder.newBuilder()
                                                                   .build(new CacheLoader<String, byte[]>()
                                                                   {
                                                                       @Override
                                                                       public byte[] load(String key) throws Exception
                                                                       {
                                                                           String to_get = "static" + key;
                                                                           InputStream in = StaticServlet.class.getClassLoader().getResourceAsStream(to_get);
                                                                           ByteArrayOutputStream out = new ByteArrayOutputStream();
                                                                           ByteStreams.copy(in, out);
                                                                           return out.toByteArray();
                                                                       }
                                                                   });

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        byte[] bytes = cache.getUnchecked(req.getPathInfo());
        resp.getOutputStream().write(bytes);
    }
}
