package org.skife.galaxy.base;

import com.ning.http.client.Request;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;

public class JsonEntityWriter implements Request.EntityWriter
{
    private static final ObjectMapper mapper = new ObjectMapper();

    private Object entity;

    public JsonEntityWriter(Object entity) {
        this.entity = entity;
    }

    public static JsonEntityWriter jsonWriter(Object entity)
    {
        return new JsonEntityWriter(entity);
    }

    @Override
    public void writeEntity(OutputStream out) throws IOException
    {
        mapper.writeValue(out, entity);
    }
}
