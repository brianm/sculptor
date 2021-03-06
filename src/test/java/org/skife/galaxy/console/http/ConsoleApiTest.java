package org.skife.galaxy.console.http;

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
import org.skife.galaxy.http.JsonMappingAsyncHandler;
import org.skife.galaxy.http.NotFoundServlet;
import org.skife.galaxy.rep.ConsoleDescription;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.EnumSet;

import static com.google.common.collect.Iterables.find;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.skife.galaxy.base.MorePredicates.beanPropertyEquals;

public class ConsoleApiTest
{
    private static AsyncHttpClient http   = new AsyncHttpClient();
    private static Server          server = new Server(InetSocketAddress.createUnresolved("0.0.0.0", 25365));
    private static File            tmp    = Files.createTempDir();

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
    public void testHasProperSelfLinkAndRegisterAgentAction() throws Exception
    {
        ConsoleDescription console = http.prepareGet("http://localhost:25365/")
                                         .setHeader("accept", MediaType.APPLICATION_JSON)
                                         .execute(new JsonMappingAsyncHandler<ConsoleDescription>(ConsoleDescription.class))
                                         .get();
        assertThat(find(console.getLinks(), beanPropertyEquals("rel", "self")).getUri(),
                   equalTo(URI.create("http://localhost:25365/")));

        assertThat(find(console.getActions(), beanPropertyEquals("rel", "register-agent")), notNullValue());

    }
}
