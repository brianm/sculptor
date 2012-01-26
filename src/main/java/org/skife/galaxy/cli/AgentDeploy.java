package org.skife.galaxy.cli;

import com.google.common.base.Preconditions;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.iq80.cli.Arguments;
import org.iq80.cli.Command;
import org.iq80.cli.Option;
import org.skife.galaxy.agent.http.Action;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "deploy", description = "Deploy tarball to an agent")
public class AgentDeploy implements Callable<Void>
{
    @Option(description = "Agent URL", name = {"-a", "--agent"})
    public URI agentUri = URI.create("http://localhost:25365/");

    @Option(description = "Name for the deployed thing", name = {"-n", "--name"})
    public String name = "Someone forgot to name me!";

    @Arguments(required = true)
    public String bundle;

    @Override
    public Void call() throws Exception
    {
        final URI bundleUri;
        if (bundle.matches("\\w+:.+")) {
            bundleUri = URI.create(bundle);
        }
        else {
            File f = new File(bundle);
            bundleUri = f.toURI();
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        AsyncHttpClient http = new AsyncHttpClient();
        try {
            Response r = http.executeRequest(new RequestBuilder().setUrl(agentUri.toString())
                                                                 .setHeader("Accept", MediaType.APPLICATION_JSON)
                                                                 .build()).get();
            RootBody root = mapper.readValue(r.getResponseBody(), RootBody.class);
            Action deploy = null;
            for (Action candidate : root._actions) {
                if ("deploy".equals(candidate.getRel())) {
                    deploy = candidate;
                }
            }
            Preconditions.checkNotNull(deploy, "No deploy action found at %s", agentUri);
            assert deploy != null;
            r = http.executeRequest(new RequestBuilder(deploy.getMethod())
                                        .setUrl(deploy.getUri().toString())
                                        .setHeader("Content-Type", "application/json")
                                        .setBody(mapper.writeValueAsString(new PostBody(bundleUri.toString(), name)))
                                        .build()).get();

            String location = r.getHeader("Location");
            System.out.println(r.getResponseBody());
        }
        finally {
            http.close();
        }
        return null;
    }

    public static class PostBody
    {
        PostBody(String url, String name)
        {
            this.url = url;
            this.name = name;
        }

        public String url;
        public String name;
    }

    public static class RootBody
    {
        public List<Action> _actions;
    }

}
