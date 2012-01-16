package org.skife.galaxy.http;

import com.google.common.io.Files;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.RequestBuilder;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.server.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.skife.galaxy.cli.GlobalOptions;

import java.io.File;
import java.util.EnumSet;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.skife.galaxy.TestingHelpers.matches;

public class JettyTest
{
    private AsyncHttpClient http;

    @Before
    public void setUp() throws Exception
    {
        this.http = new AsyncHttpClient();
    }

    @After
    public void tearDown() throws Exception
    {
        this.http.close();

    }

    @Test
    public void testFoo() throws Exception
    {
        File tmp = Files.createTempDir();
        Server server = new Server(25365);

        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        handler.addEventListener(new GuiceAgentServletModule(tmp, new GlobalOptions()));
        handler.addFilter(com.google.inject.servlet.GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
        handler.addServlet(NotFoundServlet.class, "/*");
        server.setHandler(handler);

        server.start();

        String body = http.executeRequest(new RequestBuilder()
                                              .setUrl("http://localhost:25365/")
                                              .build()).get().getResponseBody();

        assertThat(body, containsString(tmp.getAbsolutePath()));
        assertThat(body, matches("<html>\\s*<head>"));
        server.stop();
        FileUtils.deleteDirectory(tmp);
    }

    @Test
    @Ignore
    public void testRun() throws Exception
    {
        Server server = new Server(25365);

        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        handler.addEventListener(new GuiceAgentServletModule(new File("/tmp/sculptor"),
                                                             new GlobalOptions(GlobalOptions.RunType.DEBUG)));
        handler.addFilter(com.google.inject.servlet.GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
        handler.addServlet(NotFoundServlet.class, "/*");
        server.setHandler(handler);

        server.start();
        server.join();
    }


}
