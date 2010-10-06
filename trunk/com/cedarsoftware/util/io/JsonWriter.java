package com.reusable.io;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Output a Java object graph in JSON format.  This code handles cyclic
 * references and can serialize any Object graph without requiring a class
 * to be 'Serializeable' or have any specific methods on it.
 *
 * <p>Example usage:</p>
 *
 * <p>To output an object graph foo, use:</p><code>
 *     StringWriter sw = new StringWriter();<br/>
 *     JsonWriter jw = new JsonWriter(sw);<br/>
 *     jw.write(foo);<br/>
 *     System.out.println("in JSON format: " + sw);</code></br>
 *
 *  <p>That's it.  This can be used as a debugging tool.  Output an object
 *  graph using the above code.  You can copy that JSON output into this site
 *  which formats it with a lot of whitespace to make it human readable:</p>
 *  http://jsonformatter.curiousconcept.com/
 *
 *  <p>This will output any object graph deeply (or null).  Object references are
 *  properly handled.  For example, if you had A->B, B->C, and C->A, then
 *  A will be serialized with a B object in it, B will be serialized with a C
 *  object in it, and then C will be serialized with a reference to A (ref), not a
 *  redefinition of A.</p>
 *
 *  <p><b>Special case 1</b>: The primitive object wrappers (Boolean, Character, Byte,
 *  Short, Integer, Long, Float, and Double), plus String, Date, and Class
 *  objects are written in simple text output, and therefore would not be
 *  kept as references.  This keeps the serialization format small and
 *  more human readable. </p>
 *
 *  <p><b>Special case 2</b>: The Collection and Map classes are
 *  written in a simplified format so that the internal structure of the Collection
 * or Map type does not need to be exposed.  This makes it much easier to
 * hand construct Collections and Maps in JSON format.
 *  This keeps the serialization format small and more human readable. </p>
 *
 * @author John DeRegnaucourt (john@myotherdrive.com)
 * copyright (c) 2009, 2010 MyOtherDrive.com
 */
public class JsonWriter extends Writer
{
	private final Map<Object, Long> _objVisited = new IdentityHashMap();
	private final Map<Object, Long> _objsReferenced = new IdentityHashMap();
    private final Map<String, Collection> _fieldCache = new HashMap();
	private Writer _out;
	private char[] _charToString;
	private long _identity = 1;

	public JsonWriter(OutputStream out)
	{
		try
		{
			_out = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
			_charToString = new char[1];
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}

	public void write(Object obj) throws IOException
	{
		walk(obj);
		_objVisited.clear();
		writeImpl(obj, true);
        flush();
        _objVisited.clear();
        _objsReferenced.clear();
	}

	private void walk(Object obj)
	{
        if (obj == null)
        {
            return;
        }

        Long id = _objVisited.get(obj);
        if (id != null)
        {   // Only write an object once.
            _objsReferenced.put(obj, id);
            return;
        }

        _objVisited.put(obj, _identity++);
        final Class clazz = obj.getClass();

        if (isPrimitiveWrapper(clazz))
        {
            return;
        }

        if (clazz.isArray())
        {
            Class compType = clazz.getComponentType();
            if (!isPrimitiveWrapper(compType))
            {	// Speed up: do not walk "primitive" array types - they cannot reference objects, other than immutables.
                int len = Array.getLength(obj);

                for (int i=0; i < len; i++)
                {
                    walk(Array.get(obj, i));
                }
            }
        }
        else
        {
            walkFields(obj);
        }
	}

    private void walkFields(Object obj)
	{
		Collection fields = getDeepDeclaredFields(obj.getClass());

        for (Object field : fields)
        {
            Field f = (Field) field;
            f.setAccessible(true);

            if ((f.getModifiers() & Modifier.STATIC) != 0 || isPrimitiveWrapper(f.getType()))
            {    // speed up: primitives cannot reference another object
                continue;
            }

            try
            {
                walk(f.get(obj));
            }
            catch (IllegalAccessException ignored)
            {
            }
        }
	}

    private boolean writeOptionalReference(Object obj) throws IOException
    {
        if (_objVisited.containsKey(obj))
        {	// Only write (define) an object once in the JSON stream, otherwise emit a @ref
            _out.write("{\"@ref\":");
            _out.write(getId(obj));
            _out.write('}');
            return true;
        }

        // Mark the object as visited by putting it in the Map (this map is re-used / clear()'d after walk()).
        _objVisited.put(obj, null);
        return false;
    }

	private void writeImpl(Object obj, boolean showType) throws IOException
	{
        if (obj == null)
        {
            _out.write("{}");
            return;
        }

        if (obj.getClass().isArray())
        {
            writeArray(obj, showType);
        }
        else
        {
            if (obj instanceof Map)
            {
                writeMap((Map) obj, showType);
            }
            else if (obj instanceof Collection)
            {
                writeCollection((Collection) obj, showType);
            }
            else
            {
                writeObject(obj, showType);
            }
        }
	}

	private void writeId(String id) throws IOException
	{
		_out.write("\"@id\":");
		_out.write(id);
	}

	private void writeType(Object obj) throws IOException
	{
		_out.write("\"@type\":\"");
        Class c = obj.getClass();
        if (String.class.equals(c))
        {
            _out.write("string");
        }
        else if (Boolean.class.equals(c))
        {
            _out.write("boolean");
        }
        else if (Byte.class.equals(c))
        {
            _out.write("byte");
        }
        else if (Short.class.equals(c))
        {
            _out.write("short");
        }
        else if (Integer.class.equals(c))
        {
            _out.write("int");
        }
        else if (Long.class.equals(c))
        {
            _out.write("long");
        }
        else if (Double.class.equals(c))
        {
            _out.write("double");
        }
        else if (Float.class.equals(c))
        {
            _out.write("float");
        }
        else if (Character.class.equals(c))
        {
            _out.write("char");
        }
        else if (Date.class.equals(c))
        {
            _out.write("date");
        }
        else if (Class.class.equals(c))
        {
            _out.write("class");
        }
        else
        {
		    _out.write(obj.getClass().getName());
        }
		_out.write('"');
	}

	private void writeClass(Object obj, boolean showType) throws IOException
	{
        String value = ((Class)obj).getName();
		if (showType)
		{
			_out.write('{');
            writeType(obj);
			_out.write(',');
			_out.write("\"value\":\"");
			writeJsonUtf8String(value);
			_out.write("\"}");
		}
		else
		{
            _out.write('"');
			writeJsonUtf8String(value);
            _out.write('"');
		}
	}

	private void writeDate(Object obj, boolean showType) throws IOException
	{
        String value =  Long.toString(((Date)obj).getTime());

		if (showType)
		{
			_out.write('{');
			writeType(obj);
			_out.write(',');
			_out.write("\"value\":");
			_out.write(value);
			_out.write('}');
		}
		else
		{
			_out.write(value);
		}
	}

	private void writePrimitive(Object obj) throws IOException
	{
        String value = obj.toString();

        if (obj instanceof Character)
        {
            _out.write('"');
            _out.write(value);
            _out.write('"');
        }
        else
        {
            _out.write(value);
        }
	}

	private void writeArray(Object array, boolean showType) throws IOException
	{
        if (writeOptionalReference(array))
        {
            return;
        }

        Class arrayType = array.getClass();
		int len = array == null ? 0 : Array.getLength(array);
		boolean referenced = _objsReferenced.containsKey(array);
		boolean typeWritten = showType && !Object[].class.equals(arrayType);

		if (typeWritten || referenced)
		{
			_out.write('{');
		}

		if (referenced)
		{
			writeId(getId(array));
			_out.write(',');
		}

		if (typeWritten)
		{
			writeType(array);
			_out.write(',');
		}

		if (len == 0)
		{
			if (typeWritten || referenced)
			{
				_out.write("\"@items\":[]}");
			}
			else
			{
				_out.write("[]");
			}
			return;
		}

		if (typeWritten || referenced)
		{
			_out.write("\"@items\":[");
		}
		else
		{
			_out.write('[');
		}

		final Class componentClass = array.getClass().getComponentType();
		final boolean isStringArray = String[].class.equals(arrayType);
		final boolean isDateArray = Date[].class.equals(arrayType);
		final boolean isPrimitiveArray = isPrimitiveWrapper(componentClass);
		final boolean isClassArray = Class[].class.equals(arrayType);
		final boolean isObjectArray = Object[].class.equals(arrayType);

		for (int i=0; i < len; i++)
		{
			final Object value = Array.get(array, i);

			if (value == null)
			{
				_out.write("null");
			}
            else if (isStringArray)
            {
                _out.write('"');
                writeJsonUtf8String((String) value);
                _out.write('"');
            }
			else if (isDateArray)
			{
                _out.write(Long.toString(((Date)value).getTime()));
			}
			else if (isClassArray)
			{
                writeClass(value, false);
			}
            else if (isPrimitiveArray)
            {
                writePrimitive(value);
            }
			else if (isObjectArray)
			{
				if (value instanceof String)
				{
                    _out.write('"');
                    writeJsonUtf8String((String) value);
                    _out.write('"');
				}
                else if (value instanceof Date)
                {
                    writeDate(value, true);
                }
                else if (value instanceof Class)
                {
                    writeClass(value, true);
                }
				else if (value instanceof Boolean || value instanceof Long || value instanceof Double)
				{   // These types can be inferred - true | false, long integer number (- or digits), or double precision floating point (., e)
                    writePrimitive(value);
				}
				else
				{
					writeImpl(value, true);
				}
			}
			else
			{
				boolean forceType = !value.getClass().equals(componentClass);
				writeImpl(value, forceType);
			}

			if (i != len - 1)
			{
				_out.write(',');
			}

		}
		_out.write(']');
		if (typeWritten || referenced)
		{
			_out.write('}');
		}
	}

    private void writeCollection(Collection col, boolean showType) throws IOException
    {
        if (writeOptionalReference(col))
        {
            return;
        }

        boolean referenced = _objsReferenced.containsKey(col);

        _out.write("{");
        if (showType || referenced)
        {
            if (referenced)
            {
                writeId(getId(col));
            }

            if (showType)
            {
                if (referenced)
                {
                    _out.write(",");
                }
                writeType(col);
            }
        }

        int len = col.size();

        if (len == 0)
        {
            _out.write("}");
            return;
        }

        if (showType || referenced)
        {
            _out.write(",");
        }

        _out.write("\"@items\":[");
        Iterator i = col.iterator();

        while (i.hasNext())
        {
            writeCollectionElement(i.next());

            if (i.hasNext())
            {
                _out.write(",");
            }

        }
        _out.write("]}");
    }

    private void writeMap(Map map, boolean showType) throws IOException
    {
        if (writeOptionalReference(map))
        {
            return;
        }
        
        boolean referenced = _objsReferenced.containsKey(map);

        _out.write("{");
        if (showType || referenced)
        {
            if (referenced)
            {
                writeId(getId(map));
            }

            if (showType)
            {
                if (referenced)
                {
                    _out.write(",");
                }
                writeType(map);
            }
        }

        if (map.isEmpty())
        {
            _out.write("}");
            return;
        }

        if (showType || referenced)
        {
            _out.write(",");
        }

        _out.write("\"@keys\":[");
        Iterator i = map.keySet().iterator();

        while (i.hasNext())
        {
            writeCollectionElement(i.next());

            if (i.hasNext())
            {
                _out.write(",");
            }
        }

        _out.write("],\"@items\":[");
        i = map.values().iterator();

        while (i.hasNext())
        {
            writeCollectionElement(i.next());

            if (i.hasNext())
            {
                _out.write(",");
            }
        }

        _out.write("]}");
    }
    
    /**
     * Write an element that is contained in some type of collection or Map.
     */
    private void writeCollectionElement(Object o) throws IOException
    {
        if (o == null)
        {
            _out.write("null");
        }
        else if (o instanceof Boolean || o instanceof Long || o instanceof Double)
        {
            _out.write(o.toString());
        }
        else if (o instanceof String)
        {
            _out.write("\"");
            writeJsonUtf8String(o.toString());
            _out.write("\"");
        }
        else
        {
            writeImpl(o, true);
        }
    }


	private void writeObject(Object obj, boolean showType) throws IOException
	{
        if (obj instanceof String)
        {
            _out.write('"');
            writeJsonUtf8String((String) obj);
            _out.write('"');
            return;
        }
        else if (obj instanceof Date)
        {
            writeDate(obj, showType);
            return;
        }
        else if (obj instanceof Class)
        {
            writeClass(obj, showType);
            return;
        }

        if (writeOptionalReference(obj))
        {
            return;
        }

        _out.write('{');
		boolean referenced = _objsReferenced.containsKey(obj);
		if (referenced)
		{
			writeId(getId(obj));
		}

		if (referenced && showType)
		{
			_out.write(',');
		}

		if (showType)
		{
			writeType(obj);
		}

		boolean first = !showType;
		if (referenced && !showType)
		{
			first = false;
		}

		Collection fields = getDeepDeclaredFields(obj.getClass());

        for (Object field : fields)
        {
            Field f = (Field) field;
            f.setAccessible(true);

            if ((f.getModifiers() & Modifier.STATIC) != 0)
            {
                continue;
            }

            if (first)
            {
                first = false;
            }
            else
            {
                _out.write(',');
            }
            _out.write('"');
            writeJsonUtf8String(f.getName());
            _out.write("\":");

            Object o;
            try
            {
                o = f.get(obj);
            }
            catch (IllegalAccessException e)
            {
                o = null;
            }

            if (o == null)
            {    // don't quote null
                _out.write("null");
                continue;
            }

            Class type = f.getType();
            boolean forceType = !o.getClass().equals(type);     // If types are not exactly the same, write "@type" field

            if (o instanceof String)
            {
                _out.write('"');
                writeJsonUtf8String((String) o);
                _out.write('"');
            }
            else if (o instanceof Date)
            {
                writeDate(o, forceType);
            }
            else if (o instanceof Class)
            {
                writeClass(o, forceType);
            }
            else if (isPrimitiveWrapper(type))
            {   // This 'if' statement must be after String, Date, and Class check because isPrimitiveWrapper consider's them
                // primitives.  However, they must be handled individually, so they are handled above.
                writePrimitive(o);
            }
            else
            {
                writeImpl(o, forceType);
            }
        }

		_out.write('}');
	}

	private static boolean isPrimitiveWrapper(Class c)
	{
        return  c.isPrimitive() ||
                Byte.class.equals(c) ||
                String.class.equals(c) ||
                Integer.class.equals(c) ||
                Long.class.equals(c) ||
                Double.class.equals(c) ||
                Character.class.equals(c) ||
                Float.class.equals(c) ||
                Boolean.class.equals(c) ||
                Short.class.equals(c) ||
                Date.class.equals(c) ||
                Class.class.equals(c);
	}

	/**
	 * Write out special characters "\b, \f, \t, \n, \r", as such, backslash as \\
	 * quote as \" and values less than an ASCII space (20hex) as "\\u00xx" format,
	 * characters in the range of ASCII space to a '~' as ASCII, and anything higher in UTF-8.
	 */
	private void writeJsonUtf8String(String s) throws IOException
	{
		int len = s.length();

		for (int i=0; i < len; i++)
		{
			char c = s.charAt(i);

			if (c < ' ')
			{	// Anything less than ASCII space, write either in \\u00xx form, or the special \t, \n, etc. form
				if (c == '\b')
				{
					_out.write('\b');
				}
				else if (c == '\t')
				{
					_out.write('\t');
				}
				else if (c == '\n')
				{
					_out.write('\n');
				}
				else if (c == '\f')
				{
					_out.write('\f');
				}
				else if (c == '\r')
				{
					_out.write('\r');
				}
				else
				{
					String hex = Integer.toHexString(c);
					_out.write("\\u");
					int pad = 4 - hex.length();
					for (int k=0; k < pad; k++)
					{
						_out.write('0');
					}
					_out.write(hex);
				}
			}
			else if (c > '~')
			{	// Anything greater than an ASCII '~' write in UTF-8 form (multi-byte encoded)
				_charToString[0] = c;
				_out.write(new String(_charToString));
			}
			else
			{
				if (c == '\\' || c == '"')
		        {
					_out.write('\\');
		            _out.write(c);
		        }
				else
				{
					_out.write(c);
				}
			}
		}
	}

	public Collection getDeepDeclaredFields(Class c)
	{
        Collection fields = _fieldCache.get(c.getName());
        if (fields != null)
        {
            return fields;
        }

		fields = new ArrayList();
        Class curr = c;
		while (curr != null)
		{
			Field[]  local = curr.getDeclaredFields();

			if (local != null)
			{
                fields.addAll(Arrays.asList(local));
			}

			curr = curr.getSuperclass();
		}
        _fieldCache.put(c.getName(), fields);
		return fields;
	}

	public void flush()
	{
		try
		{
			if (_out != null)
			{
				_out.flush();
			}
		}
		catch (IOException ignored)
		{
		}
	}

	public void close()
	{
		try
		{
			flush();
			if (_out != null)
			{
				_out.close();
			}
		}
		catch (IOException ignored)
		{
		}
	}

	public void write(char[] cbuf, int off, int len) throws IOException
	{
		_out.write(cbuf, off, len);
	}

	private String getId(Object o)
	{
        Long id = _objsReferenced.get(o);
        return id == null ? null : Long.toString(id);
    }

    public static String convertToJSON(Object item)
    {
        try
        {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            JsonWriter writer = new JsonWriter(stream);
            writer.write(item);
            writer.close();
            return new String(stream.toByteArray(), "UTF-8");
        }
        catch (IOException e)
        {
            return null;
        }
    }    
}