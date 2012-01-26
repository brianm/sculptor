package org.skife.galaxy.cli;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import jnr.ffi.Library;
import jnr.ffi.byref.IntByReference;
import org.iq80.cli.Command;
import org.iq80.cli.Option;

import java.io.File;
import java.util.concurrent.Callable;

@Command(name = "status", description = "Retrieve agent status")
public class AgentStatus implements Callable<Void>
{

    @Option(name = {"-p", "--pidfile"}, description = "Pidfile")
    public File pidfile = new File("sculptor-agent.pid");

    @Override
    public Void call() throws Exception
    {
        if (pidfile.exists()) {
            int pid = Integer.valueOf(Files.readFirstLine(pidfile, Charsets.US_ASCII));
            LibC c = Library.loadLibrary("c", LibC.class);
            int rs = c.kill(pid, 0);
            System.out.println("status is " + rs);
            if (rs == 0) {
                // child running
                System.exit(0);
            }
            else {
                System.out.println("Stopped, but pidfile still exists");
                System.exit(1);
            }
        }
        else {
            System.out.println("Not running");
            System.exit(3);
        }

        return null;
    }
}
