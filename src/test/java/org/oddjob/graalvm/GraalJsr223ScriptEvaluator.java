package org.oddjob.graalvm;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.runtime.Evaluator;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Objects;

/**
 * Evaluate an expression as a script using the Graalvm in Jsr223 form.
 * <p/>
 */
public class GraalJsr223ScriptEvaluator implements Evaluator {

    public static final String SCRIPT_LANGUAGE = "graal.js";

    private final ScriptEngine engine;

    public GraalJsr223ScriptEvaluator() {

        ScriptEngineManager manager = new ScriptEngineManager(
                Thread.currentThread().getContextClassLoader());

        this.engine = Objects.requireNonNull(manager.getEngineByName(SCRIPT_LANGUAGE),
                "No Script Engine for " + SCRIPT_LANGUAGE);
    }

    @Override
    public <T> T evaluate(String propertyExpression, ArooaSession session, Class<T> type) throws ArooaPropertyException, ArooaConversionException {

        if (propertyExpression == null) {
            return null;
        }

        Object result;
        try {
            ScriptContext scriptContext = engine.getContext();
            scriptContext.setBindings(GraalSessionBindings.from(session.getBeanRegistry()),
                    ScriptContext.GLOBAL_SCOPE);
            result = engine.eval(propertyExpression,
                    scriptContext);
        } catch (ScriptException e) {
            throw new ArooaConversionException(e);
        }
        return session.getTools().getArooaConverter().convert(result, type);
    }

}
