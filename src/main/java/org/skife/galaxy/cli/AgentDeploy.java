package org.skife.galaxy.cli;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.iq80.cli.Arguments;
import org.iq80.cli.Command;
import org.iq80.cli.Option;
import org.skife.galaxy.http.JsonMappingAsyncHandler;
import org.skife.galaxy.rep.Action;
import org.skife.galaxy.rep.AgentDescription;
import org.skife.galaxy.rep.SlotDescription;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.concurrent.Callable;

import static org.skife.galaxy.base.MorePredicates.beanPropertyEquals;

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
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);

        AsyncHttpClient http = new AsyncHttpClient();
        try {
            AgentDescription root = http.prepareGet(agentUri.toString())
                                        .setHeader("accept", MediaType.APPLICATION_JSON)
                                        .execute(new JsonMappingAsyncHandler<AgentDescription>(AgentDescription.class))
                                        .get();

            Action deploy = Iterables.find(root.getActions(), beanPropertyEquals("rel", "deploy"));
            Preconditions.checkNotNull(deploy, "No deploy action found at %s", agentUri);

            SlotDescription slot = http.preparePost(deploy.getUri().toString())
                                       .setHeader("content-type", MediaType.APPLICATION_JSON)
                                       .setBody(mapper.writeValueAsString(new PostBody(bundleUri.toString(), name)))
                                       .execute(new JsonMappingAsyncHandler<SlotDescription>(SlotDescription.class))
                                       .get();

            System.out.println(mapper.writeValueAsString(slot));
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
