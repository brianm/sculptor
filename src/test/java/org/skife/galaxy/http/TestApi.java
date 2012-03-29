package org.skife.galaxy.http;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jetty.server.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.skife.galaxy.agent.http.GuiceAgentServletModule;
import org.skife.galaxy.rep.Action;
import org.skife.galaxy.rep.AgentDescription;
import org.skife.galaxy.rep.Link;
import org.skife.galaxy.rep.SlotDescription;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.skife.galaxy.TestingHelpers.*;
import static org.skife.galaxy.guava.MorePredicates.beanPropertyEquals;

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
        AgentDescription root = http.prepareGet("http://localhost:25365/")
                                    .setHeader("accept", MediaType.APPLICATION_JSON)
                                    .execute(new JsonMappingAsyncHandler<AgentDescription>(AgentDescription.class))
                                    .get();

        Action deploy = Iterables.find(root.getActions(), beanPropertyEquals("rel", "deploy"));
        assertThat(deploy.getMethod(), equalTo("POST"));
        assertThat(deploy.getParams().keySet(), equalTo((Set<String>) ImmutableSet.of("name", "url", "configuration")));

        // perform a deployment against it
        Response r = http.preparePost(deploy.getUri().toString())
                         .setHeader("content-type", MediaType.APPLICATION_JSON)
                         .setBody(mapper.writeValueAsString(new _Deployment()))
                         .execute()
                         .get();

        assertThat(r.getStatusCode(), isHttpSuccess());
        SlotDescription c = mapper.readValue(r.getResponseBody(), SlotDescription.class);
        assertThat(c.getDeployDir(), isExistingFile());
        assertThat(file(c.getDeployDir(), "env", "config.properties"), isExistingFile());
        assertThat(c.getState(), equalTo("stopped"));
    }

    @Test
    public void testStartDeployedThing() throws Exception
    {
        // Find deployment url/action
        AgentDescription root = http.prepareGet("http://localhost:25365/")
                                    .setHeader("accept", MediaType.APPLICATION_JSON)
                                    .execute(new JsonMappingAsyncHandler<AgentDescription>(AgentDescription.class))
                                    .get();

        Action deploy = Iterables.find(root.getActions(), beanPropertyEquals("rel", "deploy"));
        assertThat(deploy.getMethod(), equalTo("POST"));
        assertThat(deploy.getParams().keySet(), equalTo((Set<String>) ImmutableSet.of("name", "url", "configuration")));

        // perform a deployment against it
        // perform a deployment against it
        SlotDescription c = http.preparePost(deploy.getUri().toString())
                                .setHeader("content-type", MediaType.APPLICATION_JSON)
                                .setBody(mapper.writeValueAsString(new _Deployment()))
                                .execute(new JsonMappingAsyncHandler<SlotDescription>(SlotDescription.class))
                                .get();

        // start the deployed thing
        Action start = Iterables.find(c.getActions(), beanPropertyEquals("rel", "start"));
        assertThat(start.getMethod(), equalTo("POST"));
        assertThat(start.getParams(), equalTo(Collections.<String, String>emptyMap()));

        Response start_response = http.preparePost(start.getUri().toString())
                                      .execute()
                                      .get();

        // http client does not follow redirects correctly, so we
        // need to hardcode following the see other :-(
        assertThat(start_response.getStatusCode(), isHttpRedirect());
        String slot_uri = start_response.getHeader("location");

        // check to make sure the deployed thing is now running
        SlotDescription started = http.prepareGet(slot_uri)
                                      .setHeader("accept", MediaType.APPLICATION_JSON)
                                      .execute(new JsonMappingAsyncHandler<SlotDescription>(SlotDescription.class))
                                      .get();
        assertThat(started.getState(), equalTo("running"));
    }


    @Test
    @Ignore
    public void testStartApache() throws Exception
    {
        // find the deployment url
        AgentDescription root = http.prepareGet("http://localhost:25365/")
                                    .setHeader("accept", MediaType.APPLICATION_JSON)
                                    .execute(new JsonMappingAsyncHandler<AgentDescription>(AgentDescription.class))
                                    .get();

        Action deploy = Iterables.find(root.getActions(), beanPropertyEquals("rel", "deploy"));

        // perform a deployment against it
        _Deployment d = new _Deployment();
        d.url = URI.create("file:///Users/brianm/src/galaxified-apache/target/galaxified-apache-0.0.1-SNAPSHOT-x86_64-darwin11.2.0.tar.gz");
        SlotDescription c = http.preparePost(deploy.getUri().toString())
                                .setHeader("content-type", MediaType.APPLICATION_JSON)
                                .setBody(mapper.writeValueAsString(d))
                                .execute(new JsonMappingAsyncHandler<SlotDescription>(SlotDescription.class))
                                .get();

        // start the deployed thing
        Action start = Iterables.find(c.getActions(), beanPropertyEquals("rel", "start"));

        Response start_response = http.preparePost(start.getUri().toString())
                                      .execute()
                                      .get();

        // http client does not follow redirects correctly, so we
        // need to hardcode following the see other :-(
        assertThat(start_response.getStatusCode(), isHttpRedirect());
        String slot_uri = start_response.getHeader("location");

        // check to make sure the deployed thing is now running
        SlotDescription started = http.prepareGet(slot_uri)
                                      .setHeader("accept", MediaType.APPLICATION_JSON)
                                      .execute(new JsonMappingAsyncHandler<SlotDescription>(SlotDescription.class))
                                      .get();

        System.out.println(started.getDeployDir().getAbsolutePath());

//        Thread.currentThread().join();

        assertThat(started.getState(), equalTo("running"));

        Action stop = Iterables.find(c.getActions(), beanPropertyEquals("rel", "stop"));
        http.preparePost(stop.getUri().toString())
            .execute()
            .get();
    }

    @Test
    public void testDeployBundleThatDoesNotExist() throws Exception
    {
        // Find deployment url/action
        AgentDescription root = http.prepareGet("http://localhost:25365/")
                                    .setHeader("accept", MediaType.APPLICATION_JSON)
                                    .execute(new JsonMappingAsyncHandler<AgentDescription>(AgentDescription.class))
                                    .get();

        Action deploy = Iterables.find(root.getActions(), beanPropertyEquals("rel", "deploy"));

        assertThat(deploy.getMethod(), equalTo("POST"));
        assertThat(deploy.getParams().keySet(), equalTo((Set<String>) ImmutableSet.of("name", "url", "configuration")));


        _Deployment d = new _Deployment();
        d.url = URI.create("file:///tmp/does_not_exist.tar.gz");
        // perform a deployment against it
        Response r = http.preparePost(deploy.getUri().toString())
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
        AgentDescription root = http.prepareGet("http://localhost:25365/")
                                    .setHeader("accept", MediaType.APPLICATION_JSON)
                                    .execute(new JsonMappingAsyncHandler<AgentDescription>(AgentDescription.class))
                                    .get();

        Action deploy = Iterables.find(root.getActions(), beanPropertyEquals("rel", "deploy"));

        _Deployment d = new _Deployment();
        d.url = URI.create("http://localhost:25365/kjhasdjkhasdjkhasdhasdjkh");
        // perform a deployment against it
        Response r = http.preparePost(deploy.getUri().toString())
                         .setHeader("content-type", MediaType.APPLICATION_JSON)
                         .setBody(mapper.writeValueAsString(d))
                         .execute()
                         .get();

        assertThat(r.getStatusCode(), isHttpBadRequest());
    }

    @Test
    public void testDeployedThingListedAtRoot() throws Exception
    {
        AgentDescription root = http.prepareGet("http://localhost:25365/")
                                    .setHeader("accept", MediaType.APPLICATION_JSON)
                                    .execute(new JsonMappingAsyncHandler<AgentDescription>(AgentDescription.class))
                                    .get();

        Action deploy = Iterables.find(root.getActions(), beanPropertyEquals("rel", "deploy"));

        SlotDescription from_deploy = http.preparePost(deploy.getUri().toString())
                                          .setHeader("content-type", MediaType.APPLICATION_JSON)
                                          .setBody(mapper.writeValueAsString(new _Deployment()))
                                          .execute(new JsonMappingAsyncHandler<SlotDescription>(SlotDescription.class))
                                          .get();

        URI self_url = Iterables.find(from_deploy.getLinks(), beanPropertyEquals("rel", "self")).getUri();

        AgentDescription root2 = http.prepareGet("http://localhost:25365/")
                                     .setHeader("accept", MediaType.APPLICATION_JSON)
                                     .execute(new JsonMappingAsyncHandler<AgentDescription>(AgentDescription.class))
                                     .get();

        boolean found_self = false;
        for (SlotDescription slot : root2.getSlots()) {
            for (Link link : slot.getLinks()) {
                found_self = found_self || link.getUri().equals(self_url);
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
                                        beanPropertyEquals("rel", "deploy"));
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

}
