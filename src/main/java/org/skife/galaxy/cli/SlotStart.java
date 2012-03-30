package org.skife.galaxy.cli;

import com.google.common.collect.Iterables;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;
import org.skife.cli.Arguments;
import org.skife.cli.Command;
import org.skife.cli.Option;
import org.skife.galaxy.http.JsonMappingAsyncHandler;
import org.skife.galaxy.rep.Action;
import org.skife.galaxy.rep.AgentDescription;
import org.skife.galaxy.rep.SlotDescription;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.concurrent.Callable;

import static org.skife.galaxy.base.MorePredicates.beanPropertyEquals;

@Command(name = "start", description = "start service in the specified slot")
public class SlotStart implements Callable<Void>
{
    @Option(description = "Agent URL", name = {"-a", "--agent"}, title = "agent-url")
    public URI agentUri = URI.create("http://localhost:25365/");

    @Arguments(title = "slot-id", required = true, description = "Slot to start")
    public String slotId;

    @Override
    public Void call() throws Exception
    {

        AsyncHttpClient http = new AsyncHttpClient();
        try {
            AgentDescription root = http.prepareGet(agentUri.toString())
                                        .setHeader("accept", MediaType.APPLICATION_JSON)
                                        .execute(new JsonMappingAsyncHandler<AgentDescription>(AgentDescription.class))
                                        .get();

            for (SlotDescription slot : root.getSlots()) {
                if (slot.getId().toString().startsWith(slotId)) {
                    Action start = Iterables.find(slot.getActions(), beanPropertyEquals("rel", "start"));
                    Response r = http.prepareRequest(new RequestBuilder(start.getMethod())
                                                         .setUrl(start.getUri().toString())
                                                         .setHeader("content-type", MediaType.APPLICATION_JSON)
                                                         .build())
                                     .execute().get();
                    int status = r.getStatusCode();

                    if (status >= 400) {
                        // user error
                        System.err.println(r.getResponseBody());
                    }
                }
            }

            return null;
        }
        finally {
            http.close();
        }
    }
}
