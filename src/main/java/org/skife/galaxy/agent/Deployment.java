package org.skife.galaxy.agent;

import java.net.URI;
import java.util.Map;

public class Deployment
{
    private final String name;
    private final URI tarball;
    private final Map<String, URI> configuration;

    public Deployment(String name, URI tarball, Map<String, URI> configuration)
    {
        this.name = name;
        this.tarball = tarball;
        this.configuration = configuration;
    }

    public Map<String, URI> getConfiguration()
    {
        return configuration;
    }

    public URI getTarballUrl()
    {
        return tarball;
    }

    public String getName()
    {
        return name;
    }
}
