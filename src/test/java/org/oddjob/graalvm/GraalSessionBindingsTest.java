package org.oddjob.graalvm;

import org.junit.jupiter.api.Test;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.registry.BeanRegistry;
import org.oddjob.arooa.registry.SimpleBeanRegistry;

import javax.script.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class GraalSessionBindingsTest {

    @Test
    public void testEngineAssumptions() throws ScriptException {

        ScriptEngineManager manager = new ScriptEngineManager();

        ScriptEngine engine = manager.getEngineByName(GraalJsr223ScriptEvaluator.SCRIPT_LANGUAGE);

        Bindings bindings = engine.createBindings();

        SimpleScriptContext context = new SimpleScriptContext();
        context.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

        bindings.put("b", 2);
        bindings.put("c", 3);

        Number result = (Number) engine.eval("a = b + c", context);

        assertThat(result.intValue(), is(5));

        Number a = (Number) bindings.get("a");

        assertThat(a.intValue(), is(5));
    }

    @Test
    void registryValuesAreUsedAllScopes() throws ScriptException, ArooaConversionException {

        ScriptEngineManager manager = new ScriptEngineManager();

        ScriptEngine engine = manager.getEngineByName(GraalJsr223ScriptEvaluator.SCRIPT_LANGUAGE);

        BeanRegistry registry = new SimpleBeanRegistry();
        registry.register("b", 2);
        registry.register("c", 3);

        Bindings bindings = GraalSessionBindings.from(registry);

        ScriptContext context = engine.getContext();
        context.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        context.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);

        Number result = (Number) engine.eval("a = b + c", context);

        assertThat(result, is(5));

        Number aFromRegistry = registry.lookup("a", Number.class);

        // Graal doesn't set the value back in the binding we give it, whatever the scope.

        assertThat(aFromRegistry, nullValue());

        Bindings engineGlobalBinding = engine.getBindings(ScriptContext.GLOBAL_SCOPE);
        assertThat(engineGlobalBinding, sameInstance(bindings));

        Bindings engineLocalBinding = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        assertThat(engineLocalBinding, sameInstance(bindings));

        // And it HASN'T put the value back in the bindings.

        assertThat(engineGlobalBinding.get("a"), nullValue());
        assertThat(engineLocalBinding.get("a"), nullValue());
    }

    @Test
    void registryValuesAreUsedInGlobalScope() throws ScriptException, ArooaConversionException {

        ScriptEngineManager manager = new ScriptEngineManager();

        ScriptEngine engine = manager.getEngineByName(GraalJsr223ScriptEvaluator.SCRIPT_LANGUAGE);

        BeanRegistry registry = new SimpleBeanRegistry();
        registry.register("b", 2);
        registry.register("c", 3);

        Bindings bindings = GraalSessionBindings.from(registry);

        ScriptContext context = engine.getContext();
        context.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);

        Number result = (Number) engine.eval("a = b + c", context);

        assertThat(result, is(5));

        Number aFromRegistry = registry.lookup("a", Number.class);

        // Graal doesn't set the value back in the binding we give it, whatever the scope.

        assertThat(aFromRegistry, nullValue());

        Bindings engineGlobalBinding = engine.getBindings(ScriptContext.GLOBAL_SCOPE);
        assertThat(engineGlobalBinding, sameInstance(bindings));

        Bindings engineLocalBinding = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        assertThat(engineLocalBinding, not(sameInstance(bindings)));

        // And it has put the value back in the bindings.

        Number aFromBindings = (Number) engineLocalBinding.get("a");
        assertThat(aFromBindings.intValue(), is(5));
    }

}