package org.skife.galaxy.agent;

import com.google.common.io.Files;
import com.google.inject.servlet.GuiceFilter;
import com.ning.http.client.AsyncHttpClient;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.server.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skife.galaxy.console.http.GuiceConsoleListener;
import org.skife.galaxy.http.NotFoundServlet;
import org.skife.galaxy.rep.ConsoleDescription;

import java.io.File;
import java.util.EnumSet;

public class AgentConsoleRegistrationTest
{
    private static AsyncHttpClient http   = new AsyncHttpClient();
    private static Server          server = new Server(25365);
    private static File            tmp    = Files.createTempDir();
    private final Agent agent = new Agent(tmp);


    @BeforeClass
    public static void setUp() throws Exception
    {
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        handler.addEventListener(new GuiceConsoleListener(new File("/tmp"), false, "0.0.0.0", 25365));
        handler.addFilter(GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
        handler.addServlet(NotFoundServlet.class, "/*");
        server.setHandler(handler);
        server.start();
    }

    @AfterClass
    public static void tearDown() throws Exception
    {
        http.close();
        server.stop();
        FileUtils.deleteDirectory(tmp);
    }

    @Test
    public void testFoo() throws Exception
    {
//        ConsoleDescription cd = new ConsoleDescription() ;
    }
}
