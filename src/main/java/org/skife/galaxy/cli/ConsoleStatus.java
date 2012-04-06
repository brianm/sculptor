package org.skife.galaxy.cli;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import jnr.ffi.Library;
import org.skife.cli.Command;
import org.skife.cli.Option;
import org.skife.cli.OptionType;

import java.io.File;
import java.util.concurrent.Callable;

@Command(name = "status", description = "Retrieve agent status")
public class ConsoleStatus implements Callable<Void>
{
    @Option(name = {"-p", "--pidfile"},
            title = "pidfile",
            description = "path to pidfile", configuration = "console.pidfile")
    public File pidfile = new File("sculptor-agent.pid");

    @Override
    public Void call() throws Exception
    {
        if (pidfile.exists()) {
            int pid = Integer.valueOf(Files.readFirstLine(pidfile, Charsets.US_ASCII));
            LibC c = Library.loadLibrary("c", LibC.class);
            int rs = c.kill(pid, 0);
            if (rs == 0) {
                // child running
                System.out.printf("running\t%d\n", pid);
                System.exit(0);
            }
            else {
                System.out.printf("Stopped, but pidfile %s still exists\n", pidfile.getAbsolutePath());
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
