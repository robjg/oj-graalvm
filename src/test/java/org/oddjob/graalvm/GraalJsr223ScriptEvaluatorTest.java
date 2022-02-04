package org.oddjob.graalvm;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaTools;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.convert.DefaultConverter;
import org.oddjob.arooa.registry.BeanRegistry;
import org.oddjob.arooa.registry.SimpleBeanRegistry;
import org.oddjob.arooa.runtime.Evaluator;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GraalJsr223ScriptEvaluatorTest {


    @Test
    void simpleEvaluateAddingTwoVariables() throws ArooaConversionException {

        ArooaSession session = createSession();

        session.getBeanRegistry().register("a", 1);
        session.getBeanRegistry().register("b", 2);

        Evaluator test = new GraalJsr223ScriptEvaluator();

        Integer result = test.evaluate("a+b", session, Integer.class);

        assertThat(result, is(3));
    }

    @Disabled("This doesn't work but the Native Graal one does - no idea why.")
    @Test
    void simpleEvaluatingMethods() throws ArooaConversionException {

        ArooaSession session = createSession();

        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        session.getBeanRegistry().register("list", list);

        Evaluator test = new GraalJsr223ScriptEvaluator();

        @SuppressWarnings("unchecked")
        Collection<Integer> result = test.evaluate(
                "list.stream().limit(3).collect(java.util.stream.Collectors.toList())",
                session,
                Collection.class);

        assertThat(result, is(Arrays.asList(1, 2, 3)));
    }

    public static class SomeBean {

        private String foo;

        public String getFoo() {
            return foo;
        }

        public void setFoo(String foo) {
            this.foo = foo;
        }
    }

    public static class SomePublicBean {

        public String foo = "Foo";
    }

    @Disabled("This doesn't work but the Native Graal one does - no idea why.")
    @Test
    public void simpleEvaluatingPropertiesOfBeans() throws ArooaConversionException {

        ArooaSession session = createSession();

        SomeBean bean = new SomeBean();
        bean.setFoo("Foo");

        session.getBeanRegistry().register("bean1", bean);
        session.getBeanRegistry().register("bean2", new GraalScriptEvaluatorTest.SomePublicBean());

        Evaluator test = new GraalJsr223ScriptEvaluator();

        String result1 = test.evaluate(
                "bean1.foo",
                session,
                String.class);

        assertThat(result1, Matchers.nullValue());

        String result2 = test.evaluate(
                "bean2.foo",
                session,
                String.class);

        assertThat(result2, is("Foo"));
    }

    @Test
    public void testVoidMethodEvaluateToNull() throws ArooaConversionException {

        ArooaSession session = createSession();

        Evaluator test = new GraalJsr223ScriptEvaluator();

        Integer result = test.evaluate("print('foo')", session, Integer.class);

        assertThat(result, nullValue());
    }

    @Test
    public void testEvaluateNullIsNull() throws ArooaConversionException {
        ArooaSession session = createSession();

        Evaluator test = new GraalJsr223ScriptEvaluator();

        Integer result = test.evaluate(null, session, Integer.class);

        assertThat(result, nullValue());
    }

    @Test
    public void testEvaluateMissingVariableThrowException() {
        ArooaSession session = createSession();

        Evaluator test = new GraalJsr223ScriptEvaluator();

        try {
            test.evaluate("idontexist", session, Integer.class);
            assertThat("Should fail", false);
        } catch (ArooaConversionException e) {
            assertThat(e.getMessage(), e.getMessage().contains("idontexist"), is(true));
        }
    }

    private ArooaSession createSession() {

        BeanRegistry beanRegistry = new SimpleBeanRegistry();

        ArooaTools tools = mock(ArooaTools.class);
        when(tools.getArooaConverter()).thenReturn(new DefaultConverter());

        ArooaSession session = mock(ArooaSession.class);
        when(session.getTools()).thenReturn(tools);
        when(session.getBeanRegistry()).thenReturn(beanRegistry);

        return session;
    }
}