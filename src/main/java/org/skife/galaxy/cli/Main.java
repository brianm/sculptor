package org.skife.galaxy.cli;

import org.iq80.cli.GitLikeCommandParser;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        GitLikeCommandParser.builder("sculptor")
                                     .withCommandType(SculptorCommand.class)
                                     .addCommand(AgentCommand.class)
                                     .build()
                                     .parse(args)
                                     .execute();

    }
}
