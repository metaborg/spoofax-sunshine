# Spoofax Sunshine

Sunshine is a runtime library which runs Spoofax-based languages  outside of Eclipse.

## Limitations
Many. This is most likely an incomplete list:

* Only one language at a time
* No support for concrete object syntax embedding
* Only analysis is supported. No compilation, no builders.
* Completely single-threaded
* Only supports languages built to Jar, i.e. CTrees not yet supported
* Not all Spoofax primitives are implemented
* Only supports multi-file analysis. Classic `editor-analyse` is not supported.

## Usage
Basically:

    java -cp sunshine.jar Sunshine LANGUAGE_OPTS PROJECT FILES

### Parameters
Below, bold parameters are compulsory.

#### LANGUAGE_OPTS
All of the following are compulsory:

* **--lang-jar** (relative) paths to Jar files shipped with the language (typically LANG-java.jar & LANG.jar)
* **--lang-tbl** (relative) path to the parse table for the language (typically LANG.tbl)

#### PROJ_DIR
* **--proj-dir** The (relative) base path for all files to be analysed. This can be the root of an Eclipse project or just a normal directory.

#### FILES
* **--targets** A (space separated) list of paths to files to be analysed. The paths must be relative to the PROJ_DIR.

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
