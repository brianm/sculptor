package org.skife.galaxy.rep;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

public class Action
{
    private final String              rel;
    private final String              method;
    private final URI                 uri;
    private final Map<String, String> params;

    @JsonCreator
    public Action(@JsonProperty("rel") String rel,
                  @JsonProperty("method") String method,
                  @JsonProperty("uri") URI uri,
                  @JsonProperty("params") Map<String, String> params)
    {
        this.rel = rel;
        this.method = method;
        this.uri = uri;
        this.params = params;
    }

    public Action(String start, String post, URI start_uri)
    {
        this(start, post, start_uri, Collections.<String, String>emptyMap());
    }

    public Map<String, String> getParams()
    {
        return params;
    }

    public String getRel()
    {
        return rel;
    }

    public String getMethod()
    {
        return method;
    }

    public URI getUri()
    {
        return uri;
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
