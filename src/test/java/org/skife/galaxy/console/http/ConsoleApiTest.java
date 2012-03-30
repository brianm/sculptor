package org.skife.galaxy.console.http;

import com.google.common.io.Files;
import com.google.inject.servlet.GuiceFilter;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.server.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skife.galaxy.http.JsonMappingAsyncHandler;
import org.skife.galaxy.http.NotFoundServlet;
import org.skife.galaxy.rep.Action;
import org.skife.galaxy.rep.AgentDescription;
import org.skife.galaxy.rep.ConsoleDescription;
import org.skife.galaxy.rep.Link;
import org.skife.galaxy.rep.SlotDescription;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static com.google.common.collect.Iterables.find;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.skife.galaxy.TestingHelpers.isHttpSuccess;
import static org.skife.galaxy.base.JsonEntityWriter.jsonWriter;
import static org.skife.galaxy.base.MorePredicates.beanPropertyEquals;

public class ConsoleApiTest
{
    private static AsyncHttpClient http   = new AsyncHttpClient();
    private static Server          server = new Server(25365);
    private static File            tmp    = Files.createTempDir();

    @BeforeClass
    public static void setUp() throws Exception
    {
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        handler.addEventListener(new GuiceConsoleListener(new File("/tmp"), false));
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


    @Test
    public void testRegisterAgent() throws Exception
    {
        ConsoleDescription console = http.prepareGet("http://localhost:25365/")
                                         .setHeader("accept", MediaType.APPLICATION_JSON)
                                         .execute(new JsonMappingAsyncHandler<ConsoleDescription>(ConsoleDescription.class))
                                         .get();

        AgentDescription ad = new AgentDescription(Collections.<Link>emptyList(),
                                                   Collections.<Action>emptyList(),
                                                   Collections.<String, URI>emptyMap(),
                                                   Collections.<SlotDescription>emptyList(),
                                                   new File("/tmp/agent-root"),
                                                   UUID.randomUUID());

        Action register_agent = find(console.getActions(), beanPropertyEquals("rel", "register-agent"));
        assertThat(register_agent.getMethod(), equalTo("POST"));

        Response r = http.preparePost(register_agent.getUri().toString())
                         .setHeader("Content-Type", MediaType.APPLICATION_JSON)
                         .setBody(jsonWriter(ad))
                         .execute()
                         .get();

        assertThat(r.getStatusCode(), isHttpSuccess());
    }

    @Test
    public void testAgentRegisteredOnConsoleShowsUp() throws Exception
    {
        ConsoleDescription console = http.prepareGet("http://localhost:25365/")
                                         .setHeader("accept", MediaType.APPLICATION_JSON)
                                         .execute(new JsonMappingAsyncHandler<ConsoleDescription>(ConsoleDescription.class))
                                         .get();

        AgentDescription ad = new AgentDescription(Collections.<Link>emptyList(),
                                                   Collections.<Action>emptyList(),
                                                   Collections.<String, URI>emptyMap(),
                                                   Collections.<SlotDescription>emptyList(),
                                                   new File("/tmp/agent-root"),
                                                   UUID.randomUUID());

        Action register_agent = find(console.getActions(), beanPropertyEquals("rel", "register-agent"));
        http.preparePost(register_agent.getUri().toString())
            .setHeader("Content-Type", MediaType.APPLICATION_JSON)
            .setBody(jsonWriter(ad))
            .execute()
            .get();

        ConsoleDescription updated_console = http.prepareGet("http://localhost:25365/")
                                                 .setHeader("accept", MediaType.APPLICATION_JSON)
                                                 .execute(new JsonMappingAsyncHandler<ConsoleDescription>(ConsoleDescription.class))
                                                 .get();

        assertThat(find(updated_console.getAgents(), beanPropertyEquals("id", ad.getId())), equalTo(ad));

    }

}
