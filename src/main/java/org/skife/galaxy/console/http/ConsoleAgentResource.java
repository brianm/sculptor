package org.skife.galaxy.console.http;

import org.skife.galaxy.console.Console;
import org.skife.galaxy.rep.AgentDescription;
import org.skife.galaxy.rep.ConsoleAgentDescription;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.UUID;

import static com.google.common.collect.Iterables.find;
import static org.skife.galaxy.base.MorePredicates.beanPropertyEquals;

@Path("agent/{id}")
public class ConsoleAgentResource
{
    private final UriInfo ui;
    private final Console console;

    @Inject
    public ConsoleAgentResource(UriInfo ui, Console console) {
        this.ui = ui;
        this.console = console;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ConsoleAgentDescription index(@PathParam("id") UUID id)
    {
        return ConsoleAgentDescription.from(ui,
                                            find(console.getAgents(), beanPropertyEquals("id", id)),
                                            "running");
    }

    @DELETE
    public Response remove(@PathParam("id") UUID id) {
        this.console.remove(id);
        return Response.ok().build();
    }
}
