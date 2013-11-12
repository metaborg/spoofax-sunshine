# Spoofax Sunshine

Sunshine is a runtime library which runs Spoofax-based languages  outside of Eclipse.

## Limitations
Many. This is most likely an incomplete list:

* No support for concrete object syntax embedding
* Completely single-threaded
* Not all Spoofax primitives are implemented
* Only supports multi-file analysis. Classic `editor-analyse` is not supported.

## Usage
Basically:

    java -cp sunshine.jar Main --help

This will cause Sunshine to start and display a list of supported parameters.

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

### Hack for parsing
If you want to also get parse errors you need to change all calls to `parse-file` to something else which calls the Sunshine `parse-file` primitive, for example:

    sunshine-parse-file = prim("SSL_EXT_parse_file")

This primitive can parse files in any language that is registered. If parsing produces messages these are reported to console.

### Dependencies
The following are required in *lib* to compile and run Sunshine:

* Apache Commons IO: *commons-io-2.4.jar*
* Google Guava: *guava-14.0.jar*
* SDF2IMP: *sdf2imp.jar*
* Stratego/XT: *strategoxt.jar*
* Spoofax libs: *spoofax-libs.jar*
* JCommander: *jcommander-1.30.jar*
* Apache Log4j 2: *log4j-core-2.0-beta8.jar*, *log4j-api-2.0-beta8.jar*
