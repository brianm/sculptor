package org.skife.galaxy.console;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.skife.galaxy.rep.AgentDescription;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

public class Console
{
    private final ConcurrentMap<UUID, AgentDescription> agents = Maps.newConcurrentMap();

    public List<AgentDescription> getAgents()
    {
        return ImmutableList.copyOf(agents.values());
    }

    public void register(AgentDescription agent)
    {
        this.agents.put(agent.getId(), agent);
    }

    public void checkinAgent(UUID id)
    {
        // noop for now
    }
}
