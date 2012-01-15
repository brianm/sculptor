package org.skife.galaxy.http;

import java.net.URI;


public class Link
{
    private final String rel;
    private final URI    uri;
    private final String title;

    public Link(String rel, URI uri, String title)
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
    public String toString()
    {
        return new StringBuilder("<").append(uri.toString())
                                     .append(">; rel=\"")
                                     .append(rel)
                                     .append("\"; title=\"")
                                     .append(title)
                                     .append("\"")
                                     .toString();
    }
}
