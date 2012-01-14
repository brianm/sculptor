package org.skife.galaxy;

import com.google.common.base.Charsets;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Slot
{
    private final String name;
    private final File root;
    private final File deployDir;
    private final UUID uuid;

    private static final String KEY = "key";

    private final LoadingCache<String, String> state = CacheBuilder.newBuilder()
                                                                   .maximumSize(1)
                                                                   .expireAfterWrite(5, TimeUnit.SECONDS)
                                                                   .build(new CacheLoader<String, String>()
                                                                   {
                                                                       @Override
                                                                       public String load(String key)
                                                                       {
                                                                           return status().getMessage();
                                                                       }
                                                                   });

    public Slot(UUID uuid, String name, File path) throws IOException
    {
        this.uuid = uuid;
        this.name = name;
        this.root = path;
        this.deployDir = new File(path, "deploy");

        Files.write(uuid.toString(), new File(root, "uuid"), Charsets.UTF_8);
        Files.write(name, new File(root, "name"), Charsets.UTF_8);
    }

    public File getDeployDir()
    {
        return deployDir;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return String.format("<Slot %s>", root);
    }

    public static Slot from(File root) throws IOException
    {
        String name = Files.readFirstLine(new File(root, "name"), Charsets.UTF_8);
        String uuid_s = Files.readFirstLine(new File(root, "uuid"), Charsets.UTF_8);
        return new Slot(UUID.fromString(uuid_s), name, root);
    }

    public File getRoot()
    {
        return root;
    }

    public Status start()
    {
        File control = new File(new File(deployDir, "bin"), "control");
        try {
            new Command(control.getAbsolutePath(), "start")
                .setSuccessfulExitCodes(0)
                .setTimeLimit(10, TimeUnit.SECONDS)
                .execute(Agent.EXEC_POOL);
            return Status.success();
        }
        catch (CommandFailedException e) {
            return Status.failure(e.getMessage());
        }
        finally {
            state.invalidate(KEY);
        }
    }

    public Status stop()
    {
        File control = new File(new File(deployDir, "bin"), "control");
        try {
            new Command(control.getAbsolutePath(), "stop")
                .setSuccessfulExitCodes(0)
                .setTimeLimit(10, TimeUnit.SECONDS)
                .execute(Agent.EXEC_POOL);
            return Status.success();
        }
        catch (CommandFailedException e) {
            return Status.failure(e.getMessage());
        }
        finally {
            state.invalidate(KEY);
        }
    }

    public Status status()
    {
        File control = new File(new File(deployDir, "bin"), "control");
        try {
            int state = new Command(control.getAbsolutePath(), "status")
                .setSuccessfulExitCodes(0, 1, 2, 3, 4)
                .setTimeLimit(10, TimeUnit.SECONDS)
                .execute(Agent.EXEC_POOL);

            switch (state) {
                case 0:
                    return Status.success("running");
                case 1:
                    return Status.success("dead");
                case 2:
                    return Status.success("dead");
                case 3:
                    return Status.success("stopped");
                case 4:
                    return Status.success("unknown");
                default:
                    return Status.failure(String.format("unknown status code %d", state));
            }
        }
        catch (CommandFailedException e) {
            return Status.failure(e.getMessage());
        }
    }

    public Status restart()
    {
        File control = new File(new File(deployDir, "bin"), "control");
        try {
            new Command(control.getAbsolutePath(), "restart")
                .setSuccessfulExitCodes(0)
                .setTimeLimit(10, TimeUnit.SECONDS)
                .execute(Agent.EXEC_POOL);
            return Status.success();
        }
        catch (CommandFailedException e) {
            return Status.failure(e.getMessage());
        }
        finally {
            state.invalidate(KEY);
        }
    }

    public void clear() throws IOException
    {
        stop();
        FileUtils.deleteDirectory(root);
    }

    public boolean isRunning()
    {
        return "running".equals(state.getUnchecked(KEY));
    }

    public boolean isStopped()
    {
        return "stopped".equals(state.getUnchecked(KEY));
    }

    public boolean isConfused()
    {
        return isRunning() || isStopped();
    }

    public UUID getUuid()
    {
        return uuid;
    }



}
