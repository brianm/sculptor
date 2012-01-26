package org.skife.galaxy.agent.http;

import org.skife.galaxy.agent.Slot;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

import static java.util.Arrays.asList;

class SlotDescription
{
    final Slot         slot;
    final List<Link>   _links;
    final List<Action> _actions;

    SlotDescription(Slot slot, UriInfo ui)
    {
        this.slot = slot;
        final URI slot_uri = UriBuilder.fromResource(SlotResource.class)
                                       .host(ui.getRequestUri().getHost())
                                       .port(ui.getRequestUri().getPort())
                                       .scheme(ui.getRequestUri().getScheme())
                                       .build(slot.getId());

        _links = asList(new Link("self",
                                 slot_uri,
                                 "slot resource"));
        URI start_uri = UriBuilder.fromUri(ui.getRequestUri())
                                  .path(SlotResource.class, "start")
                                  .build(slot.getId());

        URI stop_uri = UriBuilder.fromUri(ui.getRequestUri())
                                 .path(SlotResource.class, "stop")
                                 .build(slot.getId());

        URI restart_uri = UriBuilder.fromUri(ui.getRequestUri())
                                    .path(SlotResource.class, "stop")
                                    .build(slot.getId());

        URI clear_uri = UriBuilder.fromUri(ui.getRequestUri())
                                  .path(SlotResource.class, "stop")
                                  .build(slot.getId());

        _actions = asList(new Action("start", "POST", start_uri),
                          new Action("restart", "POST", restart_uri),
                          new Action("clear", "POST", clear_uri),
                          new Action("stop", "POST", stop_uri));
    }

    public List<Link> get_links()
    {
        return _links;
    }

    public Slot getSlot()
    {
        return slot;
    }

    public List<Action> get_actions()
    {
        return _actions;
    }
}
