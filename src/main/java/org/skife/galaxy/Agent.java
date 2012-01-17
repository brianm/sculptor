package org.skife.galaxy;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.MoreExecutors;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Agent
{
    public static final ExecutorService EXEC_POOL = MoreExecutors.getExitingExecutorService(new ThreadPoolExecutor(1, 100, 100, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>()));

    private final Map<UUID, Slot> slots = Maps.newConcurrentMap();
    private final ConcurrentMap<String, URI> environmentConfig = Maps.newConcurrentMap();
    private final File root;

    @Inject
    public Agent(@AgentRoot File root) throws IOException
    {
        this.root = root;
        for (File path : root.listFiles()  ) {
            if (path.isDirectory()) {
                Slot slot = Slot.from(path);
                slots.put(slot.getId(), slot);
            }
        }
    }

    public Slot deploy(Deployment d) throws IOException, CommandFailedException
    {
        Slot s = Slot.deploy(this.root, d, environmentConfig);
        this.slots.put(s.getId(), s);
        return s;
    }

    public File getRoot()
    {
        return root;
    }

    public void clear(UUID slotId)
    {
        try {
            slots.get(slotId).clear();
        }
        catch (IOException e) {
            throw Throwables.propagate(e);
        }
        slots.remove(slotId);
    }

    public void addEnvironmentConfiguration(String path, URI source)
    {
        this.environmentConfig.put(path, source);
    }

    public Slot getSlot(UUID uuid)
    {
        return slots.get(uuid);
    }

    public Map<UUID, Slot> getSlots()
    {
        return ImmutableMap.copyOf(slots);
    }

    public ConcurrentMap<String, URI> getEnvironmentConfig()
    {
        return environmentConfig;
    }
}
