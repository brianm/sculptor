package org.skife.galaxy.cli;

import org.skife.cli.Cli;
import org.skife.cli.Help;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.logging.Handler;
import java.util.logging.LogManager;

import static org.skife.cli.config.PropertiesConfiguration.fromProperties;

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
                                              .withConfiguration(fromProperties(new File("sculptor.conf")))
                                              .withDescription("A Galaxy implementation")
                                              .withCommand(Help.class)
                                              .withDefaultCommand(Help.class);

        builder.withGroup("agent")
               .withDescription("Manage an agent")
               .withDefaultCommand(Help.class)
               .withCommand(Help.class)
               .withCommand(AgentDeploy.class)
               .withCommand(AgentStart.class)
               .withCommand(AgentStop.class)
               .withCommand(AgentStatus.class)
               .withCommand(AgentList.class)
               .withCommand(AgentRun.class);

        builder.withGroup("slot")
               .withDescription("Manage slots on an agent")
               .withDefaultCommand(Help.class)
               .withCommand(Help.class)
               .withCommand(SlotStart.class)
               .withCommand(SlotStop.class);

        builder.withGroup("console")
            .withDescription("Manage a console")
            .withDefaultCommand(Help.class)
            .withCommand(Help.class)
            .withCommand(ConsoleStart.class)
            .withCommand(ConsoleStop.class)
            .withCommand(ConsoleRun.class);

        builder.build().parse(args).call();
    }


}
