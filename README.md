# module-resource-dirs

Testbed for reading resources in Java 9 module projects, as complained about in
[this StackOverflow question](https://stackoverflow.com/questions/51864473/how-can-i-get-the-list-of-files-in-a-resource-directory-under-java-9?noredirect=1#comment90682441_51864473).

Usage:

1. in the console, `./gradlew run`, or
2. in the console,

   ```
   ./gradlew installDist && \
     build/install/module-resource-dirs/bin/module-resource-dirs
   ```
   
   (effectively `java -classpath <jar> <main-class>`), or
   
3. in an IDE, build and run `org.dmoles.LoadExperiment.main(String[])`

The `LoadExperiment` class uses several methods to try to read (1) a resource file, and (2) the directory containing that file.

## Via class path: resources loaded; resource directories only readable from file system

By default, `./gradlew run`, operating (AFAICT) against the build output directory, is able to read both
the resource file and the list of files in the directory.

(Note though that the behavior is subtly different depending on whether the resource path is relative or
absolute. `Class.getResourceAsStream()` requires a leading slash, while `ClassLoader.getResourceAsStream()`
requires that there **not** be a leading slash. `Module.getResourceAsStream()` seems to work either way.)

`./gradlew installDist` etc., operating against
a JAR, is able to read the resource file, but is not able to read the list of files in the directory
(though it does read the directory itself, i.e., it gets an empty `InputStream` rather than `null`.)

### `./gradlew run` 

#### `ClassLoader.getSystemClassLoader()::getResourceAsStream`

| path | loaded | details |
| --- | --- | --- |
| mydir/myresource.txt | loaded | |
| /mydir/myresource.txt | not loaded | |
| mydir | loaded | 1 files found: myresource.txt |
| /mydir | not loaded | |

#### `Thread.currentThread().getContextClassLoader()::getResourceAsStream`

| path | loaded | details |
| --- | --- | --- |
| mydir/myresource.txt | loaded | |
| /mydir/myresource.txt | not loaded | |
| mydir | loaded | 1 files found: myresource.txt |
| /mydir | not loaded | |

#### `LoadExperiment.class::getResourceAsStream`

| path | loaded | details |
| --- | --- | --- |
| mydir/myresource.txt | not loaded | |
| /mydir/myresource.txt | loaded | |
| mydir | not loaded | |
| /mydir | loaded | 1 files found: myresource.txt |

#### `getModule()::getResourceAsStream`

| path | loaded | details |
| --- | --- | --- |
| mydir/myresource.txt | loaded | |
| /mydir/myresource.txt | loaded | |
| mydir | loaded | 1 files found: myresource.txt |
| /mydir | loaded | 1 files found: myresource.txt |

### `./gradlew installDist && \`<br>&nbsp;&nbsp;`build/install/module-resource-dirs/bin/module-resource-dirs`

#### `ClassLoader.getSystemClassLoader()::getResourceAsStream`

| path | loaded | details |
| --- | --- | --- |
| mydir/myresource.txt | loaded | |
| /mydir/myresource.txt | not loaded | |
| mydir | loaded | 0 files found |
| /mydir | not loaded | |

#### `Thread.currentThread().getContextClassLoader()::getResourceAsStream`

| path | loaded | details |
| --- | --- | --- |
| mydir/myresource.txt | loaded | |
| /mydir/myresource.txt | not loaded | |
| mydir | loaded | 0 files found |
| /mydir | not loaded | |

#### `LoadExperiment.class::getResourceAsStream`

| path | loaded | details |
| --- | --- | --- |
| mydir/myresource.txt | not loaded | |
| /mydir/myresource.txt | loaded | |
| mydir | not loaded | |
| /mydir | loaded | 0 files found |

#### `getModule()::getResourceAsStream`

| path | loaded | details |
| --- | --- | --- |
| mydir/myresource.txt | loaded | |
| /mydir/myresource.txt | loaded | |
| mydir | loaded | 0 files found |
| /mydir | loaded | 0 files found |

## Via module path: no resources

Using the IDEA run command (with or without the "Delegate IDE build/run actions to Gradle"
option set), neither the `myresource.txt` file nor its containing directory is found, either
with or without a leading `/`. [According to JetBrains](https://youtrack.jetbrains.com/issue/IDEA-197469)
this is because IDEA uses the module path, while Gradle, by default, is using the classpath.

#### `ClassLoader.getSystemClassLoader()::getResourceAsStream`

| path | loaded | details |
| --- | --- | --- |
| mydir/myresource.txt | not loaded | |
| /mydir/myresource.txt | not loaded | |
| mydir | not loaded | |
| /mydir | not loaded | |

#### `Thread.currentThread().getContextClassLoader()::getResourceAsStream`

| path | loaded | details |
| --- | --- | --- |
| mydir/myresource.txt | not loaded | |
| /mydir/myresource.txt | not loaded | |
| mydir | not loaded | |
| /mydir | not loaded | |

#### `LoadExperiment.class::getResourceAsStream`

| path | loaded | details |
| --- | --- | --- |
| mydir/myresource.txt | not loaded | |
| /mydir/myresource.txt | not loaded | |
| mydir | not loaded | |
| /mydir | not loaded | |

#### `getModule()::getResourceAsStream`

| path | loaded | details |
| --- | --- | --- |
| mydir/myresource.txt | not loaded | |
| /mydir/myresource.txt | not loaded | |
| mydir | not loaded | |
| /mydir | not loaded | |

Adding the following block to the `build.gradle` file causes Gradle to use the module path, and
prevents Gradle from finding the resources:

```
run {
    inputs.property("moduleName", moduleName)
    doFirst {
        jvmArgs = [
                '--module-path', classpath.asPath,
                '--module', "$moduleName/$mainClassName"
        ]
        classpath = files()
    }
}
```

