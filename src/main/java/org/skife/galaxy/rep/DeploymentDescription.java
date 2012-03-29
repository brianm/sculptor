package org.skife.galaxy.rep;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.net.URI;
import java.util.Map;

public class DeploymentDescription
{
    private final URI url;
    private final String name;
    private final Map<String, URI> configuration;

    @JsonCreator
    public DeploymentDescription(@JsonProperty("name") String name,
                                 @JsonProperty("url") URI url,
                                 @JsonProperty("configuration") Map<String, URI> configuration) {
        this.configuration = ImmutableMap.copyOf(configuration);
        this.name = name;
        this.url = url;

    }

    public URI getUrl()
    {
        return url;
    }

    public String getName()
    {
        return name;
    }

    public Map<String, URI> getConfiguration()
    {
        return configuration;
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
