package org.skife.galaxy.console.http;

import org.skife.galaxy.console.Console;
import org.skife.galaxy.rep.AgentDescription;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.UUID;

import static com.google.common.collect.Iterables.find;
import static org.skife.galaxy.base.MorePredicates.beanPropertyEquals;

@Path("agent/{id}")
public class ConsoleAgentResource
{
    private final Console console;

    @Inject
    public ConsoleAgentResource(Console console) {
        this.console = console;
    }

    @PUT
    public AgentDescription update(AgentDescription agent)
    {
        console.register(agent);
        return agent;
    }

    @GET
    public AgentDescription index(@PathParam("id") UUID id)
    {
        return find(console.getAgents(), beanPropertyEquals("id", id));
    }

    @POST
    public Response checkIn(@PathParam("id") UUID id)
    {
        console.checkInAgent(id);
        return Response.ok().build();
    }

}
