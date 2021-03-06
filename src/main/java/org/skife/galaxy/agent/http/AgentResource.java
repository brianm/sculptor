package org.skife.galaxy.agent.http;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.sun.jersey.api.view.Viewable;
import org.skife.galaxy.agent.Agent;
import org.skife.galaxy.agent.Consoles;
import org.skife.galaxy.agent.Deployment;
import org.skife.galaxy.agent.Slot;
import org.skife.galaxy.http.ErrorReport;
import org.skife.galaxy.rep.Action;
import org.skife.galaxy.rep.AgentDescription;
import org.skife.galaxy.rep.DeploymentDescription;
import org.skife.galaxy.rep.Link;
import org.skife.galaxy.rep.SlotDescription;

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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static java.util.Arrays.asList;

@Path("/")
public class AgentResource
{
    private final UriInfo      ui;
    private final Agent        agent;
    private final ScratchSpace scratch;
    private final Set<URI> consoles;

    @Inject
    public AgentResource(UriInfo ui, Agent agent, ScratchSpace scratch, @Consoles Set<URI> consoles)
    {
        this.ui = ui;
        this.agent = agent;
        this.scratch = scratch;
        this.consoles = consoles;
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
        final URI authoritative = ui.getBaseUriBuilder().path(AgentResource.class, "explicitJson").build();
        final URI self = ui.getBaseUriBuilder().path(AgentResource.class).build();
        final URI deploy = ui.getBaseUriBuilder().path(AgentResource.class, "deploy").build();

        final List<Action> acts = asList(new Action("deploy", "POST", deploy,
                                                    ImmutableMap.of("name", "Service name",
                                                                    "url", "Deployment bundle URL",
                                                                    "configuration", "Object where keys are paths inside deployment, and values are URLs to resources")));
        final Link json_link = new Link("json", authoritative, "JSON URL");
        final Link self_link = new Link("self", self, "Canonical URL");

        return Response.ok()
                       .header("Link", json_link.toString())
                       .entity(new AgentDescription(asList(self_link, json_link),
                                                    acts,
                                                    agent.getEnvironmentConfig(),
                                                    describe(agent.getSlots()),
                                                    agent.getRoot(),
                                                    agent.getId(),
                                                    consoles))
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
            List<EnvDescription> environment = describeEnv(agent.getEnvironmentConfig());
        });
    }

    @POST
    @Path("environment")
    @Produces(MediaType.TEXT_HTML)
    public Response environment(@FormParam("path") String path, @FormParam("url") URI url)
    {
        agent.addEnvironmentConfiguration(path, url);
        URI redirect = UriBuilder.fromUri(UriBuilder.fromResource(AgentResource.class).build()).build();
        return Response.seeOther(redirect).build();
    }

    @POST
    @Path("stage_deployment")
    @Produces(MediaType.TEXT_HTML)
    public Response stageDeployment() throws IOException
    {
        UUID dep_id = UUID.randomUUID();
        final URI dep_uri = UriBuilder.fromResource(StagedDeploymentResource.class).build(dep_id);
        scratch.stageDeployment(dep_id);
        return Response.seeOther(dep_uri).build();
    }

    @POST
    @Path("deploy")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deploy(DeploymentDescription json)
    {
        final Slot s;
        try {
            s = agent.deploy(new Deployment(json.getName(), json.getUrl(), json.getConfiguration()));
        }
        catch (FileNotFoundException e) {
            // url which didna exist
            return Response.status(400).entity(new ErrorReport("url didn't resolve to anything")).build();
        }
        catch (IOException e) {
            throw Throwables.propagate(e);
        }
        final URI slot_uri = UriBuilder.fromResource(SlotResource.class)
                                       .host(ui.getRequestUri().getHost())
                                       .port(ui.getRequestUri().getPort())
                                       .scheme(ui.getRequestUri().getScheme())
                                       .build(s.getId());

        return Response.created(slot_uri)
                       .location(slot_uri)
                       .entity(SlotDescription.from(s, ui)).build();
    }

    private List<SlotDescription> describe(Map<UUID, Slot> raw_slots)
    {
        List<SlotDescription> rs = Lists.newArrayList();
        for (Map.Entry<UUID, Slot> entry : raw_slots.entrySet()) {
            rs.add(SlotDescription.from(entry.getValue(), ui));
        }
        return rs;
    }

    private List<EnvDescription> describeEnv(Map<String, URI> envTable)
    {
        List<EnvDescription> rs = Lists.newArrayList();
        for (Map.Entry<String, URI> entry : envTable.entrySet()) {
            rs.add(new EnvDescription(entry.getKey(), entry.getValue()));
        }
        return rs;
    }

    private static class EnvDescription
    {
        public final String path;
        public final URI    url;

        EnvDescription(String path, URI url)
        {
            this.path = path;
            this.url = url;
        }

    }
}
