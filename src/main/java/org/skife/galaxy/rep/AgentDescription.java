package org.skife.galaxy.rep;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AgentDescription
{
    private final UUID                  id;
    private final File                  root;
    private final Map<String, URI>      environment;
    private final List<SlotDescription> slots;
    private final List<Action>          _actions;
    private final List<Link>            _links;

    @JsonCreator
    public AgentDescription(@JsonProperty("_links") List<Link> _links,
                            @JsonProperty("_actions") List<Action> _actions,
                            @JsonProperty("environment") Map<String, URI> environment,
                            @JsonProperty("slots") List<SlotDescription> slots,
                            @JsonProperty("root") File root,
                            @JsonProperty("id") UUID id)
    {
        this.slots =  slots == null ? Collections.<SlotDescription>emptyList() : ImmutableList.copyOf(slots);
        this._actions = ImmutableList.copyOf(_actions);
        this._links = ImmutableList.copyOf(_links);
        this.environment = environment;
        this.root = root;
        this.id = id;
    }


    public UUID getId()
    {
        return id;
    }

    public File getRoot()
    {
        return root;
    }

    public Map<String, URI> getEnvironment()
    {
        return environment;
    }

    public List<SlotDescription> getSlots()
    {
        return slots;
    }

    @JsonProperty("_actions")
    public List<Action> getActions()
    {
        return _actions;
    }

    @JsonProperty("_links")
    public List<Link> getLinks()
    {
        return _links;
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
