**json-io** has been moved to `GitHub` at: https://github.com/jdereg/json-io

To include **json-io** in your maven **pom.xml**:

```
<dependency>
  <groupId>com.cedarsoftware</groupId>
  <artifactId>json-io</artifactId>
  <version>4.1.6</version>
</dependency>
```

<a href='https://coinbase.com/checkouts/f5ab44535dc53e81b79e71f123ebdf42'>Donate Bitcoins</a>

_Check out **java-util** for useful Java utilities.  Available on [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22java-util%22) or [OSS SonaType](https://oss.sonatype.org/index.html#nexus-search;quick~java-util) and [java-util](https://github.com/jdereg/java-util) source on GitHub._


---


**json-io** consists of two main classes, a reader (`JsonReader`) and a writer (`JsonWriter`).  json-io eliminates the need for using `ObjectInputStream / ObjectOutputStream` to serialize Java and instead uses the JSON format.  There is a 3rd optional class (`JsonObject`) see 'Non-typed Usage' below.

**json-io** does not require that Java classes implement `Serializable` or `Externalizable` to be serialized, unlike `ObjectInputStream` / `bjectOutputStream`. It will serialize any Java object graph into JSON and retain complete graph semantics / shape and object types. This includes supporting private fields, private inner classes (static or non-static), of any depth. It also includes handling cyclic references. Objects do not need to have public constructors to be serialized. The output JSON will not include transient fields, identical to the `ObjectOutputStream` behavior.

The `JsonReader` / `JsonWriter` code does not depend on any native or 3rd party libraries.

## Format ##
**json-io** uses proper JSON format. As little type information is included in the JSON format to keep it compact as possible. When an object's class can be inferred from a field type or array type, the object's type information is left out of the stream. For example, a `String[]` looks like `["abc", "xyz"]`.

When an object's type must be emitted, it is emitted as a meta-object field `"@type":"package.class"` in the object. When read, this tells the `JsonReader` what class to instantiate.

If an object is referenced more than once, or references an object that has not yet been defined, (say A points to B, and B points to C, and C points to A), it emits a `"@ref":n` where 'n' is the object's integer identity (with a corresponding meta entry `"@id":n` defined on the referenced object). Only referenced objects have IDs in the JSON output, reducing the JSON String length.

## Performance ##
**json-io** was written with performance in mind. In most cases json-io is faster than the JDK's `ObjectInputStream` / `ObjectOutputStream`. As the tests run, a log is written of the time it takes to serialize / deserialize and compares it to `ObjectInputStream` / `ObjectOutputStream` (if the static variable _debug is true in `TestJsonReaderWriter`)._

## Usage ##
**json-io** can be used directly on JSON Strings or with Java's Streams.

Example 1: `String `to Java object
```
Object obj = JsonReader.jsonToJava("[\"Hello, World\"]");
This will convert the JSON String to a Java Object graph. In this case, 
it would consist of an Object[] of one String element.
```

Example 2: Java object to JSON String
```
Employee emp;
// Emp fetched from database
String json = JsonWriter.objectToJson(emp);
This example will convert the Employee instance to a JSON String. 
If the JsonReader were used on this String, it would reconstitute a Java Employee instance.
```

Example 3: `InputStream` to Java object
```
JsonReader jr = new JsonReader(inputStream);
Employee emp = (Employee) jr.readObject();
```
In this example, an `InputStream` (could be from a File, the Network, etc.) is supplying an unknown amount of JSON. The `JsonReader` is used to wrap the stream to parse it, and return the Java object graph it represents.

Example 4: Java Object to `OutputStream`
```
Employee emp;
// emp obtained from database
JsonWriter jw = new JsonWriter(outputStream);
jw.write(emp);
jw.close();
```
In this example, a Java object is written to an output stream in JSON format.

## Non-typed Usage ##
**json-io** provides the choice to use the generic "Map of Maps" representation of an object, akin to a Javascript associative array. When reading from a JSON String or `InputStream` of JSON, the `JsonReader` can be constructed like this:

```
Map graph = JsonReader.jsonToMaps(String json);
```
-- or --
```
JsonReader jr = new JsonReader(InputStream, true);
Map map = (Map) jr.readObject();
```
This will return an untyped object representation of the JSON String as a Map of Maps, where the fields are the `Map` keys (Strings), and the field values are the associated Map's values. In this representation the Map instance returned is actually a `JsonObject` instance (from **json-io**). This `JsonObject` implements the `Map` interface permitting access to the entire object. Cast to a `JsonObject`, you can see the type information, position within the JSON stream, and other information.

This 'Maps' representation can be re-written to a JSON String or Stream and the output JSON will exactly match the original input JSON stream. This permits a JVM receiving JSON strings / streams that contain class references which do not exist in the JVM that is parsing the JSON, to completely read / write the stream. Additionally, the Maps can be modified before being written, and the entire graph can be re-written in one collective write. Any object model can be read, modified, and then re-written by a JVM that does not contain any of the classes in the JSON data.

## Customization ##
New APIs have been added to allow you to associate a custom reader / writer class to a particular class if you want it to be read / written specially in the JSON output. **json-io 1.x** required a custom method be implemented on the object which was having its JSON format customized. This support has been removed. That approach required access to the source code for the class being customized. The new **json-io 2.0** approach allows you to customize the JSON format for classes for which you do not have the source code.

### Dates ###
To specify an alternative date format for JsonWriter:
```
Map args = new HashMap();
args.put(JsonWriter.DATE_FORMAT, JsonWriter.ISO_DATE_TIME);
String json = JsonWriter.objectToJson(root, args);
```

In this example, the ISO yyyy/MM/ddThh:mm:ss format is used to format dates in the JSON output.  The 'value' associated to the 'DATE\_FORMAT' key can be JsonWriter.ISO\_DATE\_TIME, JsonWriter.ISO\_DATE, a date format String pattern (as a String), or a `java.text.Format` instance.  If you choose to use a `Format` instance, make sure to create a new one each time, as `SimpleDateFormat` is not thread safe.  Or use Cedar Software (java-util) `SafeSimpleDateFormat`.

## Javascript ##
Included is a small Javascript utility that will take a JSON output stream created by the JSON writer and substitute all @ref's for the actual pointed to object. It's a one-line call - `resolveRefs(json)`. This will completely fix up the @ref's to point to the appropriate objects.

## What's next? ##
Even though json-io is perfect for Java / Javascript serialization, there are other great uses for it:

## Cloning ##
Many projects use `JsonWriter` to write an object to JSON, then use the `JsonReader` to read it in, perfectly cloning the original object graph:
```
Employee emp;
// emp obtained from database
Employee deepCopy = (Employee) cloneObject(emp);

public Object cloneObject(Object root)
{
    return JsonReader.jsonToJava(JsonWriter.objectToJson(root));    
}
```

## Debugging ##
Instead of doing `System.out.println` debugging, call `JsonWriter.objectToJson(obj)` and dump that String out. It will reveal the object in all it's glory.

## Pretty-Printing JSON ##

Use JsonWriter.formatJson() API to format a passed in JSON string to a nice, human readable format. Also, when writing JSON data, use the JsonWriter.objectToJson(o, args) API, where args is a Map with a key of JsonWriter.PRETTY\_PRINT and a value of 'true' (boolean or String). When run this way, the JSON written by the JsonWriter will be formatted in a nice, human readable format.

## RESTful support ##
**json-io** can be used as the fundamental data transfer method between a Javascript / JQuery / Ajax client and a web server in a RESTful fashion. Used this way, you can create more active sites like Google's GMail, MyOtherDrive online backup, etc.

See https://github.com/jdereg/json-command-servlet for a light-weight servlet that processes Ajax / XHR calls.

## Version History ##
  * 2.9.3
    * Bug fix: When writing a `Map` with JSON primitive keys (`String`, `Long`, `Double`, or `Boolean`), a `ClassCastException` was being thrown if the type was `Long`, `Double`, or `Boolean`. This has been fixed with test added.
  * 2.9.2
    * Android: Rearranged [:.] to [.:] in regular expressions for Android compatibility. Technically, it should not matter, but [:.] was causing `java.util.regex.PatternSyntaxException: Syntax error U_ILLEGAL_ARGUMENT_ERROR` on Android JVM.
    * Bug fix: When using the `JsonWriter` arguments `Map` with `FIELD_SPECIFIERS`, if you specified a field that was transient, it was not serialized. This has been corrected. When you specify the field list for a given class, the `Map` can contain any non-static fields in the class, including `transient` fields.
    * All JUnit tests converted to **Groovy**.
  * 2.9.1
    * Bug fix: Parameterized types are only internally stamped onto generic Maps (Maps read with no `@type`) if the field that points to the `Map` is a template variable or it has template arguments.
    * Performance optimization: tracing references specially handles `Collection` and `Map`.  By avoiding internal structures, the reference trace is much faster.
  * 2.9.0
    * Unmodifiable Collections and Maps can now be serialized.
    * Added tests to ensure that JsonReader.jsonToMaps() coerces the RHS values when logical primitives, to the optional associated @type's fields.
    * More tests and improved code-coverage.
  * 2.8.1
    * bugfix: JsonReader.jsonToMaps() API was incorrectly attempting to instantiate peer objects (specified by "@type" field in the JSON) when in 'maps' mode. This made JsonReader.jsonToMaps() fail if all referenced class names did not exist in the JVM. This has been fixed.
    * Minor Javadoc cleanup (Daniel Darabos @darabos)
    * Began migration of tests from one monolithic Java class (TestJsonReaderWriter) to individual Groovy test classes.
  * 2.8.0
    * Additional attempt to instantiate classes via sun.misc.Unsafe added (optional must be turned on by calling JsonReader.setUseUnsafe(true)). json-io already tries all constructors (private or public) with varying arguments, etc. If this fails and unsafe is true, it will try sun.misc.Unsafe.allocateInstance() which effectively does a C-style malloc(). This is OK, because the rest of JsonReader fills in the member variables from the serialized content. (Submitted by Kai Hufenback).
  * 2.7.6
    * Performance optimizations. Use of switch statement instead of if-else chains.
    * JDK 1.7 for source code and target JVM.
  * 2.7.5
    * Bug fix: `ArrayIndexOutOfBounds` could still occur when serializing a class with multiple Templated fields. The exception has been fixed.
  * 2.7.4
    * Bug fix: `ArrayIndexOutOfBounds` exception occurring when serializing non-static inner class with nested template parameters. JsonReader was incorrectly passing on the 'this$0' field for further template argument processing when it should not have.
  * 2.7.3
    * JsonReader executes faster (more efficiently manages internal 'snippet' buffer and last line and column read.)
    * Improved date parsing: day of week support (long or short name), days with suffix (3rd, 25th, etc.), Java's default .toString() output for Date now parses, full time zone support, extra whitespace allowed within the date string.
    * Added ability to have custom JSON writers for interfaces (submitted by Kai Hufenbach).
  * 2.7.2
    * When writing JSON, less memory is used to manage referenced objects. JsonWriter requires a smaller memory foot print during writing.
    * New option available to JsonWriter that allows you to force enums to not write private variables. First you can make them transient. However, if you do not own the code or cannot change it, you can set the JsonWriter.getArgs().put(ENUM\_PUBLIC\_ONLY, true), and then only public fields on enums will be emitted.
  * 2.7.1
    * `BigDecimal` and `BigInteger` are always written as a primitive (immutable, non-referenced) value. This uniformizes their output.
  * 2.7.0
    * Updated to support JSON root of String, Integer, Floating point, and Boolean, per the updated JSON RFP.  Example, the String "football" is considered valid JSON. The JsonReader.readObject() API and JsonReader.jsonToJava() will return a String in this case. The JsonReader.jsonToMaps() API will still return a Map (JsonObject), and the @items key will contain an Object[.md](.md) with the single value (String, Integer, Double, Boolean) in it.
    * When a Java Map has only String keys in it, json-io will use the JSON object keys directly and associate the values to the keys as expected. For example, the Map ['Football':true] would be written {"Football":true}. However, if the keys are non-Strings, then Maps will be written as a JSON object with {"@keys":[...], "@items":[...]}, where @keys is an array [.md](.md) of all the keys, and the @items is an array [.md](.md) of all the values. Entry 0 of @keys matches with Entry 0 in the @items array, and so on. Thanks for Christian Reuschling for making the request and then supplying the implementation.
    * Change some APIs from private to protected to allow for subclasses to more easily override the default behavior.
  * 2.6.1
    * Bug fix: An internal `Map` that kept meta-information about a Java Class, changed to `ConcurrentHashMap` from `HashMap`.
  * 2.6.0
    * Added support for specifying which fields on a class will be serialized. Use the `JsonWriter.FIELD_SPECIFIERS` key and assign the value to a `Map<Class, List<String>>`, where the keys of the `Map` are classes (e.g. `Bingo.class`) and the values are `List<String>`, which indicates the fields to serialize for the class. This provides a way to reduce the number of fields written for a given class. For example, you may encounter a 3rd Party class which fails to serialize because it has an oddball field like a `ClassLoader` reference as a non-static, non-transient field. You may not have access to the source code to mark the field as `transient`. In this case, add the appropriate entries in the `FIELD_SPECIFIERS` map. Voila, problem solved. Use the `JsonWriter` API that takes `optionalArgs Map`. The key for this `Map` is `JsonWriter.FIELD_SPECIFIER` and the value is `Map<Class, List<String>>`.
  * 2.5.2
    * `java.net.URL` can now be used as a constructor argument. The reader was throwing an exception instantiating a constructor with a `URL` parameter.
    * `java.lang.Object` parameters in constructor arguments are now tried with both null and new Object() now.
  * 2.5.1
    * Fixed bug in processing `Map's` with `Set` as key (was introduced in 2.5.0).  Appropriate tests added.
  * 2.5.0
    * New 'Pretty-Print' option available. If the 'args' Map passed to JsonWriter.objectToJson(o, args) contains the key `JsonWriter.PRETTY_PRINT` and the value 'true' (`boolean` or `String`), the `JsonWriter` output will be formatted in a nice human readable format.
    * Convert a JSON String to Pretty-Print format using `JsonWriter.formatJson(String json)`. A `String` will be returned with the JSON formatted in a nice, human readable format.
    * If a Field contains Parameterized types (e.g., `Map`, and so on), `JsonReader` will use those fields to process objects deep within `Maps`, `Collections`, etc. and still create the proper Java class.
  * 2.4.5
    * Allow empty `String` ("") to be assigned to `Date` field or Date[.md](.md) element, in which case the corresponding `Date`instance is set to null.
  * 2.4.4
    * Allow empty string ("") to be assigned to non-String field (same as 2.4.2) but for `Map` of `Map` (`JsonObject`) return (as opposed to returned Java Object).
  * 2.4.2
    * If an empty string ("") is assigned to a non-String field, it will set the field to null, unless it is a primitive or a primitive wrapper, then it will set the field to the JVM default value (int 0, boolean false, etc.)
  * 2.4.1
    * Added support to allow primitives and String to be assigned to abstract / interface / base type field on an object (Serializable, Comparable, Object, etc.). Primitives can now be 'set' into these fields, without any additional type information.
  * 2.4.0
    * Primitives can be set from Strings
    * Strings can be set from primitives
    * `BigDecimal` and `BigInteger` can be set from primitives, Strings, `BigDecimal`, or `BigInteger`
  * 2.3.0
    * `Maps` and `Collections` (`Lists`, `Set`, etc.) can be read in, even when there are no `@keys` or `@items` as would come from a Javascript client.
    * json-io will now use the generic info on a `Map<Foo, Bar>` or `Collection<Foo>` object's field when the `@type` information is not included. json-io will then know to create `Foo` instances, `Bar` instances, etc. within the `Collection` or `Map`.
    * All parsing error messages now output the last 100 characters read, making it easier to locate the problem in JSON text. Furthermore, line and column number are now included (before it was a single position number). This allows you to immediately find the offending location.
    * You can now force `@type` to be written (not recommended) by putting the `JsonWriter.TYPE` key in the `JsonWriter` args map, and assigning the associated value to `true`.
  * 2.2.32
    * Date/Time format can be customized when writing JSON output. New optional `Map args` parameter added to main API of `JsonWriter` that specifies additional parameters for `JsonWriter`. Set the key to `JsonWriter.DATE_FORMAT` and the value to a `SimpleDateFormat` string.  Two ISO formats are available for convenience as constants on `JsonWriter`, `JsonWriter.ISO_DATE_FORMAT` and `JsonWriter.ISO_DATE_TIME_FORMAT`.
    * `JsonReader` updated to read many different date/time formats.
    * When `JsonReader` encounters a class that cannot be constructed, you can associate a `ClassFactory` to the class, so that when the un-instantiable class is encountered, your factory class will be called to create the class.  New API to make this assignment: `JsonReader.assignInstantiator(Class c, ClassFactory factory)`
  * 2.2.31
    * Adds ability to instantiate a wider range of constructors. This was done by attempting construction with both null and non-null values for many common class types (Collections, String, Date, Timezone, etc.)
  * 2.2.30
    * `java.sql.Date` when read in, was instantiated as a `java.util.Date`. This has been corrected.
  * 2.2.29
    * First official release through Maven Central


**json-io** featured on [json.org](http://json.org).

by John DeRegnaucourt