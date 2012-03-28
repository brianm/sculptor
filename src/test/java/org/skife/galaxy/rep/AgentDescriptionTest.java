package org.skife.galaxy.rep;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class AgentDescriptionTest
{
    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
    }

    @Test
    public void testRoundTrip() throws Exception
    {
        List<Link> links = ImmutableList.of(new Link("sound", URI.create("/woof"), "Sound!"));
        List<Action> actions = ImmutableList.of(new Action("bark",
                                                           "POST",
                                                           URI.create("/woof"),
                                                           ImmutableMap.of("volume", "How loud, as int")));
        List<SlotDescription> slots = ImmutableList.of();
        Map<String, URI> environment = ImmutableMap.of("/env/foo.conf", URI.create("http://bar/foo.conf"));
        UUID id = UUID.randomUUID();
        File root = new File("/tmp/hotdogs");
        AgentDescription ad = new AgentDescription(links,
                                                   actions,
                                                   environment,
                                                   slots,
                                                   root,
                                                   id);

        String json  =mapper.writeValueAsString(ad);
        AgentDescription ad2 = mapper.readValue(json, AgentDescription.class);
        assertThat(ad2, equalTo(ad));
    }
}
