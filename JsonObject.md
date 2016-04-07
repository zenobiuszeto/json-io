# Introduction #

JsonObject implements the java.util.Map interface so that you can manipulate a JSON string / stream that was read using the 'Maps' technique.  It adds 'house keeping' fields to the Map so that it can reconstitute the original JSON stream if the 'Maps' representation is re-written.

# Details #

When you call the JsonReader.jsonToMaps() API, a root JsonObject is always returned.  You can cast it to a Map and work with it that way.  The Map keys are the Java classes's property (member) names, and the associated values are the values for the fields.  If the field is a primitive or a primitive wrapper, the value is a primitive wrapper of the value.  Strings remain Strings, Dates are Dates, and Class instances are still classes.

If the field of a class is a complex object (it points to another object), then the value associated to the field is another JsonObject (and so on).

If the field of a class is an array [.md](.md), then the value is a Map (JsonObject) that contains a key with the name '@items' and the value is an ArrayList containing the values of the array.