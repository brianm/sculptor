package org.skife.galaxy.http;

import java.net.URI;
import java.util.Map;

public class Action
{
    private final String rel;
    private final String method;
    private final URI uri;
    private final Map<String, String> params;

    public Action(String rel, String method, URI uri, Map<String, String> params)
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
