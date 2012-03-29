package org.skife.galaxy.cli;

import org.skife.cli.Cli;
import org.skife.cli.Help;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.concurrent.Callable;
import java.util.logging.Handler;
import java.util.logging.LogManager;

public class Main
{
    static {
        java.util.logging.Logger javaRootLogger = LogManager.getLogManager().getLogger("");
        for (Handler handler : javaRootLogger.getHandlers()) {
            javaRootLogger.removeHandler(handler);
        }
        javaRootLogger.addHandler(new SLF4JBridgeHandler());
    }

    public static void main(String[] args) throws Exception
    {

        Cli.CliBuilder<Callable> builder = Cli.buildCli("sculptor", Callable.class)
                                              .withDescription("A Galaxy implementation")
                                              .withCommand(Help.class)
                                              .withDefaultCommand(Help.class);

        builder.withGroup("agent")
               .withDescription("Manage a local agent")
               .withDefaultCommand(Help.class)
               .withCommand(Help.class)
               .withCommand(AgentDeploy.class)
               .withCommand(AgentStart.class)
               .withCommand(AgentStop.class)
               .withCommand(AgentStatus.class)
               .withCommand(AgentRun.class);

        builder.build().parse(args).call();
    }
}
