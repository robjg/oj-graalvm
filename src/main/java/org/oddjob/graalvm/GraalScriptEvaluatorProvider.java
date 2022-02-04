package org.oddjob.graalvm;

import org.oddjob.arooa.runtime.Evaluator;
import org.oddjob.arooa.runtime.ScriptEvaluatorProvider;

public class GraalScriptEvaluatorProvider implements ScriptEvaluatorProvider {

    @Override
    public Evaluator provideScriptEvaluator(ClassLoader classLoader) {
        return new GraalScriptEvaluator();
    }

    @Override
    public String toString() {
        return GraalScriptEvaluatorProvider.class.getName();
    }
}
