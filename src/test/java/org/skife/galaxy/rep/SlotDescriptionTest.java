package org.skife.galaxy.rep;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class SlotDescriptionTest
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
        UUID id = UUID.randomUUID();

        File dir = new File("/tmp/sandwich");
        SlotDescription sd = new SlotDescription(links, actions, "running", id, dir, "hello world");

        String json  =mapper.writeValueAsString(sd);
        SlotDescription sd2 = mapper.readValue(json, SlotDescription.class);
        assertThat(sd2, equalTo(sd));
    }

}
