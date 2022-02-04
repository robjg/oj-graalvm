package org.oddjob.graalvm;

import org.graalvm.polyglot.Value;
import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.convert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraalScriptValue implements ArooaValue {

    private static final Logger logger = LoggerFactory.getLogger(GraalScriptValue.class);

    private final Value value;

    public static class Conversion implements ConversionProvider {

        @Override
        public void registerWith(ConversionRegistry registry) {

            registry.registerJoker(GraalScriptValue.class, new Joker<GraalScriptValue>() {
                @Override
                public <T> ConversionStep<GraalScriptValue, T> lastStep(Class<? extends GraalScriptValue> from,
                                                                        Class<T> to,
                                                                        ConversionLookup conversions) {
                    return new ConversionStep<GraalScriptValue, T>() {
                        @Override
                        public Class<GraalScriptValue> getFromClass() {
                            return GraalScriptValue.class;
                        }

                        @Override
                        public Class<T> getToClass() {
                            return to;
                        }

                        @Override
                        public T convert(GraalScriptValue from, ArooaConverter converter) {
                            logger.debug("Providing Graal Value [{}] as type {} ", from.value, to.getName());
                            return from.value.as(to);
                        }
                    };
                }
            });
        }
    }


    public GraalScriptValue(Value value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "GraalScriptValue{" +
                "value=[" + value +
                "]}";
    }
}
