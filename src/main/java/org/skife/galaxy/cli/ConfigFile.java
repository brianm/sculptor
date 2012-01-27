package org.skife.galaxy.cli;

import com.google.common.io.Files;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigFile
{
    private final Properties props = new Properties();

    public ConfigFile(File file) throws IOException
    {
        if (file.exists()) {
            FileInputStream fin = null;
            try {
                fin = new FileInputStream(file);
                props.load(fin);
            }
            finally {
                if (fin != null) {
                    fin.close();
                }
            }
        }
    }


    public File fallbackFrom(File value, String name) throws IOException
    {
        if (value != null) {
            return value;
        }
        if (props.containsKey(name)) {
            return new File(props.getProperty(name));
        }
        else {
            return null;
        }

    }
}
