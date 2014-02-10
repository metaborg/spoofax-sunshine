# Spoofax Sunshine

Sunshine is a runtime library which runs Spoofax-based languages  outside of Eclipse.

## Limitations
Many. This is most likely an incomplete list:

* Completely single-threaded
* Not all Spoofax primitives are implemented
* Only supports multi-file analysis. Classic `editor-analyse` is not supported.

## Usage
Basically:

    java -cp sunshine.jar Main --help

This will cause Sunshine to start and display a list of supported parameters.

### Manual language parameterisation

A basic example of command line parameters is given below, which causes Sunshine to analyse all files in the project directory incrementally and run the *webdsl-metrics* builder on *yellowgrass.app*.

    java -cp sunshine.jar Main
    --lang WebDSL
    --ctree ../../webdsl2/include/webdsl.ctree
    --jar ../../webdsl2/include/webdsl.jar
    --jar ../../webdsl2/include/webdsl-java.jar
    --jar ../../webdsl2/lib/task.jar
    --table ../../webdsl2/include/WebDSL.tbl
    --ssymb Unit
    --ext app
    
    --project ../../yellowgrass/
    --builder webdsl-metrics
    --build-on yellowgrass.app

### Automatic language discovery & configuration

Sunshine has a mechanism to automatically discover and configure languages. The example above becomes:

    java -cp sunshine.jar Main
    --auto-lang ../../webdsl2/include
    --project ../../yellowgrass/
    --builder "Compute Metrics"
    --build-on yellowgrass.app

The <code>--auto-lang</code> instructs Sunshine to recursively look for <code>LANG-packed.esv</code> files and load the corresponding languages.

There are three pitfalls:

1. Sunshine will assume that observer function (analysis strategy) to be used for CLI is called `analysis-default-cmd`.
2. The builder invocation is done by the name defined in the `LANG-Menu.esv`. The argument to the `--builder` flag must match the name of the declared action:

    action: "Compute Metrics" = webdsl-metrics ... 
3. If the Spoofax project does not contain any menu declarations (e.g. it is old and still uses the `LANG-Builders.esv` file) then *no* builders will be discovered or registered.

### Hack for parsing
If you want to also get parse errors you need to change all calls to `parse-file` to something else which calls the Sunshine `parse-file` primitive, for example:

    sunshine-parse-file = prim("SSL_EXT_parse_file")

This primitive can parse files in any language that is registered. If parsing produces messages these are reported to console.

### Dependencies
The following are required in *lib* to compile and run Sunshine:

* Apache Commons IO: *commons-io-2.4.jar*
* SDF2IMP: *sdf2imp.jar*
* Stratego/XT: *strategoxt.jar*
* Spoofax libs: *spoofax-libs.jar*
* JCommander: *jcommander-1.30.jar*
* Apache Log4j 2: *log4j-core-2.0-beta9.jar*, *log4j-api-2.0-beta9.jar*
* JUnit 4: *junit-4.11.jar*
* Hamcrest Core: *hamcrest-core-1.3.jar*

