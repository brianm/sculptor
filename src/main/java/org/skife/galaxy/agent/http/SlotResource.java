package org.skife.galaxy.agent.http;

import com.sun.jersey.api.view.Viewable;
import org.skife.galaxy.agent.Agent;
import org.skife.galaxy.agent.Slot;
import org.skife.galaxy.agent.Status;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@Path("/slot/{uuid}")
public class SlotResource
{
    private final UriInfo ui;
    private final Agent agent;

    @Inject
    public SlotResource(UriInfo ui, Agent agent)
    {
        this.ui = ui;
        this.agent = agent;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SlotDescription viewJson(final @PathParam("uuid") UUID uuid)
    {
        Slot slot = agent.getSlot(uuid);
        return new SlotDescription(slot, ui);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Viewable view(final @PathParam("uuid") UUID uuid)
    {
        final Slot s = agent.getSlot(uuid);
        final Status stat = s.status();
        return new Viewable("view_slot.html", new Object() {
            URI start = ui.getAbsolutePathBuilder().path(SlotResource.class, "start").build(uuid);
            URI stop = ui.getAbsolutePathBuilder().path(SlotResource.class, "stop").build(uuid);
            URI restart = ui.getAbsolutePathBuilder().path(SlotResource.class, "restart").build(uuid);
            URI clear = ui.getAbsolutePathBuilder().path(SlotResource.class, "clear").build(uuid);
            URI updateConfig = ui.getAbsolutePathBuilder().path(SlotResource.class, "updateConfig").build(uuid);
            Status status = stat;
            Slot slot = s;
        });
    }

    @POST
    @Path("start")
    public Response start(final @PathParam("uuid") UUID uuid)
    {
        Slot slot = agent.getSlot(uuid);
        slot.start();
        return Response.seeOther(UriBuilder.fromResource(SlotResource.class).build(uuid)).build();
    }

    @POST
    @Path("stop")
    public Response stop(final @PathParam("uuid") UUID uuid)
    {
        Slot slot = agent.getSlot(uuid);
        slot.stop();
        return Response.seeOther(UriBuilder.fromResource(SlotResource.class).build(uuid)).build();
    }

    @POST
    @Path("restart")
    public Response restart(final @PathParam("uuid") UUID uuid)
    {
        Slot slot = agent.getSlot(uuid);
        slot.restart();
        return Response.seeOther(UriBuilder.fromResource(SlotResource.class).build(uuid)).build();
    }

    @POST
    @Path("clear")
    public Response clear(final @PathParam("uuid") UUID uuid)
    {
        agent.clear(uuid);
        return Response.seeOther(UriBuilder.fromResource(AgentResource.class).build()).build();
    }

    @POST
    @Path("update-config")
    public Response updateConfig(final @PathParam("uuid") UUID uuid) throws IOException
    {
        Status s = agent.getSlot(uuid).updateConfig(agent.getEnvironmentConfig());
        return Response.seeOther(UriBuilder.fromResource(SlotResource.class).build(uuid)).build();
    }
}
