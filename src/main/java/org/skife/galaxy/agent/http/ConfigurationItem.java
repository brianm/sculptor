package org.skife.galaxy.agent.http;

import java.net.URI;

public class ConfigurationItem
{
    private final String path;
    private final URI url;

    public ConfigurationItem(String path, URI url)
    {
        this.path = path;
        this.url = url;
    }

    public String getPath()
    {
        return path;
    }

    public URI getUrl()
    {
        return url;
    }
}
