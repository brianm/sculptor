package org.skife.galaxy.agent.http;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class StagedDeployment
{
    private final UUID id;
    private final AtomicReference<String> name = new AtomicReference<String>();
    private final AtomicReference<URI> bundle = new AtomicReference<URI>();
    private final Map<String, URI> config = Maps.newConcurrentMap();
    public StagedDeployment(UUID id)
    {
        this.id = id;
    }

    public void setName(String name)
    {
        this.name.set(name);
    }

    public String getName() {
        return name.get();
    }

    public void setBundle(URI bundle)
    {
        this.bundle.set(bundle);
    }

    public URI getBundle()
    {
        return bundle.get();
    }

    public void addConfig(String path, URI uri)
    {
        config.put(path, uri);
    }

    public List<ConfigurationItem> getConfiguration()
    {
        List<ConfigurationItem> rs = Lists.newArrayListWithExpectedSize(config.size());
        for (Map.Entry<String, URI> entry : config.entrySet()) {
            rs.add(new ConfigurationItem(entry.getKey(), entry.getValue()));
        }
        return rs;
    }
}
