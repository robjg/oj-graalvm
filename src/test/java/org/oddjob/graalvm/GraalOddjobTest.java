package org.oddjob.graalvm;

import org.junit.jupiter.api.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.xml.XMLConfiguration;

import java.util.function.Function;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class GraalOddjobTest {

    @Test
    void evaluateInOddjobAsVariable() throws ArooaConversionException {

        String xml = "<oddjob>" +
                " <job>" +
                "  <variables id='vars'>" +
                "   <func>" +
                "    <value value='#{a => a + 2}'/>" +
                "   </func>" +
                "  </variables>" +
                " </job>" +
                "</oddjob>";

        Oddjob oj = new Oddjob();
        oj.setConfiguration(new XMLConfiguration("TEST", xml));
        oj.run();

        @SuppressWarnings("unchecked")
        Function<Integer, Integer> add2 = new OddjobLookup(oj).lookup("vars.func", Function.class);

        assertThat(add2.apply(2), is(4));
    }

    public static class NumFuncJob implements Runnable {

        private Function<Integer, Integer> func;

        private int in;

        private int result;

        @Override
        public void run() {

            result = func.apply(in);
        }

        public Function<Integer, Integer> getFunc() {
            return func;
        }

        @ArooaAttribute
        public void setFunc(Function<Integer, Integer> func) {
            this.func = func;
        }

        public int getIn() {
            return in;
        }

        public void setIn(int in) {
            this.in = in;
        }

        public int getResult() {
            return result;
        }
    }

    @Test
    void evaluateInOddjobAsJobAttribute() throws ArooaConversionException {

        String xml = "<oddjob>" +
                " <job>" +
                "  <bean class='" + NumFuncJob.class.getName() + "' id='job' in='2' func='#{a => a + 2}'/>" +
                " </job>" +
                "</oddjob>";

        Oddjob oj = new Oddjob();
        oj.setConfiguration(new XMLConfiguration("TEST", xml));
        oj.run();

        int add2 = new OddjobLookup(oj).lookup("job.result", int.class);

        assertThat(add2, is(4));
    }
}
