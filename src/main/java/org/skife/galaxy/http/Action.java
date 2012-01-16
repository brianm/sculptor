package org.skife.galaxy.http;

import org.codehaus.jackson.annotate.JsonProperty;

import java.net.URI;
import java.util.Map;

public class Action
{
    private final String              rel;
    private final String              method;
    private final URI                 uri;
    private final Map<String, String> params;

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
}
