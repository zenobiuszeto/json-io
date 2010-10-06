package com.cedarsoftware.util.io;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Output a Java object graph in JSON format.  This code handles cyclic
 * references and can serialize any Object graph without requiring a class
 * to be 'Serializeable' or have any specific methods on it.
 * <br/><ul><li>
 * Call the static method: <code>JsonWriter.toJson(employee)</code>.  This will 
 * convert the passed in 'employee' instance into a JSON String.</li>
 * <li>Using streams:   
 *     <pre>     JsonWriter writer = new JsonWriter(stream);
 *     writer.write(employee);
 *     writer.close();</pre>
 * This will write the 'employee' object to the passed in OutputStream.
 * </li>
 * <p>That's it.  This can be used as a debugging tool.  Output an object
 * graph using the above code.  You can copy that JSON output into this site
 * which formats it with a lot of whitespace to make it human readable:
 * http://jsonformatter.curiousconcept.com/
 * <br/><br/>
 * <p>This will output any object graph deeply (or null).  Object references are
 * properly handled.  For example, if you had A->B, B->C, and C->A, then
 * A will be serialized with a B object in it, B will be serialized with a C
 * object in it, and then C will be serialized with a reference to A (ref), not a
 * redefinition of A.</p>
 * <br/>
 * If a <code>void _writeJson(Writer writer)</code> method is found on an object, then that
 * method will be called to output the JSON for that object.  A corresponding
 * <code>void _readJson(Map map)</code> method is looked for and called if it exists.  These
 * should rarely (if ever) be used.  See the TestJsonReaderWriter for an example of this
 * usage.<br/><br/>
 * @author John DeRegnaucourt (jdereg@gmail.com)
 * <br/>
 * Copyright [2010] John DeRegnaucourt
 * <br/><br/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <br/><br/>
 *    http://www.apache.org/licenses/LICENSE-2.0
 * <br/><br/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
public class JsonWriter extends Writer
{
	private final Map<Object, Long> _objVisited = new IdentityHashMap<Object, Long>();
	private final Map<Object, Long> _objsReferenced = new IdentityHashMap<Object, Long>();
    private final Map<String, ClassMeta> _classMeta = new HashMap<String, ClassMeta>();
	private Writer _out;
	private long _identity = 1;

    static class ClassMeta extends LinkedHashMap<String, Field>
    {
        Method _writeMethod = null;
        Method _readMethod = null;
    }

	public JsonWriter(OutputStream out) throws IOException
	{
		try
		{
			_out = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
		}
		catch (UnsupportedEncodingException e)
		{
			throw new IOException("Unsupported encoding.  Get a JVM that supports UTF-8", e);
		}
	}

	public void write(Object obj) throws IOException
	{
		traceReferences(obj);
		_objVisited.clear();
		writeImpl(obj, true);
        flush();
        _objVisited.clear();
        _objsReferenced.clear();
	}

	private void traceReferences(Object root)
	{
        Deque<Object> stack = new LinkedList<Object>();
        stack.addFirst(root);

        while (!stack.isEmpty())
        {
            Object obj = stack.removeFirst();
            if (obj == null)
            {
                continue;
            }

            Long id = _objVisited.get(obj);
            if (id != null)
            {   // Only write an object once.
                _objsReferenced.put(obj, id);
                continue;
            }

            _objVisited.put(obj, _identity++);
            final Class clazz = obj.getClass();

            if (isPrimitiveWrapper(clazz))
            {   // Can't reference anything
                continue;
            }

            if (clazz.isArray())
            {
                Class compType = clazz.getComponentType();
                if (!isPrimitiveWrapper(compType))
                {	// Speed up: do not traceReferences "primitive" array types - they cannot reference objects, other than immutables.
                    int len = Array.getLength(obj);

                    for (int i=0; i < len; i++)
                    {
                        Object o = Array.get(obj, i);
                        if (o != null)
                        {   // Slight perf gain (null is legal)
                            stack.addFirst(o);
                        }
                    }
                }
            }
            else
            {
                traceFields(stack, obj);
            }
        }
	}

    private void traceFields(Deque<Object> stack, Object obj)
	{
		ClassMeta fields = getDeepDeclaredFields(obj.getClass(), _classMeta);

        for (Field field : fields.values())
        {
            try
            {
                if (isPrimitiveWrapper(field.getType()))
                {    // speed up: primitives cannot reference another object
                    continue;
                }

                Object o = field.get(obj);
                if (o != null)
                {
                    stack.addFirst(o);
                }
            }
            catch (IllegalAccessException ignored) { }
        }
	}

	private void writeImpl(Object obj, boolean showType) throws IOException
	{
        if (obj == null)
        {
            _out.write("{}");
        }
        else if (obj.getClass().isArray())
        {
            writeArray(obj, showType);
        }
        else
        {
            writeObject(obj, showType);
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

        // Mark the object as visited by putting it in the Map (this map is re-used / clear()'d after traceReferences()).
        _objVisited.put(obj, null);
        return false;
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
	        _out.write(c.getName());
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
			_out.write("\"value\":");
			writeJsonUtf8String(value);
			_out.write('}');
		}
		else
		{
			writeJsonUtf8String(value);
		}
	}

	private void writeDate(Object obj, boolean showType) throws IOException
	{
        String value = Long.toString(((Date)obj).getTime());

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
            _out.write('\"');
        	_out.write((Character)obj);
            _out.write('\"');
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
		int len = Array.getLength(array);
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

		final int lenMinus1 = len - 1;

        // Intentionally processing each in separate custom loop for speed.
        // All of them could be handled using reflective Array.get()
        // but it is slower.  I chose speed over code length.
		if (byte[].class.equals(arrayType))
		{
			byte[] bytes = (byte[]) array;
	        for (int i=0; i < lenMinus1; i++)
	        {
	        	_out.write(Integer.toString(bytes[i]));
                _out.write(',');
	        }
        	_out.write(Integer.toString(bytes[lenMinus1]));
		}
		else if (char[].class.equals(arrayType))
		{
			writeJsonUtf8String(new String((char[]) array));
		}
		else if (short[].class.equals(arrayType))
		{
			short[] shorts = (short[]) array;
	        for (int i=0; i < lenMinus1; i++)
	        {
	        	_out.write(Integer.toString(shorts[i]));
                _out.write(',');
	        }
        	_out.write(Integer.toString(shorts[lenMinus1]));			
		}
		else if (int[].class.equals(arrayType))
		{
			int[] ints = (int[]) array;
	        for (int i=0; i < lenMinus1; i++)
	        {
	        	_out.write(Integer.toString(ints[i]));
                _out.write(',');
	        }
        	_out.write(Integer.toString(ints[lenMinus1]));			
		}
		else if (long[].class.equals(arrayType))
		{
			long[] longs = (long[]) array;
	        for (int i=0; i < lenMinus1; i++)
	        {
	        	_out.write(Long.toString(longs[i]));
                _out.write(',');
	        }
        	_out.write(Long.toString(longs[lenMinus1]));			
		}
		else if (float[].class.equals(arrayType))
		{
			float[] floats = (float[]) array;
	        for (int i=0; i < lenMinus1; i++)
	        {
	        	_out.write(Double.toString(floats[i]));
                _out.write(',');
	        }
        	_out.write(Float.toString(floats[lenMinus1]));			
		}
		else if (double[].class.equals(arrayType))
		{
			double[] dubs = (double[]) array;
	        for (int i=0; i < lenMinus1; i++)
	        {
	        	_out.write(Double.toString(dubs[i]));
                _out.write(',');
	        }
        	_out.write(Double.toString(dubs[lenMinus1]));			
		}
		else if (boolean[].class.equals(arrayType))
		{
			boolean[] bools = (boolean[]) array;
	        for (int i=0; i < lenMinus1; i++)
	        {
	        	_out.write(bools[i] ? "true," : "false,");
	        }
        	_out.write(Boolean.toString(bools[lenMinus1]));			
		}
		else
		{					
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
	                writeJsonUtf8String((String) value);
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
	                    writeJsonUtf8String((String) value);
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
	            {	// Specific Class-type arrays - only force type when 
	            	// the instance is derived from array base class.
	                boolean forceType = !value.getClass().equals(componentClass);
	                writeImpl(value, forceType);
	            }
	
	            if (i != lenMinus1)
	            {
	                _out.write(',');
	            }
	        }
		}
		
		_out.write(']');
		if (typeWritten || referenced)
		{
			_out.write('}');
		}
	}

    /**
     * String, Date, and Class have been written before calling this method,
     * strictly for performance.
     */
	private void writeObject(Object obj, boolean showType) throws IOException
	{
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

        ClassMeta classInfo = getDeepDeclaredFields(obj.getClass(), _classMeta);
        if (classInfo._writeMethod != null)
        {   // Must show type when class has custom _writeJson() method on it.
            // The JsonReader uses this to know it is dealing with an object
            // that has custom json io methods on it.
            showType = true;
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

        if (classInfo._writeMethod == null)
        {
            for (Field field : classInfo.values())
            {
                if (first)
                {
                    first = false;
                }
                else
                {
                    _out.write(',');
                }
                writeJsonUtf8String(field.getName());
                _out.write(':');

                Object o;
                try
                {
                    o = field.get(obj);
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

                Class type = field.getType();
                boolean forceType = o.getClass() != type;     // If types are not exactly the same, write "@type" field

                if (o instanceof String)
                {
                    writeJsonUtf8String((String) o);
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
        }
        else
        {   // Invoke custom _writeJson() method.
            _out.write(',');
            try
            {
                classInfo._writeMethod.invoke(obj, _out);
            }
            catch (Exception e)
            {
                throw new IOException("Error invoking " + obj.getClass() + "._jsonWrite()", e);
            }
        }

		_out.write('}');
	}

	private static boolean isPrimitiveWrapper(Class c)
	{
        return c.isPrimitive() || JsonReader._prims.contains(c);
	}

	/**
	 * Write out special characters "\b, \f, \t, \n, \r", as such, backslash as \\
	 * quote as \" and values less than an ASCII space (20hex) as "\\u00xx" format,
	 * characters in the range of ASCII space to a '~' as ASCII, and anything higher in UTF-8.
	 */
	private void writeJsonUtf8String(String s) throws IOException
	{
        _out.write('\"');
		int len = s.length();

		for (int i=0; i < len; i++)
		{
			char c = s.charAt(i);

			if (c < ' ')
			{	// Anything less than ASCII space, write either in \\u00xx form, or the special \t, \n, etc. form
				if (c == '\b')
				{
					_out.write("\\b");
				}
				else if (c == '\t')
				{
					_out.write("\\t");
				}
				else if (c == '\n')
				{
					_out.write("\\n");
				}
				else if (c == '\f')
				{
					_out.write("\\f");
				}
				else if (c == '\r')
				{
					_out.write("\\r");
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
			else if (c == '\\' || c == '"')
			{
				_out.write('\\');
	            _out.write(c);				
			}
			else
			{	// Anything else - write in UTF-8 form (multi-byte encoded) (OutputStreamWriter is UTF-8)
				_out.write(c);
			}
		}
        _out.write('\"');
	}

    /**
     * @param c Class instance
     * @return ClassMeta which contains fields of class and customer write/read
     * methods if they exist. The results are cached internally for performance
     * when called again with same Class.
     */
    static ClassMeta getDeepDeclaredFields(Class c, Map<String, ClassMeta> classMeta)
    {
        ClassMeta classInfo = classMeta.get(c.getName());
        if (classInfo != null)
        {
            return classInfo;
        }

        classInfo = new ClassMeta();
        Class curr = c;

        while (curr != null)
        {
            try
            {
                Field[] local = curr.getDeclaredFields();

                for (Field field : local)
                {
                    if (!field.isAccessible())
                    {
                        try
                        {
                            field.setAccessible(true);
                        }
                        catch (Exception ignored) { }
                    }

                    if ((field.getModifiers() & Modifier.STATIC) == 0)
                    {    // speed up: do not process static fields.
                        classInfo.put(field.getName(), field);
                    }
                }
            }
            catch (ThreadDeath t)
            {
                throw t;
            }
            catch (Throwable ignored)
            { }

            curr = curr.getSuperclass();
        }

        try
        {
            classInfo._writeMethod = c.getDeclaredMethod("_writeJson", new Class[] {Writer.class});
        }
        catch (Exception ignored)
        { }

        try
        {
            classInfo._readMethod = c.getDeclaredMethod("_readJson", new Class[] {Map.class});
        }
        catch (Exception ignored)
        { }

        classMeta.put(c.getName(), classInfo);
        return classInfo;
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
        { }
	}

	public void close()
	{
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

    /**
     * Convert a Java Object to a JSON String.  This is the
     * easy-to-use API - it returns null if there was an error.
     * @return String containing JSON representation of passed
     * in object, or null if an error occurred.
     */
    public static String toJson(Object item)
    {
        try
        {
            return objectToJson(item);
        }
        catch(IOException ignored)
        {
            return null;
        }
    }

    /**
     * Convert a Java Object to a JSON String.
     * @return String containing JSON representation of passed
     * in object.
     * @throws java.io.IOException If an I/O error occurs
     */
    public static String objectToJson(Object item) throws IOException
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        JsonWriter writer = new JsonWriter(stream);
        writer.write(item);
        writer.close();
        return new String(stream.toByteArray(), "UTF-8");
    }    
}