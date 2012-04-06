package org.skife.galaxy.rep;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Iterables.find;
import static org.skife.galaxy.base.MorePredicates.beanPropertyEquals;

public class AgentDescription
{
    private final UUID                  id;
    private final File                  root;
    private final Map<String, URI>      environment;
    private final List<SlotDescription> slots;
    private final List<Action>          _actions;
    private final List<Link>            _links;
    private final List<URI>             consoles;

    @JsonCreator
    public AgentDescription(@JsonProperty("_links") List<Link> _links,
                            @JsonProperty("_actions") List<Action> _actions,
                            @JsonProperty("environment") Map<String, URI> environment,
                            @JsonProperty("slots") List<SlotDescription> slots,
                            @JsonProperty("root") File root,
                            @JsonProperty("id") UUID id,
                            @JsonProperty("consoles") Collection<URI> consoles)
    {
        this.slots = slots == null ? Collections.<SlotDescription>emptyList() : ImmutableList.copyOf(slots);
        this._actions = ImmutableList.copyOf(_actions);
        this._links = ImmutableList.copyOf(_links);
        this.consoles = ImmutableList.copyOf(consoles);
        this.environment = environment;
        this.root = root;
        this.id = id;
    }


    public List<URI> getConsoles()
    {
        return consoles;
    }

    @JsonIgnore
    public Link getSelfLink()
    {
        return find(_links, beanPropertyEquals("rel", "self"));
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
