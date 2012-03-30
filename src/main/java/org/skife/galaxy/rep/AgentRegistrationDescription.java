package org.skife.galaxy.rep;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.codehaus.jackson.annotate.JsonProperty;
import org.skife.galaxy.console.http.ConsoleAgentResource;

import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.List;

public class AgentRegistrationDescription
{
    private final List<Link>   links;
    private final List<Action> actions;

    public AgentRegistrationDescription(@JsonProperty("_links") List<Link> links,
                                        @JsonProperty("_actions") List<Action> actions)
    {
        this.links = links;
        this.actions = actions;
    }

    public static AgentRegistrationDescription from(AgentDescription ad, UriInfo ui)
    {
        List<Link> links = ImmutableList.of(new Link("agent-registration",
                                                     ui.getBaseUriBuilder()
                                                       .path(ConsoleAgentResource.class)
                                                       .build(ad.getId()),
                                                     "Agent registration URL"));

        List<Action> actions = ImmutableList.of(new Action("check-in",
                                                           "POST",
                                                           ui.getBaseUriBuilder()
                                                             .path(ConsoleAgentResource.class)
                                                             .build(ad.getId()),
                                                           Collections.<String, String>emptyMap()),
                                                new Action("update",
                                                           "PUT",
                                                           ui.getBaseUriBuilder()
                                                             .path(ConsoleAgentResource.class)
                                                             .build(ad.getId()),
                                                           ImmutableMap.of("<root>", "json agent description")
                                                ));
        return new AgentRegistrationDescription(links, actions);
    }


    @JsonProperty("_links")
    public List<Link> getLinks()
    {
        return links;
    }

    @JsonProperty("_actions")
    public List<Action> getActions()
    {
        return actions;
    }
}
