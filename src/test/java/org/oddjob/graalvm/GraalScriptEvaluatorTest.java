package org.oddjob.graalvm;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
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
import java.util.function.Function;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GraalScriptEvaluatorTest {

    @Test
    void simpleEvaluateAddingTwoVariables() throws ArooaConversionException {

        ArooaSession session = createSession();

        session.getBeanRegistry().register("a", 1);
        session.getBeanRegistry().register("b", 2);

        Evaluator test = new GraalScriptEvaluator();

        Integer result = test.evaluate("a+b", session, Integer.class);

        assertThat(result, is(3));
    }

    @Test
    void simpleEvaluatingMethods() throws ArooaConversionException {

        ArooaSession session = createSession();

        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        session.getBeanRegistry().register("list", list);

        Evaluator test = new GraalScriptEvaluator();

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

    @Test
    void simpleEvaluatingPropertiesOfBeans() throws ArooaConversionException {

        ArooaSession session = createSession();

        GraalScriptEvaluatorTest.SomeBean bean = new GraalScriptEvaluatorTest.SomeBean();
        bean.setFoo("Foo");

        session.getBeanRegistry().register("bean1", bean);
        session.getBeanRegistry().register("bean2", new SomePublicBean());

        Evaluator test = new GraalScriptEvaluator();

        // Graal doesn't support getters.
        String result1 = test.evaluate(
                "bean1.foo",
                session,
                String.class);

        assertThat(result1, nullValue());

        String result2 = test.evaluate(
                "bean2.foo",
                session,
                String.class);

        assertThat(result2, is("Foo"));
    }

    @Test
    void testVoidMethodEvaluateToNull() throws ArooaConversionException {

        ArooaSession session = createSession();

        Evaluator test = new GraalScriptEvaluator();

        Integer result = test.evaluate("print('foo')", session, Integer.class);

        assertThat(result, CoreMatchers.nullValue());
    }

    @Test
    void testEvaluateNullIsNull() throws ArooaConversionException {
        ArooaSession session = createSession();

        Evaluator test = new GraalScriptEvaluator();

        Integer result = test.evaluate(null, session, Integer.class);

        assertThat(result, CoreMatchers.nullValue());
    }

    @Test
    void testEvaluateMissingVariableThrowException() {
        ArooaSession session = createSession();

        Evaluator test = new GraalScriptEvaluator();

        try {
            test.evaluate("idontexist", session, Integer.class);
            assertThat("Should fail", false);
        } catch (ArooaConversionException e) {
            assertThat(e.getMessage(), Matchers.containsString("idontexist"));
        }
    }

    @Test
    void lambdaExpression() throws ArooaConversionException {
        ArooaSession session = createSession();

        Evaluator test = new GraalScriptEvaluator();

        @SuppressWarnings("unchecked")
        Function<Integer, Integer> add2 = test.evaluate("a => a + 2", session, Function.class);

        assertThat(add2.apply(2), is(4));
    }

    @Test
    void immediatelyInvokedFunction() throws ArooaConversionException {
        ArooaSession session = createSession();

        Evaluator test = new GraalScriptEvaluator();

        @SuppressWarnings("unchecked")
        Integer result = test.evaluate("(function(x) { return x * 2 })(3)",
                session, int.class);

        assertThat(result, is(6));
    }

    @Test
    void anonymousFunction() throws ArooaConversionException {
        ArooaSession session = createSession();

        Evaluator test = new GraalScriptEvaluator();

        @SuppressWarnings("unchecked")
        Function result = test.evaluate("f = function(x) { return x * 2; }",
                session, Function.class);

        assertThat(result.apply(3), is(6));
    }

    @Test
    void namedFunction() throws ArooaConversionException {
        ArooaSession session = createSession();

        Evaluator test = new GraalScriptEvaluator();

        @SuppressWarnings("unchecked")
        Function result = test.evaluate("function foo(x) { return x * 2; }",
                session, Function.class);

        assertThat(result, nullValue());
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