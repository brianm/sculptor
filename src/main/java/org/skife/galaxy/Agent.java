package org.skife.galaxy;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.google.common.util.concurrent.MoreExecutors;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Agent
{
    public static final ExecutorService EXEC_POOL = MoreExecutors.getExitingExecutorService(new ThreadPoolExecutor(1, 100, 100, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>()));

    private final Map<UUID, Slot> slots = Maps.newConcurrentMap();
    private final File root;

    @Inject
    public Agent(@AgentRoot File root) throws IOException
    {
        this.root = root;
        for (File path : root.listFiles()  ) {
            if (path.isDirectory()) {
                Slot slot = Slot.from(path);
                slots.put(slot.getUuid(), slot);
            }
        }
    }

    public Slot deploy(Deployment d) throws IOException, CommandFailedException
    {
        UUID uuid = UUID.randomUUID();
        File deployment_dir = new File(root, uuid.toString());
        Preconditions.checkState(deployment_dir.mkdirs(), "Unable to create deployment directory");
        File tmp_tarball = File.createTempFile("pleides", ".tar.gz");

        final URL url = d.getTarballUrl();
        Files.copy(new InputSupplier<InputStream>()
        {
            public InputStream getInput() throws IOException
            {
                return url.openStream();
            }
        }, tmp_tarball);

        File tmp = Files.createTempDir();
        Command c = new Command("tar",
                                "-C", tmp.getAbsolutePath(),
                                "-xf", tmp_tarball.getAbsolutePath())
            .setTimeLimit(10, TimeUnit.SECONDS);
        int out = c.execute(EXEC_POOL);

        Preconditions.checkState(out == 0);

        Preconditions.checkState(tmp.listFiles().length == 1, "Too many directories in root of expanded tarball");
        File dir = tmp.listFiles()[0];

        Files.move(dir, new File(deployment_dir, "deploy"));

        Slot slot = new Slot(uuid, d.getName(), deployment_dir);
        this.slots.put(uuid, slot);
        return slot;
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


    public Slot getSlot(UUID uuid)
    {
        return slots.get(uuid);
    }

    public Map<UUID, Slot> getSlots()
    {
        return ImmutableMap.copyOf(slots);
    }
}
