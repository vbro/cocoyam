# cocoyam

### Description

`cocoyam` is a collection of java utility libraries.
It is not intended to compete with existing major libraries such as Apache Commons and Google Guava; there is no need to "re-invent the wheel" by implementing existing functionality.

The `cocoyam` project is concerned with implementing functionality that is not present in or cannot be readily achieved with existing major java libraries. This is done regardless of the complexity of implementation and regardless of level of utility provided by such functionality. If you've thought about it and it would make your life easier, someone else could make use of it.

### Utilities implemented in `cocoyam`
1. Indexed Set. This [java.util.set](https://docs.oracle.com/javase/8/docs/api/java/util/Set.html) implementation indexes items placed in it by attribute, and in turn this index can be used to retrieve items from the set.

2. JsonUtil. This provides a method to create a [javax.json.JsonValue](https://docs.oracle.com/javaee/7/api/javax/json/JsonValue.html) out of any java Object. [java.util.Map](https://docs.oracle.com/javase/8/docs/api/java/util/Map.html)s are represented as Json objects while [java.lang.Iterable](https://docs.oracle.com/javase/8/docs/api/java/lang/Iterable.html)s and java arrays are represented as json arrays. Contents of these these structures are recursively evaluated. 

### Developer notes
To build `cocoyam` change to the directory where this project is cloned and run:
```
./gradlew clean build
```

### Contributing
If you have ideas of functionality to add to the project that meet the guidelines stated in the Description above, please have a look at the [contributing page](CONTRIBUTING.md).

### Licence
This project uses the [MIT License](LICENSE.md).
