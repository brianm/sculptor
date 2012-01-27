package org.skife.galaxy.cli;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import jnr.ffi.Library;
import org.iq80.cli.Command;
import org.iq80.cli.Option;
import org.iq80.cli.OptionType;

import java.io.File;
import java.util.concurrent.Callable;

@Command(name = "stop", description = "Stop running agent")
public class AgentStop implements Callable<Void>
{

    @Option(name = {"-p", "--pidfile"}, description = "Pidfile")
    public File pidfile = new File("sculptor-agent.pid");

    @Option(name={"-c", "--config"}, description = "Configuration file", type = OptionType.GLOBAL)
    public File config = new File("/etc/sculptor/agent.conf");

    @Override
    public Void call() throws Exception
    {
        ConfigFile cf = new ConfigFile(config);
        pidfile = cf.fallbackFrom(pidfile, "pidfile");

        if (pidfile.exists()) {
            int pid = Integer.valueOf(Files.readFirstLine(pidfile, Charsets.US_ASCII));
            LibC c = Library.loadLibrary("c", LibC.class);
            int rs = c.kill(pid, 15);
            if (rs != 0) {
                System.out.println(c.strerror(rs));
            }
            pidfile.delete();
        }

        return null;
    }


}
