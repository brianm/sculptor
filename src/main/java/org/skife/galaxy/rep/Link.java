package org.skife.galaxy.rep;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.net.URI;


public class Link
{
    private final String rel;
    private final URI    uri;
    private final String title;

    @JsonCreator
    public Link(@JsonProperty("rel") String rel,
                @JsonProperty("uri") URI uri,
                @JsonProperty("title") String title)
    {
        this.rel = rel;
        this.uri = uri;
        this.title = title;
    }

    public String getRel()
    {
        return rel;
    }

    public URI getUri()
    {
        return uri;
    }

    public String getTitle()
    {
        return title;
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
