package org.skife.galaxy.cli;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ning.http.client.AsyncHttpClient;
import org.skife.cli.Arguments;
import org.skife.cli.Command;
import org.skife.cli.Option;
import org.skife.galaxy.rep.ConsoleAgentDescription;
import org.skife.galaxy.rep.ConsoleDescription;
import org.skife.galaxy.rep.SlotDescription;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.List;
import java.util.concurrent.Callable;

import static org.skife.galaxy.http.JsonMappingAsyncHandler.fromJson;

@Command(name = "list")
public class ConsoleList implements Callable<Void>
{

    @Option(name = {"-c", "--console"},
            title = "console",
            description = "Console URL, default is http://localhost:36525",
            configuration = "console")
    public URI console = URI.create("http://localhost:36525");


    @Arguments
    public List<String> arguments = Lists.newArrayList();

    @Override
    public Void call() throws Exception
    {
        AsyncHttpClient http = new AsyncHttpClient();
        try {
            ConsoleDescription cd = http.prepareGet(console.toString())
                                        .setHeader("Accept", MediaType.APPLICATION_JSON)
                                        .execute(fromJson(ConsoleDescription.class))
                                        .get();


            if (ImmutableSet.copyOf(arguments).contains("agents")) {
                for (ConsoleAgentDescription d : cd.getAgents()) {
                    System.out.printf("%s\t%s\n", d.getAgent().getId(), d.getAgent().getSelfLink().getUri());
                }
            }
            else {
                for (ConsoleAgentDescription d : cd.getAgents()) {
                    for (SlotDescription slot : d.getAgent().getSlots()) {
                        System.out.printf("%s\t%s\t%s\t%s\n",
                                          slot.getId(),
                                          slot.getName(),
                                          slot.getSelfLink().getUri(),
                                          slot.getState());
                    }
                }
            }
        }
        finally {
            http.close();
        }

        return null;
    }
}
