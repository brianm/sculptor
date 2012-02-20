package org.skife.galaxy.http;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jetty.server.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.LogManager;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.skife.galaxy.TestingHelpers.*;

public class TestApi
{
//    static {
//        java.util.logging.Logger javaRootLogger = LogManager.getLogManager().getLogger("");
//        for (Handler handler : javaRootLogger.getHandlers()) {
//            javaRootLogger.removeHandler(handler);
//        }
//    }

    private static AsyncHttpClient http   = new AsyncHttpClient();
    private static Server          server = new Server(25365);
    private static File            tmp    = Files.createTempDir();

    @BeforeClass
    public static void setUp() throws Exception
    {
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        handler.addEventListener(new GuiceAgentServletModule(tmp, true));
        handler.addFilter(com.google.inject.servlet.GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
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
    public void testDeploy() throws Exception
    {
        // Find deployment url/action
        _Root root = http.prepareGet("http://localhost:25365/")
                         .setHeader("accept", MediaType.APPLICATION_JSON)
                         .execute(new JsonMappingAsyncHandler<_Root>(_Root.class)).get();

        _Action deploy = Iterables.find(root._actions, fieldEquals("rel", "deploy"));
        assertThat(deploy.method, equalTo("POST"));
        assertThat(deploy.params.keySet(), equalTo((Set<String>) ImmutableSet.of("name", "url", "configuration")));

        // perform a deployment against it
        Response r = http.preparePost(deploy.uri)
                         .setHeader("content-type", MediaType.APPLICATION_JSON)
                         .setBody(mapper.writeValueAsString(new _Deployment()))
                         .execute()
                         .get();

        assertThat(r.getStatusCode(), isHttpSuccess());
        _DeployedSlot c = mapper.readValue(r.getResponseBody(), _DeployedSlot.class);
        assertThat(c.slot.deployDir, isExistingFile());
        assertThat(file(c.slot.deployDir, "env", "config.properties"), isExistingFile());
        assertThat(c.slot.stopped, equalTo(true));
    }

    @Test
    public void testStartDeployedThing() throws Exception
    {
        // find the deployment url
        _Root root = http.prepareGet("http://localhost:25365/")
                         .setHeader("accept", MediaType.APPLICATION_JSON)
                         .execute(new JsonMappingAsyncHandler<_Root>(_Root.class)).get();
        _Action deploy = Iterables.find(root._actions, fieldEquals("rel", "deploy"));

        // perform a deployment against it
        _DeployedSlot c = http.preparePost(deploy.uri)
                              .setHeader("content-type", MediaType.APPLICATION_JSON)
                              .setBody(mapper.writeValueAsString(new _Deployment()))
                              .execute(new JsonMappingAsyncHandler<_DeployedSlot>(_DeployedSlot.class))
                              .get();

        // start the deployed thing
        _Action start = Iterables.find(c._actions, fieldEquals("rel", "start"));
        assertThat(start.method, equalTo("POST"));
        assertThat(start.params, equalTo(Collections.<String, String>emptyMap()));

        Response start_response = http.preparePost(start.uri)
                                      .execute()
                                      .get();

        // http client does not follow redirects correctly, so we
        // need to hardcode following the see other :-(
        assertThat(start_response.getStatusCode(), isHttpRedirect());
        String slot_uri = start_response.getHeader("location");

        // check to make sure the deployed thing is now running
        _DeployedSlot started = http.prepareGet(slot_uri)
                                    .setHeader("accept", MediaType.APPLICATION_JSON)
                                    .execute(new JsonMappingAsyncHandler<_DeployedSlot>(_DeployedSlot.class))
                                    .get();
        assertThat(started.slot.running, equalTo(true));
    }

    //
    @Test
    @Ignore
    public void testStartApache() throws Exception
    {
        // find the deployment url
        _Root root = http.prepareGet("http://localhost:25365/")
                         .setHeader("accept", MediaType.APPLICATION_JSON)
                         .execute(new JsonMappingAsyncHandler<_Root>(_Root.class)).get();
        _Action deploy = Iterables.find(root._actions, fieldEquals("rel", "deploy"));

        // perform a deployment against it
        _Deployment d = new _Deployment();
        d.url = URI.create("file:///Users/brianm/src/galaxified-apache/target/galaxified-apache-0.0.1-SNAPSHOT-x86_64-darwin11.2.0.tar.gz");
        _DeployedSlot c = http.preparePost(deploy.uri)
                              .setHeader("content-type", MediaType.APPLICATION_JSON)
                              .setBody(mapper.writeValueAsString(d))
                              .execute(new JsonMappingAsyncHandler<_DeployedSlot>(_DeployedSlot.class))
                              .get();

        // start the deployed thing
        _Action start = Iterables.find(c._actions, fieldEquals("rel", "start"));
        assertThat(start.method, equalTo("POST"));
        assertThat(start.params, equalTo(Collections.<String, String>emptyMap()));

        Response start_response = http.preparePost(start.uri)
                                      .execute()
                                      .get();

        // http client does not follow redirects correctly, so we
        // need to hardcode following the see other :-(
        assertThat(start_response.getStatusCode(), isHttpRedirect());
        String slot_uri = start_response.getHeader("location");

        // check to make sure the deployed thing is now running
        _DeployedSlot started = http.prepareGet(slot_uri)
                                    .setHeader("accept", MediaType.APPLICATION_JSON)
                                    .execute(new JsonMappingAsyncHandler<_DeployedSlot>(_DeployedSlot.class))
                                    .get();

        System.out.println(started.slot.deployDir.getAbsolutePath());

//        Thread.currentThread().join();

        assertThat(started.slot.running, equalTo(true));

        _Action stop = Iterables.find(c._actions, fieldEquals("rel", "stop"));
        http.preparePost(stop.uri)
            .execute()
            .get();
    }

    @Test
    public void testDeployBundleThatDoesNotExist() throws Exception
    {
        // Find deployment url/action
        _Root root = http.prepareGet("http://localhost:25365/")
                         .setHeader("accept", MediaType.APPLICATION_JSON)
                         .execute(new JsonMappingAsyncHandler<_Root>(_Root.class)).get();

        _Action deploy = Iterables.find(root._actions, fieldEquals("rel", "deploy"));
        assertThat(deploy.method, equalTo("POST"));
        assertThat(deploy.params.keySet(), equalTo((Set<String>) ImmutableSet.of("name", "url", "configuration")));


        _Deployment d = new _Deployment();
        d.url = URI.create("file:///tmp/does_not_exist.tar.gz");
        // perform a deployment against it
        Response r = http.preparePost(deploy.uri)
                         .setHeader("content-type", MediaType.APPLICATION_JSON)
                         .setBody(mapper.writeValueAsString(d))
                         .execute()
                         .get();

        assertThat(r.getStatusCode(), isHttpBadRequest());
    }

    @Test
    public void testDeployBundleThatDoesNotExistOverHttp() throws Exception
    {
        // Find deployment url/action
        _Root root = http.prepareGet("http://localhost:25365/")
                         .setHeader("accept", MediaType.APPLICATION_JSON)
                         .execute(new JsonMappingAsyncHandler<_Root>(_Root.class)).get();

        _Action deploy = Iterables.find(root._actions, fieldEquals("rel", "deploy"));
        assertThat(deploy.method, equalTo("POST"));
        assertThat(deploy.params.keySet(), equalTo((Set<String>) ImmutableSet.of("name", "url", "configuration")));

        _Deployment d = new _Deployment();
        d.url = URI.create("http://localhost:25365/kjhasdjkhasdjkhasdhasdjkh");
        // perform a deployment against it
        Response r = http.preparePost(deploy.uri)
                         .setHeader("content-type", MediaType.APPLICATION_JSON)
                         .setBody(mapper.writeValueAsString(d))
                         .execute()
                         .get();

        assertThat(r.getStatusCode(), isHttpBadRequest());
    }

    @Test
    public void testDeployedThingListedAtRoot() throws Exception
    {
        _Action deploy = Iterables.find(http.prepareGet("http://localhost:25365/")
                                            .setHeader("accept", MediaType.APPLICATION_JSON)
                                            .execute(new JsonMappingAsyncHandler<_Root>(_Root.class)).get()._actions,
                                        fieldEquals("rel", "deploy"));

        _DeployedSlot from_deploy = http.preparePost(deploy.uri)
                                        .setHeader("content-type", MediaType.APPLICATION_JSON)
                                        .setBody(mapper.writeValueAsString(new _Deployment()))
                                        .execute(new JsonMappingAsyncHandler<_DeployedSlot>(_DeployedSlot.class))
                                        .get();

        URI self_url = Iterables.find(from_deploy._links, fieldEquals("rel", "self")).uri;

        _Root root = http.prepareGet("http://localhost:25365/")
                         .setHeader("accept", MediaType.APPLICATION_JSON)
                         .execute(new JsonMappingAsyncHandler<_Root>(_Root.class)).get();

        boolean found_self = false;
        for (_DeployedSlot slot : root.slots) {
            for (_Link link : slot._links) {
                found_self = found_self || link.uri.equals(self_url);
            }
        }

        assertThat(found_self, equalTo(true));
    }

    @Test
    @Ignore
    public void testRun() throws Exception
    {
        _Action deploy = Iterables.find(http.prepareGet("http://localhost:25365/")
                                            .setHeader("accept", MediaType.APPLICATION_JSON)
                                            .execute(new JsonMappingAsyncHandler<_Root>(_Root.class)).get()._actions,
                                        fieldEquals("rel", "deploy"));
        http.preparePost(deploy.uri)
            .setHeader("content-type", MediaType.APPLICATION_JSON)
            .setBody(mapper.writeValueAsString(new _Deployment()))
            .execute()
            .get();

        server.join();
    }

    public static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static class _Root
    {
        public List<_Action>       _actions;
        public List<_DeployedSlot> slots;
    }

    public static class _Deployment
    {
        public URI              url           = new File("src/test/resources/echo.tar.gz").toURI();
        public String           name          = "test deployment";
        public Map<String, URI> configuration = ImmutableMap.of("/env/config.properties",
                                                                new File("src/test/resources/some_config.properties")
                                                                    .toURI());
    }

    public static class _DeployedSlot
    {
        public List<_Link>   _links;
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

        public String toString()
        {
            return Objects.toStringHelper(this).add("rel", rel)
                          .add("method", method)
                          .add("uri", uri)
                          .add("params", params)
                          .toString();
        }
    }

    public static class JsonMappingAsyncHandler<T> extends AsyncCompletionHandler<T>
    {

        private final Class<T> type;

        public JsonMappingAsyncHandler(Class<T> type)
        {
            this.type = type;
        }

        @Override
        public T onCompleted(Response response) throws Exception
        {
            try {
                return mapper.readValue(response.getResponseBody(), type);
            }
            catch (Exception e) {
                System.err.println(response.getResponseBody());
                fail(e.getMessage());
                throw e;
            }
        }
    }
}
