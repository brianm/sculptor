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
import java.io.IOException;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static com.google.common.collect.Iterables.find;
import static org.skife.galaxy.base.MorePredicates.beanPropertyEquals;
import static org.skife.galaxy.http.JsonMappingAsyncHandler.fromJson;

@Command(name = "start", description = "start service in the specified slot")
public class SlotStart implements Callable<Void>
{
    @Option(description = "Agent URL", name = {"-a", "--agent"}, title = "agent-url", configuration = "agent")
    public URI agentUri = URI.create("http://localhost:25365/");

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
                startFromAgent(agentUri, slotId, http);
            }
            else if (consoleUti != null) {
                startFromConsole(consoleUti, slotId, http);
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

    public static void startFromConsole(URI consoleUti, String slotId, AsyncHttpClient http) throws Exception
    {
        ConsoleDescription cd = http.prepareGet(consoleUti.toString())
                                    .setHeader("Accept", MediaType.APPLICATION_JSON)
                                    .execute(fromJson(ConsoleDescription.class))
                                    .get();
        for (ConsoleAgentDescription agentDescription : cd.getAgents()) {
            for (SlotDescription slot : agentDescription.getAgent().getSlots()) {
                if (slot.getId().toString().startsWith(slotId)) {
                    startFromAgent(find(agentDescription.getAgent().getLinks(), beanPropertyEquals("rel", "self")).getUri(),
                                   slotId,
                                   http);
                    return;
                }
            }
        }
    }

    public static void startFromAgent(URI agent, String slotId, AsyncHttpClient http) throws Exception
    {
        AgentDescription root = http.prepareGet(agent.toString())
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
    }
}
