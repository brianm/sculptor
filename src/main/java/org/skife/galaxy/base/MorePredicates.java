package org.skife.galaxy.base;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;

import javax.annotation.Nullable;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

public class MorePredicates
{
    public static <T> Predicate<T> beanPropertyEquals(final String propertyName, final Object value)
    {
        return new Predicate<T>()
        {
            @Override
            public boolean apply(@Nullable T input)
            {
                if (input == null) return false;

                try {
                    for (PropertyDescriptor pd : Introspector.getBeanInfo(input.getClass())
                                                             .getPropertyDescriptors())
                    {
                        if (propertyName.equals(pd.getName())) {
                            return Objects.equal(value, pd.getReadMethod().invoke(input));
                        }
                    }
                    return false;
                }
                catch (Exception e) {
                    throw Throwables.propagate(e);
                }
            }
        };
    }
}
