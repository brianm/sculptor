package org.skife.galaxy.cli;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.ning.http.client.AsyncHttpClient;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.skife.cli.Arguments;
import org.skife.cli.Command;
import org.skife.cli.Option;
import org.skife.galaxy.http.JsonMappingAsyncHandler;
import org.skife.galaxy.rep.Action;
import org.skife.galaxy.rep.AgentDescription;
import org.skife.galaxy.rep.DeploymentDescription;
import org.skife.galaxy.rep.SlotDescription;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkState;
import static org.skife.galaxy.base.MorePredicates.beanPropertyEquals;

@Command(name = "deploy", description = "Deploy tarball to an agent")
public class AgentDeploy implements Callable<Void>
{
    @Option(description = "Agent URL", name = {"-a", "--agent"}, title = "agent-url")
    public URI agentUri = URI.create("http://localhost:25365/");

    @Option(description = "Name for the deployed thing", name = {"-n", "--name"}, title = "Deployment name")
    public String name = "Someone forgot to name me!";

    @Option(name = {"-c", "--config"},
            title = "config-key-value-pair",
            description = "Config entry of form '/path/in/deploy=http://url/to-fetch', multiple okay")
    public List<String> rawConfiguration = Collections.emptyList();

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

        Splitter first_equals = Splitter.on('=').limit(1);
        ImmutableMap.Builder<String, URI> config_builder =  ImmutableMap.builder();
        for (String raw : rawConfiguration) {
            Iterator<String> itty = first_equals.split(raw).iterator();
            checkState(itty.hasNext(), "--config must be of the form <path>=<url>");
            String path = itty.next();
            checkState(itty.hasNext(), "--config must be of the form <path>=<url>");
            URI uri = URI.create(itty.next());
            config_builder.put(path, uri);
        }
        Map<String, URI> config = config_builder.build();

        AsyncHttpClient http = new AsyncHttpClient();
        try {
            AgentDescription root = http.prepareGet(agentUri.toString())
                                        .setHeader("accept", MediaType.APPLICATION_JSON)
                                        .execute(new JsonMappingAsyncHandler<AgentDescription>(AgentDescription.class))
                                        .get();

            Action deploy = Iterables.find(root.getActions(), beanPropertyEquals("rel", "deploy"));
            Preconditions.checkNotNull(deploy, "No deploy action found at %s", agentUri);

            DeploymentDescription description = new DeploymentDescription(name, bundleUri, config);
            SlotDescription slot = http.preparePost(deploy.getUri().toString())
                                       .setHeader("content-type", MediaType.APPLICATION_JSON)
                                       .setBody(mapper.writeValueAsString(description))
                                       .execute(new JsonMappingAsyncHandler<SlotDescription>(SlotDescription.class))
                                       .get();

            System.out.println(mapper.writeValueAsString(slot));
        }
        finally {
            http.close();
        }
        return null;
    }
}
