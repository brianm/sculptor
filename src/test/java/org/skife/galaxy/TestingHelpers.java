package org.skife.galaxy;

import com.google.common.base.Predicate;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

import javax.annotation.Nullable;
import java.io.File;
import java.util.regex.Pattern;

public class TestingHelpers
{
    public static <T> void assertIsh(T actual, Matcher<?> matcher)
    {
        if (!matcher.matches(actual)) {
            Description description = new StringDescription();
            description.appendText("\nExpected: ");
            description.appendDescriptionOf(matcher);
            description.appendText("\n     got: ");
            description.appendValue(actual);
            description.appendText("\n");
            throw new java.lang.AssertionError(description.toString());
        }
    }

    public static Matcher<Integer> isHttpSuccess()
    {
        return new BaseMatcher<Integer>()
        {
            @Override
            public boolean matches(Object item)
            {
                Integer val = (Integer) item;
                return val >= 200 && val < 300;
            }

            @Override
            public void describeTo(Description d)
            {
                d.appendText("a value between 200 and 299");
            }
        };
    }


    public static <T> Predicate<T> fieldEquals(final String name, final Object value)
    {
        return new Predicate<T>()
        {
            @Override
            public boolean apply(@Nullable T input)
            {
                if (input == null) return false;

                Object cand;
                try {
                    cand = input.getClass().getField(name).get(input);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                return value.equals(cand);
            }
        };
    }

    public static File file(File root, String... children)
    {
        File rs = root;
        for (String child : children) {
            rs = new File(rs, child);
        }
        return rs;
    }

    public static Matcher<String> matches(final String pattern) {
        return new BaseMatcher<String>()
        {
            @Override
            public boolean matches(Object item)
            {
                return Pattern.compile(pattern).matcher(String.valueOf(item)).find();
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("a string matching the regex /" + pattern + "/");
            }
        };
    }

    public static Matcher<File> exists()
    {
        return new BaseMatcher<File>()
        {
            @Override
            public boolean matches(Object item)
            {
                if (! (item instanceof File)) { return false; }
                File f = (File) item;
                return f.exists();
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("File does not seem to exist");
            }
        };
    }


}
