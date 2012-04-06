package org.skife.galaxy.console;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.MoreExecutors;
import com.ning.http.client.AsyncHttpClient;
import org.skife.galaxy.base.Start;
import org.skife.galaxy.rep.AgentDescription;
import org.skife.galaxy.rep.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Iterables.find;
import static org.skife.galaxy.base.MorePredicates.beanPropertyEquals;
import static org.skife.galaxy.http.JsonMappingAsyncHandler.fromJson;

public class Console
{
    private static final Logger log = LoggerFactory.getLogger(Console.class);

    private final ConcurrentMap<UUID, AgentDescription> agents    = Maps.newConcurrentMap();
    private final Set<URI>                              agentUris = new ConcurrentSkipListSet<URI>();

    private ScheduledExecutorService cron;

    public Console()
    {
        this.cron = MoreExecutors.getExitingScheduledExecutorService(new ScheduledThreadPoolExecutor(1));
    }

    public List<AgentDescription> getAgents()
    {
        return ImmutableList.copyOf(agents.values());
    }

    public void register(URI agent)
    {
        agentUris.add(agent);
    }

    @Subscribe
    public void start(Start _)
    {
        this.cron.scheduleWithFixedDelay(new ScanAgents(), 0, 10, TimeUnit.SECONDS);
    }

    public void remove(UUID id)
    {
        AgentDescription ad = agents.remove(id);
        Link self = find(ad.getLinks(), beanPropertyEquals("rel", "self"));
        agentUris.remove(self.getUri());

    }

    private class ScanAgents implements Runnable
    {
        @Override
        public void run()
        {
            final AsyncHttpClient http = new AsyncHttpClient();
            try {
                for (URI uri : agentUris) {
                    try {
                        AgentDescription ad = http.prepareGet(uri.toString())
                                                  .setHeader("Accept", MediaType.APPLICATION_JSON)
                                                  .execute(fromJson(AgentDescription.class))
                                                  .get();
                        agents.put(ad.getId(), ad);
                    }
                    catch (Exception e) {
                        log.info("Unable to retrieve agent description from " + uri, e);
                    }
                }
            }
            finally {
                http.close();
            }
        }
    }

}
