package org.skife.galaxy;

import java.net.URL;

public class Deployment
{
    private final String name;
    private final URL tarball;

    public Deployment(String name, URL tarball)
    {
        this.name = name;
        this.tarball = tarball;
    }

    public URL getTarballUrl()
    {
        return tarball;
    }

    public String getName()
    {
        return name;
    }
}
