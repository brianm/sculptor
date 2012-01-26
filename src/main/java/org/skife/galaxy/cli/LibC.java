package org.skife.galaxy.cli;

public interface LibC
{
    int kill(int pid, int signal);
    String strerror(int errno);
}
