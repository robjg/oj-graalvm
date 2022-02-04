package org.oddjob.graalvm;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.runtime.Evaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class GraalScriptEvaluator implements Evaluator {

    private static final Logger logger = LoggerFactory.getLogger(GraalScriptEvaluator.class);

    @Override
    public <T> T evaluate(String propertyExpression, ArooaSession session, Class<T> type) throws ArooaPropertyException, ArooaConversionException {

        logger.debug("Evaluating [{}] as type {} ", propertyExpression, type.getName());

        if (propertyExpression == null) {
            return null;
        }

        Context context = Context.newBuilder("js")
                .option("engine.WarnInterpreterOnly", "false")
                .allowAllAccess(true)
                .logHandler(new SLF4JBridgeHandler())
                .build();

        Value bindings = context.getBindings("js");

        BeanDirectory beanDirectory = session.getBeanRegistry();

        for (String name : beanDirectory.getAllIds()) {
            Object value = beanDirectory.lookup(name);
            if (value == null) {
                continue;
            }
            bindings.putMember(name, value);
        }

        try {
            Value result = context.eval("js", propertyExpression);

            if (ArooaValue.class.isAssignableFrom(type)) {
                return session.getTools().getArooaConverter().convert(new GraalScriptValue(result), type);
            }
            else {
                return result.as(type);
            }
        }
        catch (RuntimeException e) {
            throw new ArooaConversionException("Failed evaluating [" + propertyExpression + "]", e);
        }
    }
}
