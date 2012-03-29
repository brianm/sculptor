package org.skife.galaxy.cli;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import jnr.ffi.Library;
import org.skife.cli.Command;
import org.skife.cli.Option;

import java.io.File;
import java.util.concurrent.Callable;

@Command(name = "stop", description = "Stop running agent")
public class AgentStop implements Callable<Void>
{
    @Option(name = {"-p", "--pidfile"},
            title = "pidfile",
            description = "path to pidfile",
            configuration = "agent.pidfile")
    public File pidfile = new File("sculptor-agent.pid");

    @Override
    public Void call() throws Exception
    {
        if (pidfile.exists()) {
            int pid = Integer.valueOf(Files.readFirstLine(pidfile, Charsets.US_ASCII));
            LibC c = Library.loadLibrary("c", LibC.class);
            int rs = c.kill(pid, 15);
            if (rs != 0) {
                System.out.println(c.strerror(rs));
            }
            Preconditions.checkState(pidfile.delete(), "Unable to delete pidfile");
        }

        return null;
    }


}
