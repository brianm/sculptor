package org.skife.galaxy.rep;

import com.google.common.collect.ImmutableList;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

public class ListOfLinks
{
    private final List<Link> links;

    @JsonCreator
    public ListOfLinks(@JsonProperty("_links") List<Link> links)
    {
        this.links = ImmutableList.copyOf(links);
    }

    @JsonProperty("_links")
    public List<Link> getLinks()
    {
        return links;
    }
}
