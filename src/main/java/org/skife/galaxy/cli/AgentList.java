package org.skife.galaxy.cli;

import com.ning.http.client.AsyncHttpClient;
import org.skife.cli.Command;
import org.skife.cli.Option;
import org.skife.galaxy.http.JsonMappingAsyncHandler;
import org.skife.galaxy.rep.AgentDescription;
import org.skife.galaxy.rep.SlotDescription;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.concurrent.Callable;

@Command(name = {"list", "ls"}, description = "List slots on an agent")
public class AgentList implements Callable<Void>
{
    @Option(description = "Agent URL", name = {"-a", "--agent"}, title = "agent-url")
    public URI agentUri = URI.create("http://localhost:25365/");

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
                System.out.printf("%s\t%s\t%s\t%s\n", slot.getId(), slot.getName(),  slot.getBundleUrl(), slot.getState());
            }

            return null;
        }
        finally {
            http.close();
        }
    }
}
