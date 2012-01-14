package org.skife.galaxy.http;

import com.google.common.collect.Lists;
import com.sun.jersey.api.view.Viewable;
import org.skife.galaxy.Agent;
import org.skife.galaxy.Deployment;
import org.skife.galaxy.Slot;

import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/")
public class AgentResource
{
    private final Agent agent;

    @Inject
    public AgentResource(Agent agent)
    {
        this.agent = agent;
    }

    @GET
    @Produces("text/html")
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
    @Path("/deploy")
    public Response deploy(@FormParam("name") String name,  @FormParam("url") URL tarball) throws IOException
    {
        final Slot s = agent.deploy(new Deployment(name, tarball));

        final URI slot_uri = URI.create("/slot/" + s.getUuid().toString());
        return Response.created(slot_uri)
                       .location(slot_uri)
                       .entity(new Viewable("slot_created.html", new Object()
                       {
                           URI uri = slot_uri;
                       }))
                       .build();
    }

    private List<SlotDescription> describe(Map<UUID, Slot> raw_slots)
    {
        List<SlotDescription> rs = Lists.newArrayList();
        for (Map.Entry<UUID, Slot> entry : raw_slots.entrySet()) {
            rs.add(new SlotDescription(entry.getKey(), entry.getValue()));
        }
        return rs;
    }

    private static class SlotDescription
    {
        final UUID uuid;
        final Slot slot;

        SlotDescription(UUID uuid, Slot slot)
        {
            this.uuid = uuid;
            this.slot = slot;
        }
    }
}
