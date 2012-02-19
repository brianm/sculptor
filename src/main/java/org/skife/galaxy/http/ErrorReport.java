package org.skife.galaxy.http;

public class ErrorReport
{

    private final String error;

    public ErrorReport(String msg) {
        this.error = msg;
    }

    public String getError()
    {
        return error;
    }
}
