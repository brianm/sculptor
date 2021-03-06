package org.skife.galaxy.console.http;

import org.skife.galaxy.console.Console;
import org.skife.galaxy.rep.AgentDescription;
import org.skife.galaxy.rep.Link;
import org.skife.galaxy.rep.ListOfLinks;
import org.skife.galaxy.rep.ConsoleDescription;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static com.google.common.collect.Iterables.find;
import static org.skife.galaxy.base.MorePredicates.beanPropertyEquals;

@Path("/")
public class ConsoleResource
{

    private final UriInfo ui;
    private final Console console;

    @Inject
    public ConsoleResource(UriInfo ui, Console console)
    {
        this.ui = ui;
        this.console = console;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ConsoleDescription index()
    {
        return ConsoleDescription.createFrom(ui, console);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("register-agent")
    public Response registerAgent(ListOfLinks links)
    {
        Link agent = find(links.getLinks(), beanPropertyEquals("rel", "agent"));
        console.register(agent.getUri());
        return Response.ok().build();
    }
}
