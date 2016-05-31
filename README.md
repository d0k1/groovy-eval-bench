# Some JHM benchmarks to get the right way to call Groovy from Java #
There is some jmh benchmarks I wrote to find out which way is the best to call groovy scripts and/or groovy's template engine

Each benchmark tries to run the same primitive groovy script. 

```
def a = 10.25;
def b = 2;
def x = 122;
def y = a * x + b + key;
return y;
```

The script makes some simple math operations and requires external variable - ``key``.

The point of these benchmarks is to measure real overhead produced by various ways of running groovy inside JVM.

* GroovyShellBenchmark covers standard way to call groovy from java - throught GroovyShell
* JSR223Benchmark covers unified way to call any script engine from java - jsr223 - scripting for the java platform
* TypedScript uses groovy to compile the script to *.class, and then instantiates an object and call directly it's method(s). 
That case uses special version of the script. `TypedScript.groovy` This script has a definition of a class with a method to make a calculation.
Of course method do exactly the same as an original `SimpleScript.groovy`.

# Conditions #
## Hardware ##
All of these benchmarks I ran on my laptop.
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
All of these tests I run on Ubuntu 16.04 x64

## JMH options ##
I used annotation to configre JMH to run each test. 
List of annotations I used below:
```
@BenchmarkMode(value = Mode.Throughput)
@Warmup(iterations = 4, time = 20, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 8, time = 20, timeUnit = TimeUnit.SECONDS)
@Fork(4)
@Threads(8)
```

# Results I got #
Test execution took almost 2 hours (``# Run complete. Total time: 01:52:31``)

| Method                                                                        | Score          | Error          | Units |
|-------------------------------------------------------------------------------|----------------|----------------|-------|
|GroovyShellBenchmark.groovyNoShellNoScriptInOneCycle                           |3380448.142     | ±107825.500    | ops/s |
|GroovyShellBenchmark.groovyNoShellNoScriptNoBindingInOneCycle                  |3782839.491     | ±109339.793    | ops/s |
|**GroovyShellBenchmark.groovyNoShellNoScriptNoBindingNoMethodInOneCycle**      |**3759805.032** | **±35432.064** | ops/s |
|GroovyJSR223Benchmark.groovyNoEngineInOneCycle                                 |132404.812      | ±1534.816      | ops/s |
|GroovyJSR223Benchmark.groovyNoEngineNoScriptInOneCycle                         |133206.002      | ±1647.096      | ops/s |
|**TypedScriptBenchmark.groovyNoShellNoCompile**                                |**3319700.567** | **±46150.756** | ops/s |
|TypedScriptBenchmark.groovyNoShellNoCompileNoInstantiate                       |3836564.333     | ±135134.920    | ops/s |

