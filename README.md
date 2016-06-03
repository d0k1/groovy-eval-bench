# Some JHM benchmarks to get the most efficient way to call Groovy from Java #
There are some jmh benchmarks I wrote to find out which way is the best to call groovy scripts and/or groovy's template engine

The point of these benchmarks is to measure real overhead produced by various ways of running groovy inside JVM.

Each benchmark tries to run the same primitive groovy script in it's own way: 

* GroovyShellBenchmark covers standard way to call groovy from java - throught GroovyShell
* JSR223Benchmark covers unified way to call any script engine from java - jsr223 - scripting for the java platform
* TypedScript uses groovy to compile the script to *.class, and then instantiates an object and call directly it's method(s). 
* IndyGroovyShellBenchmark shows what would happened to performance if Groovy will use `INVOKEDYNAMIC` compiler option
* ScriptInheritanceBenchmark demonstrates an overhead when Groovy has special base class for script
* TemplateBenchmark shows the price for Groovy GString templating
* JavaBenchmark shows performance of the same operations written in Java

# Conditions #
## Hardware ##
CPU used to run benchmarks
```
processor	: 0
vendor_id	: GenuineIntel
cpu family	: 6
model		: 69
model name	: Intel(R) Core(TM) i5-4210U CPU @ 1.70GHz
stepping	: 1
microcode	: 0x1d
cpu MHz		: 1716.843
cache size	: 3072 KB
physical id	: 0
siblings	: 4
core id		: 0
cpu cores	: 2
apicid		: 0
initial apicid	: 0
fpu		: yes
fpu_exception	: yes
cpuid level	: 13
wp		: yes
flags		: fpu vme de pse tsc msr pae mce cx8 apic sep mtrr pge mca cmov pat pse36 clflush dts acpi mmx fxsr sse sse2 ss ht tm pbe syscall nx pdpe1gb rdtscp lm constant_tsc arch_perfmon pebs bts rep_good nopl xtopology nonstop_tsc aperfmperf eagerfpu pni pclmulqdq dtes64 monitor ds_cpl vmx est tm2 ssse3 sdbg fma cx16 xtpr pdcm pcid sse4_1 sse4_2 movbe popcnt tsc_deadline_timer aes xsave avx f16c rdrand lahf_lm abm epb tpr_shadow vnmi flexpriority ept vpid fsgsbase tsc_adjust bmi1 avx2 smep bmi2 erms invpcid xsaveopt dtherm ida arat pln pts
bugs		:
bogomips	: 4788.73
clflush size	: 64
cache_alignment	: 64
address sizes	: 39 bits physical, 48 bits virtual
power management:
```
## Environment ##
OS Ubuntu 16.04 x64 with Oracle Java
```
$ java -version
java version "1.8.0_91"
Java(TM) SE Runtime Environment (build 1.8.0_91-b14)
Java HotSpot(TM) 64-Bit Server VM (build 25.91-b14, mixed mode)
```

## JMH options ##
I used these annotations to configre JMH to run each test:
```
@BenchmarkMode(value = Mode.Throughput)
@Warmup(iterations = 4, time = 20, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 8, time = 20, timeUnit = TimeUnit.SECONDS)
@Fork(4)
@Threads(8)
```

# Results #

| Method                                                                     | Score      | Error      | Units |
|----------------------------------------------------------------------------|------------|------------|-------|
|GroovyShellBenchmark.groovyNoShellNoScriptInOneCycle                        |3380448.142 |±107825.500 | ops/s |
|GroovyCompilationBenchmark.scriptFromClassloader                            |3443216.027 |±36354.126  | ops/s |
|GroovyShellBenchmark.groovyNoShellNoScriptInOneCycle                        |4101902.557 |±105978.620 | ops/s |
|GroovyShellBenchmark.groovyNoShellNoScriptNoBindingInOneCycle               |4346689.976 |±113999.967 | ops/s |
|GroovyShellBenchmark.groovyNoShellNoScriptNoBindingNoMethodInOneCycle       |4232970.384 |±93109.872  | ops/s |
|IndyGroovyShellBenchmark.groovyNoShellNoScriptInOneCycle                    |4734100.850 |±32975.947  | ops/s |
|IndyGroovyShellBenchmark.groovyNoShellNoScriptNoBindingInOneCycle           |4821255.535 |±25239.823  | ops/s |
|IndyGroovyShellBenchmark.groovyNoShellNoScriptNoBindingNoMethodInOneCycle   |4958224.153 |±91995.790  | ops/s |
|GroovyJSR223Benchmark.groovyNoEngineInOneCycle                              |137030.242  |±3186.743   | ops/s |
|GroovyJSR223Benchmark.groovyNoEngineNoScriptInOneCycle                      |135547.553  |±641.629    | ops/s |
|GroovyJSR223Benchmark.groovyNoEngineNoScriptLessBindingInOneCycle           |213188.583  |±987.700    | ops/s |
|ScriptInheritanceBenchmark.groovyNoCompileCustomBinding                     |3269535.727 |±25125.812  | ops/s |
|StaticCompilationBenchmark.testStaticCompiledScript                         |12531283.303|±107423.687 | ops/s |
|TypedScriptBenchmark.groovyNoShellNoCompile                                 |4689558.813 |±62647.290  | ops/s |
|TypedScriptBenchmark.groovyNoShellNoCompileNoInstantiate                    |5071219.208 |±55486.532  | ops/s |
|TemplateBenchmark.templateNoCompilation                                     |1386462.321 |±9572.253   | ops/s |
|TemplateBenchmark.templateNoCompilationCustomParser                         |1594189.510 |±42076.362  | ops/s |
|TemplateBenchmark.templateNoCompilationCustomParserTyped                    |1998500.778 |±56442.280  | ops/s |
|JavaBenchmark.vanillaJava                                                   |35339538.490|±4034261.124| ops/s |
