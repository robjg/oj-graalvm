package org.oddjob.graalvm;

import com.oracle.truffle.js.builtins.PolyglotBuiltins;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.IOException;

public class SimpleGraal {

    private static final Logger logger = LoggerFactory.getLogger(SimpleGraal.class);

    public static void main(String[] args) throws IOException {

        logger.info(logger.getClass().getName());

        Context context = Context.newBuilder("js")
                .option("engine.WarnInterpreterOnly", "false")
                .allowHostAccess(HostAccess.ALL)
                //allows access to all Java classes
                .allowHostClassLookup(className -> true)
                .logHandler(new SLF4JBridgeHandler())
                .build();

        Value bindings = context.getBindings("js");
        bindings.putMember("greeting", "Hello Javascript");

        context.eval(Source.newBuilder("js", "print (greeting)", "src.js").build());

    }
}
