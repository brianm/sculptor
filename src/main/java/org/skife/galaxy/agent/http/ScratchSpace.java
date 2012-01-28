package org.skife.galaxy.agent.http;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.UUID;

public class ScratchSpace
{
    private final Map<UUID, StagedDeployment> staged = Maps.newConcurrentMap();
    public void stageDeployment(UUID id)
    {
        staged.put(id, new StagedDeployment(id));
    }

    public StagedDeployment getStagedDeployment(UUID id)
    {
        return staged.get(id);
    }
}
