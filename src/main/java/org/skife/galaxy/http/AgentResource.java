package org.skife.galaxy.http;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.sun.jersey.api.view.Viewable;
import org.skife.galaxy.Agent;
import org.skife.galaxy.Deployment;
import org.skife.galaxy.Slot;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.Arrays.asList;

@Path("/")
public class AgentResource
{
    private final UriInfo ui;
    private final Agent   agent;

    @Inject
    public AgentResource(UriInfo ui, Agent agent)
    {
        this.ui = ui;
        this.agent = agent;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response indexJson()
    {
        return explicitJson();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("index.json")
    public Response explicitJson()
    {
        final URI authoritative = ui.getAbsolutePathBuilder().path(AgentResource.class, "explicitJson").build();
        final URI deploy = ui.getAbsolutePathBuilder().path(AgentResource.class, "deploy").build();
        final List<Action> acts = asList(new Action("deploy", "POST", deploy,
                                                    ImmutableMap.of("name", "Service name",
                                                                    "url", "Deployment bundle URL")));
        final Link json_link = new Link("alternate", authoritative, "JSON URL");
        return Response.ok()
                       .header("Link", json_link.toString())
                       .entity(new Object()
                       {
                           public final File root = agent.getRoot();
                           public final List<SlotDescription> slots = describe(agent.getSlots());
                           public final List<Action> _actions = acts;
                           public final List<Link> _links = asList(json_link);
                       })
                       .build();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Viewable index() throws UnknownHostException
    {
        final Map<UUID, Slot> raw_slots = agent.getSlots();

        return new Viewable("index.html", new Object()
        {
            Agent agent = AgentResource.this.agent;
            String hostname = InetAddress.getLocalHost().getHostName();
            String ip = InetAddress.getLocalHost().getHostAddress();
            List<SlotDescription> slots = describe(raw_slots);
        });
    }

    @POST
    @Path("deploy")
    @Produces(MediaType.TEXT_HTML)
    public Response deploy(@FormParam("name") String name, @FormParam("url") URI tarball) throws IOException
    {
        final Slot s = agent.deploy(new Deployment(name, tarball));
        final URI slot_uri = UriBuilder.fromResource(SlotResource.class)
                                       .host(ui.getRequestUri().getHost())
                                       .port(ui.getRequestUri().getPort())
                                       .scheme(ui.getRequestUri().getScheme())
                                       .build(s.getId());
        return Response.created(slot_uri)
                       .location(slot_uri)
                       .entity(new Viewable("slot_created.html", new Object()
                       {
                           URI uri = slot_uri;
                       }))
                       .build();
    }

    @POST
    @Path("deploy")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deployJson(DeployJson json) throws IOException
    {
        final Slot s = agent.deploy(new Deployment(json.name, json.url));
        final URI slot_uri = UriBuilder.fromResource(SlotResource.class)
                                       .host(ui.getRequestUri().getHost())
                                       .port(ui.getRequestUri().getPort())
                                       .scheme(ui.getRequestUri().getScheme())
                                       .build(s.getId());

        return Response.created(slot_uri)
                       .entity(new SlotDescription(s, ui)).build();
    }

    private List<SlotDescription> describe(Map<UUID, Slot> raw_slots)
    {
        List<SlotDescription> rs = Lists.newArrayList();
        for (Map.Entry<UUID, Slot> entry : raw_slots.entrySet()) {
            rs.add(new SlotDescription(entry.getValue(),
                                       ui));
        }
        return rs;
    }

    public static class DeployJson
    {
        public URI    url;
        public String name;
    }
}
