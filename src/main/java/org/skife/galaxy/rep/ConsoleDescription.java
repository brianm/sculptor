package org.skife.galaxy.rep;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.skife.galaxy.console.Console;
import org.skife.galaxy.console.http.ConsoleResource;

import javax.ws.rs.core.UriInfo;
import java.util.List;

public class ConsoleDescription
{
    private final List<AgentDescription> agents;
    private final List<Link>             links;
    private       List<Action>           actions;

    @JsonCreator
    public ConsoleDescription(@JsonProperty("agents") List<AgentDescription> agents,
                              @JsonProperty("_links") List<Link> links,
                              @JsonProperty("_actions") List<Action> actions)
    {
        this.links = ImmutableList.copyOf(links);
        this.agents = ImmutableList.copyOf(agents);
        this.actions = ImmutableList.copyOf(actions);

    }

    public static ConsoleDescription createFrom(UriInfo ui, Console console)
    {
        return new ConsoleDescription(console.getAgents(),
                                      ImmutableList.of(new Link("self", ui.getBaseUriBuilder().build(), "console url")),
                                      ImmutableList.of(new Action("register-agent",
                                                                  "POST",
                                                                  ui.getBaseUriBuilder()
                                                                    .path(ConsoleResource.class, "registerAgent")
                                                                    .build())));
    }

    public List<AgentDescription> getAgents()
    {
        return agents;
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

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
