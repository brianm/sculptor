package org.skife.galaxy.base;

import com.google.common.eventbus.EventBus;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public class GuiceEventBusRegistrar implements TypeListener
{
    private final EventBus events;

    public GuiceEventBusRegistrar(EventBus events)
    {
        this.events = events;
    }

    @Override
    public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter)
    {
        encounter.register(new InjectionListener<I>()
        {
            @Override
            public void afterInjection(I injectee)
            {
                events.register(injectee);
            }
        });
    }
}
