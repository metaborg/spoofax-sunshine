# Spoofax Sunshine

Sunshine is a runtime library which runs Spoofax-based languages outside of Eclipse.

## Usage

Basically:

```
java -jar sunshine.jar --help
```

This will cause Sunshine to start and display a list of supported parameters.

### Automatic language discovery & configuration

Sunshine has a mechanism to automatically discover and configure languages. The example above becomes:

```
java -jar sunshine.jar \
  --auto-lang path/to/lang/ \
  --project path/to/project \
  --builder "Compile" \
  --build-on-all
```
