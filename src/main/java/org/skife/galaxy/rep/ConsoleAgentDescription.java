package org.skife.galaxy.rep;

import com.google.common.collect.ImmutableList;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.skife.galaxy.console.http.ConsoleAgentResource;

import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

public class ConsoleAgentDescription
{
    private final AgentDescription agent;
    private final List<Link>       links;
    private final List<Action>     actions;
    private final String           state;

    @JsonCreator
    public ConsoleAgentDescription(@JsonProperty("agent") AgentDescription agent,
                                   @JsonProperty("state") String state,
                                   @JsonProperty("_links") List<Link> links,
                                   @JsonProperty("_actions") List<Action> actions)
    {
        this.agent = agent;
        this.links = ImmutableList.copyOf(links);
        this.actions = ImmutableList.copyOf(actions);
        this.state = state;
    }

    public AgentDescription getAgent()
    {
        return agent;
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

    public String getState()
    {
        return state;
    }

    public static ConsoleAgentDescription from(UriInfo ui, AgentDescription id, String state)
    {
        Link self = new Link("agent-on-console",
                             ui.getBaseUriBuilder().path(ConsoleAgentResource.class).build(id.getId()),
                             "Representation of the agent on the console");
        Action remove = new Action("remove-agent",
                                   "DELETE",
                                   ui.getBaseUriBuilder().path(ConsoleAgentResource.class).build(id.getId()),
                                   Collections.<String, String>emptyMap());
        return new ConsoleAgentDescription(id, state, asList(self), asList(remove));
    }
}
