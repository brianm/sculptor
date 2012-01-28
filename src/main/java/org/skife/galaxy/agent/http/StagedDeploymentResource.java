package org.skife.galaxy.agent.http;

import com.sun.jersey.api.view.Viewable;
import org.skife.galaxy.agent.Agent;
import org.skife.galaxy.agent.Deployment;
import org.skife.galaxy.agent.Slot;

import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@Path("staged_deployment/{id}/")
public class StagedDeploymentResource
{
    private final UriInfo ui;
    private final ScratchSpace scratch;
    private final Agent agent;

    @Inject
    public StagedDeploymentResource(UriInfo ui, ScratchSpace scratch, Agent agent)
    {
        this.ui = ui;
        this.scratch = scratch;
        this.agent = agent;
    }

    @GET
    public Viewable index(@PathParam("id") final UUID id)
    {
        return new Viewable("index.html", new Object() {
            StagedDeployment deployment = scratch.getStagedDeployment(id);
        });
    }

    @POST
    @Path("deploy")
    public Response deploy(@PathParam("id") UUID id) throws IOException
    {
        StagedDeployment sd = scratch.getStagedDeployment(id);
        Deployment d = sd.createDeployment();
        Slot s = agent.deploy(d);
        URI slot_uri = UriBuilder.fromUri(ui.getBaseUri()).path(SlotResource.class).build(s.getId());

        return Response.seeOther(slot_uri).build();
    }

    @POST
    @Path("name")
    public Response name(@PathParam("id") UUID id, @FormParam("name") String name)
    {
        StagedDeployment sd = scratch.getStagedDeployment(id);
        sd.setName(name);
        URI uri =  UriBuilder.fromUri(ui.getBaseUri()).path(StagedDeploymentResource.class).build(id);
        return Response.seeOther(uri).build();
    }

    @POST
    @Path("bundle")
    public Response bundle(@PathParam("id") UUID id, @FormParam("bundle") URI bundle)
    {
        StagedDeployment sd = scratch.getStagedDeployment(id);
        sd.setBundle(bundle);
        URI uri =  UriBuilder.fromUri(ui.getBaseUri()).path(StagedDeploymentResource.class).build(id);
        return Response.seeOther(uri).build();
    }

    @POST
    @Path("config")
    public Response config(@PathParam("id") UUID id,  @FormParam("path") String path, @FormParam("url") URI configUri)
    {
        StagedDeployment sd = scratch.getStagedDeployment(id);
        sd.addConfig(path, configUri);
        URI uri =  UriBuilder.fromUri(ui.getBaseUri()).path(StagedDeploymentResource.class).build(id);
        return Response.seeOther(uri).build();
    }
}
