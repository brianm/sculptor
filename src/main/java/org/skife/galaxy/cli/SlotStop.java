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
import org.skife.galaxy.rep.ConsoleAgentDescription;
import org.skife.galaxy.rep.ConsoleDescription;
import org.skife.galaxy.rep.SlotDescription;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.concurrent.Callable;

import static com.google.common.collect.Iterables.find;
import static org.skife.galaxy.base.MorePredicates.beanPropertyEquals;
import static org.skife.galaxy.http.JsonMappingAsyncHandler.fromJson;

@Command(name = "stop", description = "stop service in the specified slot")
public class SlotStop implements Callable<Void>
{
    @Option(description = "Agent URL", name = {"-a", "--agent"}, title = "agent-url")
    public URI agentUri;

    @Option(description = "Console URL", name = {"-c", "--console"}, title = "agent-url", configuration = "console")
    public URI consoleUti;

    @Arguments(title = "slot-id", required = true, description = "Slot to start")
    public String slotId;

    @Override
    public Void call() throws Exception
    {
        AsyncHttpClient http = new AsyncHttpClient();
        try {
            if (agentUri != null) {
                stopFromAgwnt(agentUri, http);
            }
            else if (consoleUti != null) {
                stopFromConsole(http);
            }
            else {
                System.err.println("You must specify either an agent or console!");
                System.exit(1);
            }
        }
        finally {
            http.close();
        }
        return null;
    }

    private void stopFromConsole(AsyncHttpClient http) throws Exception
    {
        ConsoleDescription cd = http.prepareGet(consoleUti.toString())
                                    .setHeader("Accept", MediaType.APPLICATION_JSON)
                                    .execute(fromJson(ConsoleDescription.class))
                                    .get();
        for (ConsoleAgentDescription agentDescription : cd.getAgents()) {
            for (SlotDescription slot : agentDescription.getAgent().getSlots()) {
                if (slot.getId().toString().startsWith(slotId)) {
                    stopFromAgwnt(find(agentDescription.getAgent()
                                                       .getLinks(), beanPropertyEquals("rel", "self")).getUri(), http);
                    return;
                }
            }
        }
    }

    private void stopFromAgwnt(URI agent, AsyncHttpClient http) throws Exception
    {
        AgentDescription root = http.prepareGet(agent.toString())
                                    .setHeader("accept", MediaType.APPLICATION_JSON)
                                    .execute(new JsonMappingAsyncHandler<AgentDescription>(AgentDescription.class))
                                    .get();

        for (SlotDescription slot : root.getSlots()) {
            if (slot.getId().toString().startsWith(slotId)) {
                Action start = Iterables.find(slot.getActions(), beanPropertyEquals("rel", "stop"));
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
    }
}
