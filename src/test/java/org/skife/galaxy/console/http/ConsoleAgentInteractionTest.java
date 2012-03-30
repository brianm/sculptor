package org.skife.galaxy.console.http;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import com.google.inject.servlet.GuiceFilter;
import com.ning.http.client.AsyncHttpClient;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.server.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.joda.time.Duration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skife.galaxy.agent.http.GuiceAgentListener;
import org.skife.galaxy.http.NotFoundServlet;

import java.io.File;
import java.net.URI;
import java.util.EnumSet;

public class ConsoleAgentInteractionTest
{
    private static AsyncHttpClient http        = new AsyncHttpClient();
    private static Server          console     = new Server(36525);
    private static Server          agent       = new Server(25365);
    private static File            console_tmp = Files.createTempDir();
    private static File            agent_tmp   = Files.createTempDir();

    @BeforeClass
    public static void consoleSetup() throws Exception
    {
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        handler.addEventListener(new GuiceConsoleListener(console_tmp, false));
        handler.addFilter(GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
        handler.addServlet(NotFoundServlet.class, "/*");
        console.setHandler(handler);
        console.start();
    }

    @AfterClass
    public static void consoleTearDown() throws Exception
    {
        console.stop();
        FileUtils.deleteDirectory(console_tmp);
    }

    @BeforeClass
    public static void agentSetUp() throws Exception
    {
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        handler.addEventListener(new GuiceAgentListener(agent_tmp,
                                                        true,
                                                        ImmutableSet.of(URI.create("http://localhost:36525")),
                                                        Duration.millis(100)));
        handler.addFilter(com.google.inject.servlet.GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
        handler.addServlet(NotFoundServlet.class, "/*");
        agent.setHandler(handler);
        agent.start();
    }

    @AfterClass
    public static void agentTearDown() throws Exception
    {
        http.close();
        agent.stop();
        FileUtils.deleteDirectory(agent_tmp);
    }


    @Test
    public void testRegistration() throws Exception
    {

    }
}
