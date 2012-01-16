package org.skife.galaxy.cli;

import org.iq80.cli.GitLikeCommandParser;
import org.slf4j.bridge.SLF4JBridgeHandler;

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
        GitLikeCommandParser.builder("sculptor")
                                     .withCommandType(SculptorCommand.class)
                                     .addCommand(AgentCommand.class)
                                     .addCommand(DeployCommand.class)
                                     .build()
                                     .parse(args)
                                     .execute();

    }
}
