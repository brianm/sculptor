package org.skife.galaxy.agent;

import com.google.common.collect.Maps;

import java.net.URI;
import java.util.Map;

public class Deployment
{
    private final String name;
    private final URI tarball;
    private final Map<String, URI> configuration = Maps.newLinkedHashMap();

    public Deployment(String name, URI tarball, Map<String, URI> config)
    {
        this.name = name;
        this.tarball = tarball;
        if (config != null) {
            configuration.putAll(config);
        }
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
