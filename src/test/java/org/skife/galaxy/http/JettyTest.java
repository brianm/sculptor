package org.skife.galaxy.http;

import com.google.common.base.Objects;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jetty.server.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.skife.galaxy.agent.http.GuiceAgentServletModule;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogManager;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.skife.galaxy.TestingHelpers.*;

public class JettyTest
{
    static {
        java.util.logging.Logger javaRootLogger = LogManager.getLogManager().getLogger("");
        for (Handler handler : javaRootLogger.getHandlers()) {
            javaRootLogger.removeHandler(handler);
        }
    }

    private AsyncHttpClient http;
    private Server          server;
    private File            tmp;

    @Before
    public void setUp() throws Exception
    {
        this.http = new AsyncHttpClient();
        tmp = Files.createTempDir();
        server = new Server(25365);

        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        handler.addEventListener(new GuiceAgentServletModule(tmp, true));
        handler.addFilter(com.google.inject.servlet.GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
        handler.addServlet(NotFoundServlet.class, "/*");
        server.setHandler(handler);
        server.start();
    }

    @After
    public void tearDown() throws Exception
    {
        this.http.close();
        server.stop();
        FileUtils.deleteDirectory(tmp);

    }

    @Test
    public void testServerRuns() throws Exception
    {

        String body = http.executeRequest(new RequestBuilder()
                                              .setHeader("Accept", "text/html")
                                              .setUrl("http://localhost:25365/")
                                              .build()).get().getResponseBody();

        assertThat(body, containsString(tmp.getAbsolutePath()));
        assertThat(body, matches("<html>\\s*<head>"));
    }

    @Test
    public void testJsonDeploy() throws Exception
    {
        Response r = http.preparePost("http://localhost:25365/deploy")
                         .setHeader("content-type", MediaType.APPLICATION_JSON)
                         .setBody(mapper.writeValueAsString(new _Deploy()))
                         .execute().get();

        assertThat(r.getStatusCode(), isHttpSuccess());
        _Container c = mapper.readValue(r.getResponseBody(), _Container.class);
        assertThat(c.slot.deployDir, exists());
        assertThat(file(c.slot.deployDir, "env", "config.properties"), exists());
    }

    @Test
    public void testStartApi() throws Exception
    {
        Response r = http.preparePost("http://localhost:25365/deploy")
                         .setHeader("content-type", MediaType.APPLICATION_JSON)
                         .setBody(mapper.writeValueAsString(new _Deploy()))
                         .execute().get();

        assertThat(r.getStatusCode(), isHttpSuccess());
        _Container c = mapper.readValue(r.getResponseBody(), _Container.class);
        _Action start = Iterables.find(c._actions, fieldEquals("rel", "start"));
        assertThat(start.method, equalTo("POST"));

        //throw new UnsupportedOperationException("Not Yet Implemented!");

    }

    @Test
    @Ignore
    public void testRun() throws Exception
    {
        server.join();
    }

    public static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static class _Deploy
    {
        public URI url = new File("src/test/resources/echo.tar.gz").toURI();
        public String name = "test deployment";
        public Map<String, URI> configuration = ImmutableMap.of("/env/config.properties",
                                                                new File("src/test/resources/some_config.properties")
                                                                    .toURI());
    }

    public static class _Container
    {
        public List<_Link>   links;
        public List<_Action> _actions;
        public _Slot         slot;
    }

    public static class _Link
    {
        public String rel;
        public URI    uri;
    }

    public static class _Slot
    {
        public File    root;
        public File    deployDir;
        public boolean running;
        public boolean stopped;
    }

    public static class _Action
    {
        public String              rel;
        public String              method;
        public String              uri;
        public Map<String, String> params;

        public String toString() {
            return Objects.toStringHelper(this).add("rel", rel)
                          .add("method", method)
                          .add("uri", uri)
                          .add("params", params)
                          .toString();
        }
    }
}
