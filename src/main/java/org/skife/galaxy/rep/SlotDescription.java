package org.skife.galaxy.rep;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonProperty;
import org.skife.galaxy.agent.Slot;
import org.skife.galaxy.agent.http.SlotResource;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;

public class SlotDescription
{
    private final List<Link>   _links;
    private final List<Action> _actions;
    private final String       state;
    private final UUID         id;
    private final File         deployDir;
    private final String       name;
    private final URI bundleUrl;


    public SlotDescription(@JsonProperty("_links") List<Link> links,
                           @JsonProperty("_actions") List<Action> actions,
                           @JsonProperty("state") String state,
                           @JsonProperty("id") UUID id,
                           @JsonProperty("deploy_dir") File deployDir,
                           @JsonProperty("name") String name,
                           @JsonProperty("bundle-url") URI bundleUrl)
    {
        this.id = id;
        this.deployDir = deployDir;
        this.name = name;
        this.bundleUrl = bundleUrl;
        this._links = ImmutableList.copyOf(links);
        this._actions = ImmutableList.copyOf(actions);
        this.state = state;
    }


    public static SlotDescription from(Slot slot, UriInfo ui)
    {
        final URI slot_uri = UriBuilder.fromResource(SlotResource.class)
                                       .host(ui.getRequestUri().getHost())
                                       .port(ui.getRequestUri().getPort())
                                       .scheme(ui.getRequestUri().getScheme())
                                       .build(slot.getId());

        List<Link> links = asList(new Link("self",
                                           slot_uri,
                                           "slot resource"));
        URI start_uri = UriBuilder.fromUri(slot_uri)
                                  .path(SlotResource.class, "start")
                                  .build(slot.getId());

        URI stop_uri = UriBuilder.fromUri(slot_uri)
                                 .path(SlotResource.class, "stop")
                                 .build(slot.getId());

        URI restart_uri = UriBuilder.fromUri(slot_uri)
                                    .path(SlotResource.class, "stop")
                                    .build(slot.getId());

        URI clear_uri = UriBuilder.fromUri(slot_uri)
                                  .path(SlotResource.class, "stop")
                                  .build(slot.getId());

        List<Action> _actions = asList(new Action("start", "POST", start_uri),
                                       new Action("restart", "POST", restart_uri),
                                       new Action("clear", "POST", clear_uri),
                                       new Action("stop", "POST", stop_uri));

        return new SlotDescription(links,
                                   _actions,
                                   slot.getState(),
                                   slot.getId(),
                                   slot.getDeployDir(),
                                   slot.getName(),
                                   slot.getBundleUrl());
    }

    @JsonProperty("bundle-url")
    public URI getBundleUrl()
    {
        return bundleUrl;
    }

    @JsonProperty("deploy_dir")
    public File getDeployDir()
    {
        return deployDir;
    }

    @JsonProperty("_links")
    public List<Link> getLinks()
    {
        return _links;
    }

    @JsonProperty("_actions")
    public List<Action> getActions()
    {
        return _actions;
    }

    public String getState()
    {
        return state;
    }

    public UUID getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public boolean equals(Object o)
    {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
