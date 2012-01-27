package org.skife.galaxy.agent;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import org.apache.commons.io.FileUtils;
import org.skife.galaxy.agent.command.Command;
import org.skife.galaxy.agent.command.CommandFailedException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Slot
{
    private final URI    bundleUrl;
    private final String name;
    private final File   root;
    private final File   deployDir;
    private final UUID   id;

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

    public Slot(UUID uuid,
                URI bundle,
                String name,
                File path) throws IOException
    {
        this.id = uuid;
        this.bundleUrl = bundle;
        this.name = name;
        this.root = path;
        this.deployDir = new File(path, "deploy");
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

    public static Slot deploy(File root, Deployment d, Map<String, URI> envFiles) throws IOException
    {
        UUID uuid = UUID.randomUUID();
        File deployment_dir = new File(root, uuid.toString());
        Preconditions.checkState(deployment_dir.mkdirs(), "Unable to create deployment directory");
        File tmp_tarball = File.createTempFile("pleides", ".tar.gz");

        final URL url = d.getTarballUrl().toURL();
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
        int out = c.execute(Agent.EXEC_POOL);

        Preconditions.checkState(out == 0);

        Preconditions.checkState(tmp.listFiles().length == 1, "Too many directories in root of expanded tarball");
        File dir = tmp.listFiles()[0];
        Files.write(d.getName(), new File(deployment_dir, "name"), Charsets.UTF_8);
        Files.write(uuid.toString(), new File(deployment_dir, "slot_id"), Charsets.UTF_8);
        Files.write(d.getTarballUrl().toString(), new File(deployment_dir, "bundle_url"), Charsets.UTF_8);

        for (final Map.Entry<String, URI> entry : envFiles.entrySet()) {
            File target = new File(dir, entry.getKey());
            if (!target.getParentFile().exists()) {
                Preconditions.checkArgument(target.getParentFile().mkdirs(),
                                            "Unable to create directory for config file %s", target);
            }
            Files.copy(new InputSupplier<InputStream>()
            {
                @Override
                public InputStream getInput() throws IOException
                {
                    return entry.getValue().toURL().openStream();
                }
            }, target);
        }

        Files.move(dir, new File(deployment_dir, "deploy"));

        return new Slot(uuid, d.getTarballUrl(), d.getName(), deployment_dir);

    }

    public static Slot from(File root) throws IOException
    {
        String name = Files.readFirstLine(new File(root, "name"), Charsets.UTF_8);
        String uuid_s = Files.readFirstLine(new File(root, "slot_id"), Charsets.UTF_8);
        String bundle_source = Files.readFirstLine(new File(root, "bundle_url"), Charsets.UTF_8);
        return new Slot(UUID.fromString(uuid_s),
                        URI.create(bundle_source),
                        name,
                        root);
    }

    public URI getBundleUrl()
    {
        return bundleUrl;
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

    public Status updateConfig(Map<String, URI> envFiles) throws IOException
    {
        for (final Map.Entry<String, URI> entry : envFiles.entrySet()) {
            File target = new File(deployDir, entry.getKey());
            if (!target.getParentFile().exists()) {
                Preconditions.checkArgument(target.getParentFile().mkdirs(),
                                            "Unable to create directory for config file %s", target);
            }
            Files.copy(new InputSupplier<InputStream>()
            {
                @Override
                public InputStream getInput() throws IOException
                {
                    return entry.getValue().toURL().openStream();
                }
            }, target);
        }
        return Status.success();
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
        return !isRunning() && !isStopped();
    }

    public UUID getId()
    {
        return id;
    }
}
