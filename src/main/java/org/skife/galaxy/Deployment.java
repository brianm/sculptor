package org.skife.galaxy;

import java.net.URI;

public class Deployment
{
    private final String name;
    private final URI tarball;

    public Deployment(String name, URI tarball)
    {
        this.name = name;
        this.tarball = tarball;
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
