package org.skife.galaxy.agent;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.util.concurrent.MoreExecutors;
import com.ning.http.client.AsyncHttpClient;
import org.joda.time.Duration;
import org.skife.galaxy.ServerRoot;
import org.skife.galaxy.agent.command.CommandFailedException;
import org.skife.galaxy.agent.http.ConfigurationItem;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.google.common.util.concurrent.MoreExecutors.getExitingScheduledExecutorService;

public class Agent
{
    static {
        try {
            Class.forName("org.sqlite.JDBC");
        }
        catch (ClassNotFoundException e) {
            throw Throwables.propagate(e);
        }
    }

    public static final ExecutorService EXEC_POOL = MoreExecutors.getExitingExecutorService(new ThreadPoolExecutor(1, 100, 100, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>()));

    private final Map<UUID, Slot> slots = Maps.newConcurrentMap();
    private final File     root;
    private final Dao      dao;
    private final UUID     id;
    private final Set<URI> consoleUrls;


    public Agent(@ServerRoot File root) throws IOException
    {
        this(root, Collections.<URI>emptySet(), Duration.standardDays(1));
    }

    @Inject
    public Agent(@ServerRoot File root,
                 @Consoles Set<URI> consoles,
                 @ConsoleAnnouncement Duration announce) throws IOException
    {
        this.root = root;
        this.consoleUrls = ImmutableSet.copyOf(consoles);

        File dbFile = new File(root, "state.db");
        Files.createParentDirs(dbFile);
        dao = new DBI("jdbc:sqlite:" + dbFile.getAbsolutePath()).onDemand(Dao.class);
        dao.createEnv();

        File uuid_file = new File(root, "agent-id");
        if (uuid_file.exists()) {
            this.id = UUID.fromString(Files.readFirstLine(uuid_file, Charsets.UTF_8));
        }
        else {
            this.id = UUID.randomUUID();
            Files.write(this.id.toString(), uuid_file, Charsets.UTF_8);
        }
        for (File path : root.listFiles()) {
            if (path.isDirectory()) {
                Slot slot = Slot.from(path);
                slots.put(slot.getId(), slot);
            }
        }

        // starting these threads is okay, we do not reference *this* at all
        ScheduledExecutorService ex = getExitingScheduledExecutorService(
            new ScheduledThreadPoolExecutor(consoles.size()),
            1, TimeUnit.SECONDS);
        for (URI console : consoles) {
            ex.scheduleWithFixedDelay(new ConsoleReporter(console),
                                      0,
                                      announce.getMillis(),
                                      TimeUnit.MILLISECONDS);
        }
    }

    public Slot deploy(Deployment d) throws IOException, CommandFailedException
    {
        Map<String, URI> config = Maps.newHashMap(getEnvironmentConfig());
        config.putAll(d.getConfiguration());
        Slot s = Slot.deploy(this.root, d, config);
        this.slots.put(s.getId(), s);
        return s;
    }

    public Set<URI> getConsoleUrls()
    {
        return consoleUrls;
    }

    public UUID getId()
    {
        return id;
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
        dao.setEnvConfig(path, source.toString());
    }

    public Slot getSlot(UUID uuid)
    {
        return slots.get(uuid);
    }

    public Map<UUID, Slot> getSlots()
    {
        return ImmutableMap.copyOf(slots);
    }

    public Map<String, URI> getEnvironmentConfig()
    {
        ImmutableMap.Builder<String, URI> b = new ImmutableMap.Builder<String, URI>();
        for (ConfigurationItem item : dao.getEnvConfigs()) {
            b.put(item.getPath(), item.getUrl());
        }
        return b.build();
    }

    public static interface Dao
    {
        @SqlUpdate("create table if not exists env_config ( path varchar, url varchar, primary key (path))")
        public void createEnv();

        @SqlUpdate("insert or replace into env_config (path, url) values (:path, :url)")
        public void setEnvConfig(@Bind("path") String name, @Bind("url") String value);

        @SqlQuery("select path, url from env_config")
        @Mapper(ConfigurationItemMapper.class)
        public List<ConfigurationItem> getEnvConfigs();
    }

    public static class ConfigurationItemMapper implements ResultSetMapper<ConfigurationItem>
    {
        @Override
        public ConfigurationItem map(int i, ResultSet resultSet, StatementContext statementContext) throws SQLException
        {
            return new ConfigurationItem(resultSet.getString("path"), URI.create(resultSet.getString("url")));
        }
    }


    private static class ConsoleReporter implements Runnable
    {
        private final URI uri;

        private ConsoleReporter(URI uri)
        {
            this.uri = uri;
        }

        @Override
        public void run()
        {
            AsyncHttpClient http = new AsyncHttpClient();
            try {
                System.out.printf("pretending to report to console at %s\n", uri);
            }
            finally {
                http.close();
            }
        }
    }
}
