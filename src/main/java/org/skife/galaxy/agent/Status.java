package org.skife.galaxy.agent;

import com.google.common.base.Objects;

public final class Status
{
    private final String msg;
    private final boolean success;

    private Status(String msg, boolean success) {
        this.msg = msg;
        this.success = success;
    }

    public String getMessage()
    {
        return msg;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public static Status success()
    {
        return new Status("", true);
    }

    public static Status failure(String message)
    {
        return new Status(message, false);
    }

    public static Status success(String message)
    {
        return new Status(message, true);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Status status = (Status) o;

        return success == status.success && msg.equals(status.msg);

    }

    @Override
    public int hashCode()
    {
        int result = msg.hashCode();
        result = 31 * result + (success ? 1 : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this).add("success", success).add("message", msg).toString();
    }
}
