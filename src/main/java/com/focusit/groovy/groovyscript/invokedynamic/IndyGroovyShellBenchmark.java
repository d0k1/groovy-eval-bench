/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.focusit.groovy.groovyscript.invokedynamic;

import groovy.lang.GroovyShell;
import groovy.lang.MetaMethod;
import groovy.lang.Script;
import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import static org.codehaus.groovy.control.CompilerConfiguration.INVOKEDYNAMIC;

@State(Scope.Benchmark)
@BenchmarkMode(value = Mode.Throughput)
@Warmup(iterations = 5, time = 20, timeUnit = TimeUnit.SECONDS) //4
@Measurement(iterations = 10, time = 20, timeUnit = TimeUnit.SECONDS) //8
@Fork(4)
@Threads(8)
public class IndyGroovyShellBenchmark {
    private String simpleScriptBody;
    private GroovyShell shell;
    private CompilerConfiguration cfg;
    private Script script;
    private Script scriptBinded;
    private MetaMethod runMethod;

    @Setup
    public void init() throws IOException {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("SimpleScript.groovy")) {
            simpleScriptBody = IOUtils.toString(is, "UTF-8");
        }
        cfg = new CompilerConfiguration();
        cfg.getOptimizationOptions().put(INVOKEDYNAMIC, true);
        shell = new GroovyShell(cfg);

        script = shell.parse(simpleScriptBody);
        scriptBinded = shell.parse(simpleScriptBody);
        scriptBinded.setProperty("key", 123L);
        runMethod = scriptBinded.getMetaClass().getMetaMethod("run", null);
    }

    @Benchmark
    public void groovyNoShellNoScriptInOneCycle(Blackhole bh) {
        script.setProperty("key", 123L);
        Object result = script.run();
        bh.consume(result);
    }

    @Benchmark
    public void groovyNoShellNoScriptNoBindingInOneCycle(Blackhole bh) {
        Object result = scriptBinded.run();
        bh.consume(result);
    }

    @Benchmark
    public void groovyNoShellNoScriptNoBindingNoMethodInOneCycle(Blackhole bh) {
        Object result = runMethod.invoke(scriptBinded, null);
        bh.consume(result);
    }
}
