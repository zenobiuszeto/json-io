package com.cedarsoftware.util.io;

import junit.framework.TestCase;

import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Test cases for JsonReader / JsonWriter
 *
 * @author John DeRegnaucourt (jdereg@gmail.com)
 *         <br/>
 *         Copyright [2010] John DeRegnaucourt
 *         <br/><br/>
 *         Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *         <br/><br/>
 *         http://www.apache.org/licenses/LICENSE-2.0
 *         <br/><br/>
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 */
public class TestJsonReaderWriter extends TestCase
{
    public static boolean _debug = true;    // Change to 'false' to "hush" output
    public static Date _testDate = new Date();
    public static Character _CONST_CHAR = new Character('j');
    public static Byte _CONST_BYTE = new Byte((byte) 16);
    public static Short _CONST_SHORT = new Short((short) 26);
    public static Integer _CONST_INT = new Integer(36);
    public static Long _CONST_LONG = new Long(46);
    public static Float _CONST_FLOAT = new Float(56.56);
    public static Double _CONST_DOUBLE = new Double(66.66);
    private long _startWrite;
    private long _endWrite;
    private long _startRead;
    private long _endRead;

    public static class TestObject implements Comparable, Serializable
    {
        protected String _name;
        private TestObject _other;

        public TestObject(String name)
        {
            _name = name;
        }

        public int compareTo(Object that)
        {
            if (!(that instanceof TestObject))
            {
                return 1;
            }
            return _name.compareTo(((TestObject) that)._name);
        }

        public int hashCode()
        {
            return _name == null ? 0 : _name.hashCode();
        }

        public boolean equals(Object that)
        {
            if (that == null)
            {
                return false;
            }
            return that instanceof TestObject && _name.equals(((TestObject) that)._name);
        }

        public String toString()
        {
            return "name=" + _name;
        }

        public String getName()
        {
            return _name;
        }
    }

    private static class TestObjectKid extends TestObject implements Serializable
    {
        private String _email;

        TestObjectKid(String name, String email)
        {
            super(name);
            _email = email;
        }
    }

    private static class TestJsonNoDefaultOrPublicConstructor
    {
        private final String _str;
        private final Date _date;
        private final byte _byte;
        private final short _short;
        private final int _int;
        private final long _long;
        private final float _float;
        private final double _double;
        private final boolean _boolean;
        private final char _char;
        private final String[] _strings;
        private final int[] _ints;

        private TestJsonNoDefaultOrPublicConstructor(String string, Date date, byte b, short s, int i, long l, float f, double d, boolean bool, char c, String[] strings, int[] ints)
        {
            _str = string;
            _date = date;
            _byte = b;
            _short = s;
            _int = i;
            _long = l;
            _float = f;
            _double = d;
            _boolean = bool;
            _char = c;
            _strings = strings;
            _ints = ints;
        }

        public String getString()
        {
            return _str;
        }

        public Date getDate()
        {
            return _date;
        }

        public byte getByte()
        {
            return _byte;
        }

        public short getShort()
        {
            return _short;
        }

        public int getInt()
        {
            return _int;
        }

        public long getLong()
        {
            return _long;
        }

        public float getFloat()
        {
            return _float;
        }

        public double getDouble()
        {
            return _double;
        }

        public boolean getBoolean()
        {
            return _boolean;
        }

        public char getChar()
        {
            return _char;
        }

        public String[] getStrings()
        {
            return _strings;
        }

        public int[] getInts()
        {
            return _ints;
        }
    }

    private static class Empty implements Serializable
    {
        public static double multiple(double x, double y)
        {
            return x * y;
        }

        public boolean equals(Object other)
        {
            return other != null && other instanceof Empty;
        }
    }

    public static class TestArray implements Serializable
    {
        private Empty _empty_a;
        private Empty _empty_b;
        private Empty[] _empty_c;
        private Empty[] _empty_d;
        private Empty[][] _empty_e;
        private Empty[][] _empty_f;

        private boolean[] _booleans_a;
        private boolean[] _booleans_b;
        private boolean[] _booleans_c;
        private Boolean[] _booleans_d;
        private Boolean[] _booleans_e;
        private Boolean[] _booleans_f;
        private Boolean[] _booleans_g;
        private boolean[][] _booleans_h;
        private boolean[][] _booleans_i;
        private boolean[][] _booleans_j;

        private char[] _chars_a;
        private char[] _chars_b;
        private char[] _chars_c;
        private Character[] _chars_d;
        private Character[] _chars_e;
        private Character[] _chars_f;
        private char[][] _chars_g;
        private Character[][] _chars_h;

        private byte[] _bytes_a;
        private byte[] _bytes_b;
        private byte[] _bytes_c;
        private Byte[] _bytes_d;
        private Byte[] _bytes_e;
        private Byte[] _bytes_f;
        private byte[][] _bytes_g;
        private Byte[][] _bytes_h;
        private byte[] _bytes_i;

        private short[] _shorts_a;
        private short[] _shorts_b;
        private short[] _shorts_c;
        private Short[] _shorts_d;
        private Short[] _shorts_e;
        private Short[] _shorts_f;
        private short[][] _shorts_g;
        private Short[][] _shorts_h;

        private int[] _ints_a;
        private int[] _ints_b;
        private int[] _ints_c;
        private int[][] _int_1;
        private Integer[] _ints_d;
        private Integer[] _ints_e;
        private Integer[] _ints_f;
        private Integer[][] _ints_g;

        private long[] _longs_a;
        private long[] _longs_b;
        private long[] _longs_c;
        private long[][][] _longs_1;
        private Long[] _longs_d;
        private Long[] _longs_e;
        private Long[] _longs_f;
        private Long[][] _longs_g;

        private float[] _floats_a;
        private float[] _floats_b;
        private float[] _floats_c;
        private Float[] _floats_d;
        private Float[] _floats_e;
        private Float[] _floats_f;
        private float[][] _floats_g;
        private Float[][] _floats_h;

        private double[] _doubles_a;
        private double[] _doubles_b;
        private double[] _doubles_c;
        private Double[] _doubles_d;
        private Double[] _doubles_e;
        private Double[] _doubles_f;
        private double[][] _doubles_g;
        private Double[][] _doubles_h;

        private String[] _strings_a;
        private String[][] _strings_b;

        private Date[] _dates_a;
        private Date[][] _dates_b;

        private Class[] _classes_a;
        private Class[][] _classes_b;

        private StringBuffer _stringbuffer_a;
        private StringBuffer[] _stringbuffer_b;
        private StringBuffer[][] _stringbuffer_c;

        private TestObject _testobj_a;
        private TestObject[] _testobj_b;
        private TestObject[][] _testobj_c;

        private Object[] _test_a;
        private Object[] _test_b;
        private Object[] _hetero_a;
        private Object[] _testRefs0;
        private Object[] _testRefs1;

        private Object _arrayO;
        private Object _arrayS;
        private Object _arrayArrayO;

        public TestArray()
        {
        }

        public void init()
        {
            _empty_a = new Empty();
            _empty_b = null;
            _empty_c = new Empty[]{};
            _empty_d = new Empty[]{new Empty(), null};
            _empty_e = new Empty[][]{{}};
            _empty_f = new Empty[][]{{new Empty(), null}, null, {}, {new Empty()}};

            _booleans_a = new boolean[]{true, false, true};
            _booleans_b = new boolean[]{};
            _booleans_c = null;
            _booleans_d = new Boolean[]{Boolean.TRUE, Boolean.FALSE, null};
            _booleans_e = new Boolean[]{};
            _booleans_f = null;
            _booleans_g = new Boolean[]{null};
            _booleans_h = new boolean[][]{{true}, {true, false}, {true, false, true}, null, {}};
            _booleans_i = null;
            _booleans_j = new boolean[][]{null};

            _chars_a = new char[]{'a', '\t', '\u0005'};
            _chars_b = new char[]{};
            _chars_c = null;
            _chars_d = new Character[]{new Character('a'), new Character('\t'), new Character('\u0006')};
            _chars_e = new Character[]{};
            _chars_f = null;
            _chars_g = new char[][]{{'a', '\t', '\u0004'}, null, {}};
            _chars_h = new Character[][]{{new Character('a'), new Character('\t'), new Character('\u0004')}, null, {}};

            _bytes_a = new byte[]{Byte.MIN_VALUE, -1, 0, 1, Byte.MAX_VALUE};
            _bytes_b = new byte[]{};
            _bytes_c = null;
            _bytes_d = new Byte[]{new Byte(Byte.MIN_VALUE), new Byte((byte) -1), new Byte((byte) 0), new Byte((byte) 1), new Byte(Byte.MAX_VALUE)};
            _bytes_e = new Byte[]{};
            _bytes_f = null;
            _bytes_g = new byte[][]{null, {}, {Byte.MAX_VALUE}};
            _bytes_h = new Byte[][]{null, {}, {new Byte(Byte.MAX_VALUE)}};
            _bytes_i = new byte[]{16};

            _shorts_a = new short[]{Short.MIN_VALUE, -1, 0, 1, Short.MAX_VALUE};
            _shorts_b = new short[]{};
            _shorts_c = null;
            _shorts_d = new Short[]{new Short(Short.MIN_VALUE), new Short((short) -1), new Short((short) 0), new Short((short) 1), new Short(Short.MAX_VALUE)};
            _shorts_e = new Short[]{};
            _shorts_f = null;
            _shorts_g = new short[][]{null, {}, {Short.MAX_VALUE}};
            _shorts_h = new Short[][]{null, {}, {new Short(Short.MAX_VALUE)}};

            _ints_a = new int[]{Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE};
            _ints_b = new int[]{};
            _ints_c = null;
            _int_1 = new int[][]{{Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE}, null, {-1, 0, 1, 2}};
            _ints_d = new Integer[]{new Integer(Integer.MIN_VALUE), new Integer(-1), new Integer(0), new Integer(1), new Integer(Integer.MAX_VALUE)};
            _ints_e = new Integer[]{};
            _ints_f = null;
            _ints_g = new Integer[][]{null, {}, {new Integer(Integer.MAX_VALUE)}};

            _longs_a = new long[]{Long.MIN_VALUE, -1, 0, 1, Long.MAX_VALUE};
            _longs_b = new long[]{};
            _longs_c = null;
            _longs_1 = new long[][][]{{{-1}, {0, 1}, {1, 2, 3}}, {}, {{1, 2}}, {{}}};
            _longs_d = new Long[]{new Long(Long.MIN_VALUE), new Long(-1), new Long(0), new Long(1), new Long(Long.MAX_VALUE)};
            _longs_e = new Long[]{};
            _longs_f = null;
            _longs_g = new Long[][]{null, {}, {new Long(Long.MAX_VALUE)}};

            _floats_a = new float[]{0.0f, Float.MIN_VALUE, Float.MAX_VALUE, -1.0f};
            _floats_b = new float[]{};
            _floats_c = null;
            _floats_d = new Float[]{new Float(0.0f), new Float(Float.MIN_VALUE), new Float(Float.MAX_VALUE), new Float(-1.0f), null};
            _floats_e = new Float[]{};
            _floats_f = null;
            _floats_g = new float[][]{null, {}, {Float.MAX_VALUE}};
            _floats_h = new Float[][]{null, {}, {new Float(Float.MAX_VALUE)}};

            _doubles_a = new double[]{0.0, Double.MIN_VALUE, Double.MAX_VALUE, -1.0};
            _doubles_b = new double[]{};
            _doubles_c = null;
            _doubles_d = new Double[]{new Double(0.0), new Double(Double.MIN_VALUE), new Double(Double.MAX_VALUE), new Double(-1.0), null};
            _doubles_e = new Double[]{};
            _doubles_f = null;
            _doubles_g = new double[][]{null, {}, {Double.MAX_VALUE}};
            _doubles_h = new Double[][]{null, {}, {new Double(Double.MAX_VALUE)}};

            _strings_a = new String[]{null, "\u0007", "\t\rfood\n\f", "null"};
            _strings_b = new String[][]{{"alpha", "bravo", "charlie"}, {null, "\u0007", "\t", "null"}, null, {}};

            _dates_a = new Date[]{new Date(0), _testDate, null};
            _dates_b = new Date[][]{null, {}, {_testDate}};

            _classes_a = new Class[]{boolean.class, char.class, byte.class, short.class, int.class, long.class, float.class, double.class, null, String.class};
            _classes_b = new Class[][]{null, {}, {Date.class}};

            _stringbuffer_a = new StringBuffer("food");
            _stringbuffer_b = new StringBuffer[3];
            _stringbuffer_b[0] = new StringBuffer("first");
            _stringbuffer_b[1] = new StringBuffer("second");
            _stringbuffer_b[2] = null;
            _stringbuffer_c = new StringBuffer[][]{null, {}, {new StringBuffer("sham-wow")}};

            _testobj_a = new TestObject("food");
            _testobj_b = new TestObject[]{new TestObject("ten"), new TestObject("hut")};
            _testobj_c = new TestObject[][]{null, {}, {new TestObject("mighty-mend")}};

            _test_a = new Object[1];
            _test_b = new Object[1];
            _test_a[0] = _test_b;
            _test_b[0] = _test_a;

            _hetero_a = new Object[]{new Character('a'), Boolean.TRUE, new Byte((byte) 9), new Short((short) 9), new Integer(9), new Long(9), new Float(9.9), new Double(9.9), "getStartupInfo", _testDate, boolean.class, null, "null", _CONST_INT, Class.class};
            _testRefs0 = new Object[]{_testDate, Boolean.FALSE, _CONST_CHAR, _CONST_BYTE, _CONST_SHORT, _CONST_INT, _CONST_LONG, _CONST_FLOAT, _CONST_DOUBLE, "Happy"};
            _testRefs1 = new Object[]{_testDate, Boolean.FALSE, _CONST_CHAR, _CONST_BYTE, _CONST_SHORT, _CONST_INT, _CONST_LONG, _CONST_FLOAT, _CONST_DOUBLE, "Happy"};
            _arrayO = new Object[]{"foo", true, null, 16L, 3.14};
            _arrayS = new String[]{"fingers", "toes"};
            _arrayArrayO = new Object[][]{{"true", "false"}, {1L, 2L, 3L}, null, {1.1, 2.2}, {true, false}};
        }
    }

    public void testArray() throws Exception
    {
        println("\nTestJsonWriter.testArray()");
        TestArray obj = new TestArray();
        obj.init();
        String jsonOut = getJsonString(obj);
        println(jsonOut);

        JsonReader jr = new JsonReader(convertStringToInputStream(jsonOut));
        TestArray root = (TestArray) readJsonObject(jr);

        assertTrue(root._empty_a != null);
        assertNull(root._empty_b);
        assertTrue(root._empty_c.length == 0);
        assertTrue(root._empty_d.length == 2);
        assertTrue(root._empty_d[0].equals(new Empty()));
        assertNull(root._empty_d[1]);
        assertTrue(root._empty_e.length == 1);
        assertTrue(root._empty_e[0].length == 0);
        assertTrue(root._empty_f.length == 4);
        assertTrue(root._empty_f[0].length == 2);
        assertTrue(root._empty_f[0][0].equals(new Empty()));
        assertNull(root._empty_f[0][1]);
        assertNull(root._empty_f[1]);
        assertTrue(root._empty_f[2].length == 0);
        assertTrue(root._empty_f[3].length == 1);

        assertTrue(root._booleans_a.getClass().equals(boolean[].class));
        assertTrue(root._booleans_a.length == 3);
        assertTrue(root._booleans_a[0]);
        assertFalse(root._booleans_a[1]);
        assertTrue(root._booleans_a[2]);
        assertTrue(root._booleans_b.length == 0);
        assertNull(root._booleans_c);
        assertTrue(root._booleans_d.length == 3);
        assertTrue(root._booleans_d[0].booleanValue());
        assertFalse(root._booleans_d[1].booleanValue());
        assertNull(root._booleans_d[2]);
        assertTrue(root._booleans_e.length == 0);
        assertNull(root._booleans_f);
        assertTrue(root._booleans_g.length == 1);
        assertNull(root._booleans_g[0]);
        assertTrue(root._booleans_h.length == 5);
        assertTrue(root._booleans_h[0].length == 1);
        assertTrue(root._booleans_h[1].length == 2);
        assertTrue(root._booleans_h[2].length == 3);
        assertTrue(root._booleans_h[0][0]);
        assertTrue(root._booleans_h[1][0]);
        assertFalse(root._booleans_h[1][1]);
        assertTrue(root._booleans_h[2][0]);
        assertFalse(root._booleans_h[2][1]);
        assertTrue(root._booleans_h[2][2]);
        assertNull(root._booleans_h[3]);
        assertTrue(root._booleans_h[4].length == 0);
        assertNull(root._booleans_i);
        assertTrue(root._booleans_j.length == 1);
        assertNull(root._booleans_j[0]);

        assertTrue(root._chars_a[0] == 'a');
        assertTrue(root._chars_a[1] == '\t');
        assertTrue(root._chars_a[2] == '\u0005');
        assertTrue(root._chars_b.length == 0);
        assertNull(root._chars_c);
        assertTrue(root._chars_d[0].charValue() == 'a');
        assertTrue(root._chars_d[1].charValue() == '\t');
        assertTrue(root._chars_d[2].charValue() == '\u0006');
        assertTrue(root._chars_e.length == 0);
        assertNull(root._chars_f);

        assertTrue(root._bytes_a[0] == Byte.MIN_VALUE);
        assertTrue(root._bytes_a[1] == -1);
        assertTrue(root._bytes_a[2] == 0);
        assertTrue(root._bytes_a[3] == 1);
        assertTrue(root._bytes_a[4] == Byte.MAX_VALUE);
        assertTrue(root._bytes_b.length == 0);
        assertNull(root._bytes_c);
        assertTrue(root._bytes_d[0].byteValue() == Byte.MIN_VALUE);
        assertTrue(root._bytes_d[1].byteValue() == -1);
        assertTrue(root._bytes_d[2].byteValue() == 0);
        assertTrue(root._bytes_d[3].byteValue() == 1);
        assertTrue(root._bytes_d[4].byteValue() == Byte.MAX_VALUE);
        assertTrue(root._bytes_e.length == 0);
        assertNull(root._bytes_f);
        assertTrue(root._bytes_g.length == 3);
        assertNull(root._bytes_g[0]);
        assertTrue(root._bytes_g[1].length == 0);
        assertTrue(root._bytes_g[2].length == 1);
        assertTrue(root._bytes_g[2][0] == Byte.MAX_VALUE);
        assertTrue(root._bytes_h.length == 3);
        assertNull(root._bytes_h[0]);
        assertTrue(root._bytes_h[1].length == 0);
        assertTrue(root._bytes_h[2].length == 1);
        assertTrue(root._bytes_h[2][0].byteValue() == Byte.MAX_VALUE);
        assertTrue(root._bytes_i[0] == 16);

        assertTrue(root._chars_g.length == 3);
        assertTrue(root._chars_g[0].length == 3);
        assertTrue(root._chars_g[0][0] == 'a');
        assertTrue(root._chars_g[0][1] == '\t');
        assertTrue(root._chars_g[0][2] == '\u0004');
        assertNull(root._chars_g[1]);
        assertTrue(root._chars_g[2].length == 0);
        assertTrue(root._chars_h.length == 3);
        assertTrue(root._chars_h[0].length == 3);
        assertTrue(root._chars_h[0][0].equals(new Character('a')));
        assertTrue(root._chars_h[0][1].equals(new Character('\t')));
        assertTrue(root._chars_h[0][2].equals(new Character('\u0004')));
        assertNull(root._chars_h[1]);
        assertTrue(root._chars_h[2].length == 0);

        assertTrue(root._shorts_a[0] == Short.MIN_VALUE);
        assertTrue(root._shorts_a[1] == -1);
        assertTrue(root._shorts_a[2] == 0);
        assertTrue(root._shorts_a[3] == 1);
        assertTrue(root._shorts_a[4] == Short.MAX_VALUE);
        assertTrue(root._shorts_b.length == 0);
        assertNull(root._shorts_c);
        assertTrue(root._shorts_d[0].shortValue() == Short.MIN_VALUE);
        assertTrue(root._shorts_d[1].shortValue() == -1);
        assertTrue(root._shorts_d[2].shortValue() == 0);
        assertTrue(root._shorts_d[3].shortValue() == 1);
        assertTrue(root._shorts_d[4].shortValue() == Short.MAX_VALUE);
        assertTrue(root._shorts_e.length == 0);
        assertNull(root._shorts_f);
        assertTrue(root._shorts_g.length == 3);
        assertNull(root._shorts_g[0]);
        assertTrue(root._shorts_g[1].length == 0);
        assertTrue(root._shorts_g[2].length == 1);
        assertTrue(root._shorts_g[2][0] == Short.MAX_VALUE);
        assertTrue(root._shorts_h.length == 3);
        assertNull(root._shorts_h[0]);
        assertTrue(root._shorts_h[1].length == 0);
        assertTrue(root._shorts_h[2].length == 1);
        assertTrue(root._shorts_h[2][0].shortValue() == Short.MAX_VALUE);

        assertTrue(root._ints_a[0] == Integer.MIN_VALUE);
        assertTrue(root._ints_a[1] == -1);
        assertTrue(root._ints_a[2] == 0);
        assertTrue(root._ints_a[3] == 1);
        assertTrue(root._ints_a[4] == Integer.MAX_VALUE);
        assertTrue(root._ints_b.length == 0);
        assertNull(root._ints_c);
        assertTrue(root._ints_d[0].intValue() == Integer.MIN_VALUE);
        assertTrue(root._ints_d[1].intValue() == -1);
        assertTrue(root._ints_d[2].intValue() == 0);
        assertTrue(root._ints_d[3].intValue() == 1);
        assertTrue(root._ints_d[4].intValue() == Integer.MAX_VALUE);
        assertTrue(root._ints_e.length == 0);
        assertNull(root._ints_f);
        assertTrue(root._int_1.length == 3);
        assertTrue(root._int_1[0].length == 5);
        assertTrue(root._int_1[0][0] == Integer.MIN_VALUE);
        assertTrue(root._int_1[0][1] == -1);
        assertTrue(root._int_1[0][2] == 0);
        assertTrue(root._int_1[0][3] == 1);
        assertTrue(root._int_1[0][4] == Integer.MAX_VALUE);
        assertNull(root._int_1[1]);
        assertTrue(root._int_1[2].length == 4);
        assertTrue(root._int_1[2][0] == -1);
        assertTrue(root._int_1[2][1] == 0);
        assertTrue(root._int_1[2][2] == 1);
        assertTrue(root._int_1[2][3] == 2);
        assertTrue(root._ints_g.length == 3);
        assertNull(root._ints_g[0]);
        assertTrue(root._ints_g[1].length == 0);
        assertTrue(root._ints_g[2].length == 1);
        assertTrue(root._ints_g[2][0].intValue() == Integer.MAX_VALUE);

        assertTrue(root._longs_a[0] == Long.MIN_VALUE);
        assertTrue(root._longs_a[1] == -1);
        assertTrue(root._longs_a[2] == 0);
        assertTrue(root._longs_a[3] == 1);
        assertTrue(root._longs_a[4] == Long.MAX_VALUE);
        assertTrue(root._longs_b.length == 0);
        assertNull(root._longs_c);
        assertTrue(root._longs_d[0].longValue() == Long.MIN_VALUE);
        assertTrue(root._longs_d[1].longValue() == -1);
        assertTrue(root._longs_d[2].longValue() == 0);
        assertTrue(root._longs_d[3].longValue() == 1);
        assertTrue(root._longs_d[4].longValue() == Long.MAX_VALUE);
        assertTrue(root._longs_e.length == 0);
        assertNull(root._longs_f);
        assertTrue(root._longs_1.length == 4);
        assertTrue(root._longs_1[0].length == 3);
        assertTrue(root._longs_1[1].length == 0);
        assertTrue(root._longs_1[2].length == 1);
        assertTrue(root._longs_1[3].length == 1);
        assertTrue(root._ints_g.length == 3);
        assertNull(root._longs_g[0]);
        assertTrue(root._longs_g[1].length == 0);
        assertTrue(root._longs_g[2].length == 1);
        assertTrue(root._longs_g[2][0].longValue() == Long.MAX_VALUE);

        assertTrue(root._floats_a.length == 4);
        assertTrue(root._floats_a[0] == 0.0f);
        assertTrue(root._floats_a[1] == Float.MIN_VALUE);
        assertTrue(root._floats_a[2] == Float.MAX_VALUE);
        assertTrue(root._floats_a[3] == -1.0f);
        assertTrue(root._floats_b.length == 0);
        assertNull(root._floats_c);
        assertTrue(root._floats_d.length == 5);
        assertTrue(root._floats_d[0].equals(new Float(0.0f)));
        assertTrue(root._floats_d[1].equals(new Float(Float.MIN_VALUE)));
        assertTrue(root._floats_d[2].equals(new Float(Float.MAX_VALUE)));
        assertTrue(root._floats_d[3].equals(new Float(-1.0f)));
        assertNull(root._floats_d[4]);
        assertTrue(root._floats_e.length == 0);
        assertNull(root._floats_f);
        assertNull(root._floats_g[0]);
        assertTrue(root._floats_g[1].length == 0);
        assertTrue(root._floats_g[2].length == 1);
        assertTrue(root._floats_g[2][0] == Float.MAX_VALUE);
        assertNull(root._floats_h[0]);
        assertTrue(root._floats_h[1].length == 0);
        assertTrue(root._floats_h[2].length == 1);
        assertTrue(root._floats_h[2][0].floatValue() == Float.MAX_VALUE);

        assertTrue(root._doubles_a.length == 4);
        assertTrue(root._doubles_a[0] == 0.0);
        assertTrue(root._doubles_a[1] == Double.MIN_VALUE);
        assertTrue(root._doubles_a[2] == Double.MAX_VALUE);
        assertTrue(root._doubles_a[3] == -1.0);
        assertTrue(root._doubles_b.length == 0);
        assertNull(root._doubles_c);
        assertTrue(root._doubles_d.length == 5);
        assertTrue(root._doubles_d[0].equals(new Double(0.0)));
        assertTrue(root._doubles_d[1].equals(new Double(Double.MIN_VALUE)));
        assertTrue(root._doubles_d[2].equals(new Double(Double.MAX_VALUE)));
        assertTrue(root._doubles_d[3].equals(new Double(-1.0)));
        assertNull(root._doubles_d[4]);
        assertTrue(root._doubles_e.length == 0);
        assertNull(root._doubles_f);
        assertNull(root._doubles_g[0]);
        assertTrue(root._doubles_g[1].length == 0);
        assertTrue(root._doubles_g[2].length == 1);
        assertTrue(root._doubles_g[2][0] == Double.MAX_VALUE);
        assertNull(root._doubles_h[0]);
        assertTrue(root._doubles_h[1].length == 0);
        assertTrue(root._doubles_h[2].length == 1);
        assertTrue(root._doubles_h[2][0].doubleValue() == Double.MAX_VALUE);

        assertNull(root._strings_a[0]);
        assertTrue(root._strings_a[1].equals("\u0007"));
        assertTrue(root._strings_a[2].equals("\t\rfood\n\f"));
        assertTrue(root._strings_a[3].equals("null"));
        assertTrue(root._strings_b.length == 4);
        assertTrue(root._strings_b[0].length == 3);
        assertTrue(root._strings_b[0][0].equals("alpha"));
        assertTrue(root._strings_b[0][1].equals("bravo"));
        assertTrue(root._strings_b[0][2].equals("charlie"));
        assertTrue(root._strings_b[1].length == 4);
        assertNull(root._strings_b[1][0]);
        assertTrue(root._strings_b[1][1].equals("\u0007"));
        assertTrue(root._strings_b[1][2].equals("\t"));
        assertTrue(root._strings_b[1][3].equals("null"));
        assertNull(root._strings_b[2]);
        assertTrue(root._strings_b[3].length == 0);

        assertTrue(root._dates_a[0].equals(new Date(0)));
        assertTrue(root._dates_a[1].equals(_testDate));
        assertNull(root._dates_a[2]);
        assertNull(root._dates_b[0]);
        assertTrue(root._dates_b[1].length == 0);
        assertTrue(root._dates_b[2].length == 1);
        assertTrue(root._dates_b[2][0].equals(_testDate));

        assertTrue(root._classes_a.length == 10);
        assertTrue(root._classes_a[0].equals(boolean.class));
        assertTrue(root._classes_a[1].equals(char.class));
        assertTrue(root._classes_a[2].equals(byte.class));
        assertTrue(root._classes_a[3].equals(short.class));
        assertTrue(root._classes_a[4].equals(int.class));
        assertTrue(root._classes_a[5].equals(long.class));
        assertTrue(root._classes_a[6].equals(float.class));
        assertTrue(root._classes_a[7].equals(double.class));
        assertNull(root._classes_a[8]);
        assertTrue(root._classes_a[9].equals(String.class));
        assertNull(root._classes_b[0]);
        assertTrue(root._classes_b[1].length == 0);
        assertTrue(root._classes_b[2].length == 1);
        assertTrue(root._classes_b[2][0].equals(Date.class));

        assertTrue(root._stringbuffer_a.toString().equals("food"));
        assertTrue(root._stringbuffer_b.length == 3);
        assertTrue(root._stringbuffer_b[0].toString().equals("first"));
        assertTrue(root._stringbuffer_b[1].toString().equals("second"));
        assertNull(root._stringbuffer_b[2]);
        assertTrue(root._stringbuffer_c.length == 3);
        assertNull(root._stringbuffer_c[0]);
        assertTrue(root._stringbuffer_c[1].length == 0);
        assertTrue(root._stringbuffer_c[2].length == 1);
        assertTrue(root._stringbuffer_c[2][0].toString().equals("sham-wow"));

        assertTrue(root._testobj_a.equals(new TestObject("food")));
        assertTrue(root._testobj_b.length == 2);
        assertTrue(root._testobj_b[0].getName().equals("ten"));
        assertTrue(root._testobj_b[1].getName().equals("hut"));
        assertTrue(root._testobj_c.length == 3);
        assertNull(root._testobj_c[0]);
        assertTrue(root._testobj_c[1].length == 0);
        assertTrue(root._testobj_c[2].length == 1);
        assertTrue(root._testobj_c[2][0].equals(new TestObject("mighty-mend")));

        assertTrue(root._test_a.length == 1);
        assertTrue(root._test_b.length == 1);
        assertTrue(root._test_a[0] == root._test_b);
        assertTrue(root._test_b[0] == root._test_a);

        assertTrue(root._hetero_a.length == 15);
        assertTrue(root._hetero_a[0].equals(new Character('a')));
        assertTrue(root._hetero_a[1].equals(Boolean.TRUE));
        assertTrue(root._hetero_a[2].equals(new Byte((byte) 9)));
        assertTrue(root._hetero_a[3].equals(new Short((short) 9)));
        assertTrue(root._hetero_a[4].equals(new Integer(9)));
        assertTrue(root._hetero_a[5].equals(new Long(9)));
        assertTrue(root._hetero_a[6].equals(new Float(9.9)));
        assertTrue(root._hetero_a[7].equals(new Double(9.9)));
        assertTrue(root._hetero_a[8].equals("getStartupInfo"));
        assertTrue(root._hetero_a[9].equals(_testDate));
        assertTrue(root._hetero_a[10].equals(boolean.class));
        assertNull(root._hetero_a[11]);
        assertTrue(root._hetero_a[12].equals("null"));
        assertTrue(root._hetero_a[13].equals(_CONST_INT));
        assertTrue(root._hetero_a[14].equals(Class.class));

        assertTrue(root._testRefs0.length == 10);
        assertTrue(root._testRefs0[0].equals(_testDate));
        assertTrue(root._testRefs0[1].equals(Boolean.FALSE));
        assertTrue(root._testRefs0[2].equals(_CONST_CHAR));
        assertTrue(root._testRefs0[3].equals(_CONST_BYTE));
        assertTrue(root._testRefs0[4].equals(_CONST_SHORT));
        assertTrue(root._testRefs0[5].equals(_CONST_INT));
        assertTrue(root._testRefs0[6].equals(_CONST_LONG));
        assertTrue(root._testRefs0[7].equals(_CONST_FLOAT));
        assertTrue(root._testRefs0[8].equals(_CONST_DOUBLE));
        assertTrue(root._testRefs0[9].equals("Happy"));

        assertTrue(root._testRefs1.length == 10);
        assertFalse(addressEquals(root._testRefs1[0], root._testRefs0[0]));
        assertTrue(root._testRefs1[1] == root._testRefs0[1]);    // Works because we only read in Boolean.TRUE, Boolean.FALSE, or null
        assertTrue(root._testRefs1[2] == root._testRefs0[2]);
        assertTrue(root._testRefs1[3] == root._testRefs0[3]);
        assertTrue(root._testRefs1[4] == root._testRefs0[4]);
        assertTrue(root._testRefs1[5] == root._testRefs0[5]);
        assertTrue(root._testRefs1[6].equals(root._testRefs0[6]));
        assertTrue(root._testRefs1[7] == root._testRefs0[7]);
        assertTrue(root._testRefs1[8].equals(root._testRefs0[8]));
        assertTrue(root._testRefs1[9].equals(root._testRefs0[9]));

        assertTrue(root._arrayO instanceof Object[]);
        Object[] items = (Object[]) root._arrayO;
        assertTrue(items.length == 5);
        assertTrue("foo".equals(items[0]));
        assertTrue(Boolean.TRUE.equals(items[1]));
        assertNull(items[2]);
        assertTrue(((Long) 16L).equals(items[3]));
        assertTrue(((Double) 3.14).equals(items[4]));

        assertTrue(root._arrayS instanceof String[]);
        String[] strItems = (String[]) root._arrayS;
        assertTrue(strItems.length == 2);
        assertTrue("fingers".equals(strItems[0]));
        assertTrue("toes".equals(strItems[1]));

        assertTrue(root._arrayArrayO instanceof Object[]);
        assertTrue(root._arrayArrayO instanceof Object[][]);
        assertFalse(root._arrayArrayO instanceof Object[][][]);

        time(root);
    }

    private static class TestCollection implements Serializable
    {
        private Collection[] _cols;
        private List _strings_a;
        private List _strings_b;
        private List _strings_c;
        private List _dates_a;
        private List _dates_b;
        private List _dates_c;
        private List _classes_a;
        private List _classes_b;
        private List _classes_c;
        private List _sb_a;
        private List _sb_b;
        private List _sb_c;
        private List _poly_a;
        private Set _strs_a;
        private Set _strs_b;
        private Set _strs_c;
        private Set _strs_d;

        private void init()
        {
            Collection array = new ArrayList();
            array.add(_testDate);
            array.add("Hello");
            array.add(new TestObject("fudge"));
            array.add(_CONST_INT);

            Collection set = new HashSet();
            set.add(Map.class);
            set.add(Boolean.TRUE);
            set.add(null);
            set.add(_CONST_INT);

            Collection tree = new TreeSet();
            tree.add(new Integer(Integer.MIN_VALUE));
            tree.add(new Integer(1));
            tree.add(new Integer(Integer.MAX_VALUE));
            tree.add(_CONST_INT);

            _cols = new Collection[]{array, set, tree};

            _strings_a = new LinkedList();
            _strings_a.add("Alpha");
            _strings_a.add("Bravo");
            _strings_a.add("Charlie");
            _strings_a.add("Delta");
            _strings_b = new LinkedList();
            _strings_c = null;

            _dates_a = new ArrayList();
            _dates_a.add(new Date(0));
            _dates_a.add(_testDate);
            _dates_a.add(new Date(Long.MAX_VALUE));
            _dates_a.add(null);
            _dates_b = new ArrayList();
            _dates_c = null;

            _classes_a = new ArrayList();
            _classes_a.add(boolean.class);
            _classes_a.add(char.class);
            _classes_a.add(byte.class);
            _classes_a.add(short.class);
            _classes_a.add(int.class);
            _classes_a.add(long.class);
            _classes_a.add(float.class);
            _classes_a.add(double.class);
            _classes_a.add(String.class);
            _classes_a.add(Date.class);
            _classes_a.add(null);
            _classes_a.add(Class.class);
            _classes_b = new ArrayList();
            _classes_c = null;

            _sb_a = new LinkedList();
            _sb_a.add(new StringBuffer("one"));
            _sb_a.add(new StringBuffer("two"));
            _sb_b = new LinkedList();
            _sb_c = null;

            _poly_a = new ArrayList();
            _poly_a.add(Boolean.TRUE);
            _poly_a.add(new Character('a'));
            _poly_a.add(new Byte((byte) 16));
            _poly_a.add(new Short((short) 69));
            _poly_a.add(new Integer(714));
            _poly_a.add(new Long(420));
            _poly_a.add(new Float(0.4));
            _poly_a.add(new Double(3.14));
            _poly_a.add("Jones'in\tfor\u0019a\ncoke");
            _poly_a.add(null);
            _poly_a.add(new StringBuffer("eddie"));
            _poly_a.add(_testDate);
            _poly_a.add(Long.class);
            _poly_a.add(new String[]{"beatles", "stones"});
            _poly_a.add(new TestObject[]{new TestObject("flint"), new TestObject("stone")});
            _poly_a.add(new Object[]{"fox", "wolf", "dog", "hound"});

            Set colors = new TreeSet();
            colors.add(new TestObject("red"));
            colors.add(new TestObject("green"));
            colors.add(new TestObject("blue"));
            _poly_a.add(colors);

            _strs_a = new HashSet();
            _strs_a.add("Dog");
            _strs_a.add("Cat");
            _strs_a.add("Cow");
            _strs_a.add("Horse");
            _strs_a.add("Duck");
            _strs_a.add("Bird");
            _strs_a.add("Goose");
            _strs_b = new HashSet();
            _strs_c = null;
            _strs_d = new TreeSet();
            _strs_d.addAll(_strs_a);
        }
    }

    public void testCollection() throws Exception
    {
        println("\nTestJsonWriter.testCollection()");
        TestCollection obj = new TestCollection();
        obj.init();
        String jsonOut = getJsonString(obj);
        println(jsonOut);

        JsonReader jr = new JsonReader(convertStringToInputStream(jsonOut));
        TestCollection root = (TestCollection) readJsonObject(jr);

        assertTrue(root._cols.length == 3);
        assertTrue(root._cols[0].getClass().equals(ArrayList.class));
        assertTrue(root._cols[1].getClass().equals(HashSet.class));
        assertTrue(root._cols[2].getClass().equals(TreeSet.class));

        Collection array = root._cols[0];
        assertTrue(array.size() == 4);
        assertTrue(array.getClass().equals(ArrayList.class));
        List alist = (List) array;
        assertTrue(alist.get(0).equals(_testDate));
        assertTrue(alist.get(1).equals("Hello"));
        assertTrue(alist.get(2).equals(new TestObject("fudge")));
        assertTrue(alist.get(3).equals(_CONST_INT));

        Collection set = root._cols[1];
        assertTrue(set.size() == 4);
        assertTrue(set.getClass().equals(HashSet.class));
        assertTrue(set.contains(Map.class));
        assertTrue(set.contains(Boolean.TRUE));
        assertTrue(set.contains(null));
        assertTrue(set.contains(_CONST_INT));

        set = root._cols[2];
        assertTrue(set.size() == 4);
        assertTrue(set.getClass().equals(TreeSet.class));
        assertTrue(set.contains(new Integer(Integer.MIN_VALUE)));
        assertTrue(set.contains(new Integer(1)));
        assertTrue(set.contains(new Integer(Integer.MAX_VALUE)));
        assertTrue(set.contains(_CONST_INT));

        assertTrue(root._strings_a.size() == 4);
        assertTrue(root._strings_a.get(0).equals("Alpha"));
        assertTrue(root._strings_a.get(1).equals("Bravo"));
        assertTrue(root._strings_a.get(2).equals("Charlie"));
        assertTrue(root._strings_a.get(3).equals("Delta"));
        assertTrue(root._strings_b.size() == 0);
        assertNull(root._strings_c);

        assertTrue(root._dates_a.size() == 4);
        assertTrue(root._dates_a.get(0).equals(new Date(0)));
        assertTrue(root._dates_a.get(1).equals(_testDate));
        assertTrue(root._dates_a.get(2).equals(new Date(Long.MAX_VALUE)));
        assertNull(root._dates_a.get(3));
        assertTrue(root._dates_b.size() == 0);
        assertNull(root._dates_c);

        assertTrue(root._classes_a.size() == 12);
        assertTrue(root._classes_a.get(0) == boolean.class);
        assertTrue(root._classes_a.get(1).equals(char.class));
        assertTrue(root._classes_a.get(2).equals(byte.class));
        assertTrue(root._classes_a.get(3).equals(short.class));
        assertTrue(root._classes_a.get(4).equals(int.class));
        assertTrue(root._classes_a.get(5).equals(long.class));
        assertTrue(root._classes_a.get(6).equals(float.class));
        assertTrue(root._classes_a.get(7).equals(double.class));
        assertTrue(root._classes_a.get(8).equals(String.class));
        assertTrue(root._classes_a.get(9).equals(Date.class));
        assertNull(root._classes_a.get(10));
        assertTrue(root._classes_a.get(11).equals(Class.class));
        assertTrue(root._classes_b.size() == 0);
        assertNull(root._classes_c);

        assertTrue(root._sb_a.size() == 2);
        assertTrue(root._sb_a.get(0).toString().equals("one"));
        assertTrue(root._sb_a.get(1).toString().equals("two"));
        assertTrue(root._sb_b.size() == 0);
        assertNull(root._sb_c);

        assertTrue(root._poly_a.size() == 17);
        assertTrue(root._poly_a.get(0).equals(Boolean.TRUE));
        assertTrue(root._poly_a.get(1).equals(new Character('a')));
        assertTrue(root._poly_a.get(2).equals(new Byte((byte) 16)));
        assertTrue(root._poly_a.get(3).equals(new Short((byte) 69)));
        assertTrue(root._poly_a.get(4).equals(new Integer(714)));
        assertTrue(root._poly_a.get(5).equals(new Long(420)));
        assertTrue(root._poly_a.get(6).equals(new Float(0.4)));
        assertTrue(root._poly_a.get(7).equals(new Double(3.14)));
        assertTrue(root._poly_a.get(8).equals("Jones'in\tfor\u0019a\ncoke"));
        assertNull(root._poly_a.get(9));
        assertTrue(root._poly_a.get(10).toString().equals("eddie"));
        assertTrue(root._poly_a.get(11).equals(_testDate));
        assertTrue(root._poly_a.get(12).equals(Long.class));

        String[] sa = (String[]) root._poly_a.get(13);
        assertTrue(sa[0].equals("beatles"));
        assertTrue(sa[1].equals("stones"));
        TestObject[] to = (TestObject[]) root._poly_a.get(14);
        assertTrue(to[0].getName().equals("flint"));
        assertTrue(to[1].getName().equals("stone"));
        Object[] arrayInCol = (Object[]) root._poly_a.get(15);
        assertTrue(arrayInCol[0].equals("fox"));
        assertTrue(arrayInCol[1].equals("wolf"));
        assertTrue(arrayInCol[2].equals("dog"));
        assertTrue(arrayInCol[3].equals("hound"));

        Set colors = (Set) root._poly_a.get(16);
        assertTrue(colors.size() == 3);
        assertTrue(colors.contains(new TestObject("red")));
        assertTrue(colors.contains(new TestObject("green")));
        assertTrue(colors.contains(new TestObject("blue")));

        assertTrue(root._strs_a.size() == 7);
        assertTrue(root._strs_a.contains("Dog"));
        assertTrue(root._strs_a.contains("Cat"));
        assertTrue(root._strs_a.contains("Cow"));
        assertTrue(root._strs_a.contains("Horse"));
        assertTrue(root._strs_a.contains("Duck"));
        assertTrue(root._strs_a.contains("Bird"));
        assertTrue(root._strs_a.contains("Goose"));
        assertTrue(root._strs_b.size() == 0);
        assertNull(root._strs_c);
        assertTrue(root._strs_d.size() == 7);
        assertTrue(root._strs_d instanceof TreeSet);

        time(root);
    }

    private static class TestMap implements Serializable
    {
        private Map _strings_a;
        private Map _strings_b;
        private Map _strings_c;
        private Map _testobjs_a;
        private Map _map_col;
        private Map _map_col_2;
        private Map _map_col_3;
        private Map _map_obj;
        private Map _map_con;

        private void init()
        {
            _strings_a = new HashMap();
            _strings_b = new HashMap();
            _strings_c = null;
            _testobjs_a = new TreeMap();
            _map_col = new HashMap();
            _map_col_2 = new TreeMap();
            _map_col_3 = new HashMap();
            _map_obj = new HashMap();
            _map_con = new ConcurrentHashMap();

            _strings_a.put("woods", "tiger");
            _strings_a.put("mickleson", "phil");
            _strings_a.put("garcia", "sergio");

            _testobjs_a.put(new TestObject("one"), new TestObject("alpha"));
            _testobjs_a.put(new TestObject("two"), new TestObject("bravo"));

            List l = new LinkedList();
            l.add("andromeda");
            _map_col.put(new TestObject[]{new TestObject("earth"), new TestObject("jupiter")}, l);
            _map_col_2.put("cat", new Object[]{"tiger", "lion", "cheetah", "jaguar"});
            _map_col_3.put(new Object[]{"composite", "key"}, "value");

            _map_obj.put(new Integer(99), new Double(.123));
            _map_obj.put(null, null);

            _map_con.put(new TestObject("alpha"), new TestObject("one"));
            _map_con.put(new TestObject("bravo"), new TestObject("two"));
        }
    }

    public void testMap() throws Exception
    {
        println("\nTestJsonWriter.testMap()");
        TestMap obj = new TestMap();
        obj.init();
        String jsonOut = getJsonString(obj);
        println(jsonOut);

        JsonReader jr = new JsonReader(convertStringToInputStream(jsonOut));
        TestMap root = (TestMap) readJsonObject(jr);

        assertTrue(root._strings_a.size() == 3);
        assertTrue(root._strings_a.get("woods").equals("tiger"));
        assertTrue(root._strings_a.get("mickleson").equals("phil"));
        assertTrue(root._strings_a.get("garcia").equals("sergio"));
        assertTrue(root._strings_b.size() == 0);
        assertNull(root._strings_c);

        assertTrue(root._testobjs_a.size() == 2);
        assertTrue(root._testobjs_a.get(new TestObject("one")).equals(new TestObject("alpha")));
        assertTrue(root._testobjs_a.get(new TestObject("two")).equals(new TestObject("bravo")));

        assertTrue(root._map_col.size() == 1);
        Iterator i = root._map_col.keySet().iterator();
        TestObject[] key = (TestObject[]) i.next();
        key[0]._name.equals("earth");
        key[1]._name.equals("jupiter");
        i = root._map_col.values().iterator();
        Collection list = (Collection) i.next();
        list.contains("andromeda");

        // Check value-side of Map with Object[] (special case because Object[]'s @type is never written)
        Object[] catTypes = (Object[]) root._map_col_2.get("cat");
        assertTrue(catTypes[0].equals("tiger"));
        assertTrue(catTypes[1].equals("lion"));
        assertTrue(catTypes[2].equals("cheetah"));
        assertTrue(catTypes[3].equals("jaguar"));

        assertTrue(root._map_col_3.size() == 1);
        i = root._map_col_3.keySet().iterator();
        Object[] key_a = (Object[]) i.next();
        key_a[0].equals("composite");
        key_a[1].equals("key");
        String value = (String) root._map_col_3.get(key_a);
        assertTrue(value.equals("value"));

        assertTrue(root._map_obj.size() == 2);
        assertTrue(root._map_obj.get(new Integer(99)).equals(new Double(.123)));
        assertNull(root._map_obj.get(null));

        assertTrue(root._map_con.size() == 2);
        assertTrue(root._map_con instanceof ConcurrentHashMap);
        i = root._map_con.entrySet().iterator();
        while (i.hasNext())
        {
            Map.Entry e = (Map.Entry) i.next();
            TestObject key1 = (TestObject) e.getKey();
            TestObject value1 = (TestObject) e.getValue();
            if (key1.equals(new TestObject("alpha")))
            {
                assertTrue(value1.getName().equals("one"));
            }
            else if (key1.equals(new TestObject("bravo")))
            {
                assertTrue(value1.getName().equals("two"));
            }
            else
            {
                assertTrue("Unknown key", false);
            }
        }

        time(root);
    }

    // Test direct fields for all types, primitives, special handled fields
    // like Date, String, and Class, plus regular objects, and circular
    // references.

    private static class TestFields implements Serializable
    {
        private boolean _boolean_a;
        private boolean _boolean_b;
        private Boolean _boolean_c;
        private Boolean _boolean_d;
        private Boolean _boolean_e;

        private char _char_a;
        private char _char_b;
        private char _char_c;
        private Character _char_d;
        private Character _char_e;
        private Character _char_f;
        private Character _char_g;

        private byte _byte_a;
        private byte _byte_b;
        private Byte _byte_c;
        private Byte _byte_d;
        private Byte _byte_e;

        private short _short_a;
        private short _short_b;
        private Short _short_c;
        private Short _short_d;
        private Short _short_e;

        private int _int_a;
        private int _int_b;
        private Integer _int_c;
        private Integer _int_d;
        private Integer _int_e;

        private long _long_a;
        private long _long_b;
        private Long _long_c;
        private Long _long_d;
        private Long _long_e;

        private float _float_a;
        private float _float_b;
        private Float _float_c;
        private Float _float_d;
        private Float _float_e;

        private double _double_a;
        private double _double_b;
        private Double _double_c;
        private Double _double_d;
        private Double _double_e;

        private String _string_a;
        private String _string_b;
        private String _string_c;

        private Date _date_a;
        private Date _date_b;
        private Date _date_c;

        private Class _class_a;
        private Class _class_b;
        private Class _class_c;
        private Class _class_d;
        private Class _class_e;
        private Class _class_f;
        private Class _class_g;
        private Class _class_h;
        private Class _class_i;
        private Class _class_j;

        private StringBuffer _sb_a;
        private StringBuffer _sb_b;
        private StringBuffer _sb_c;

        // Cycle test
        private TestObject _cycleTest;

        // Ensure @type is dropped when Collection field type matches instance type
        // Normally, this is poor coding style, however, the @type field can be dropped
        // in these cases, making the JSON output smaller.
        private ArrayList _arrayList_empty;
        private ArrayList _arrayList_1;
        private List _arrayList_2;
        private List _arrayList_3;
        private List _arrayList_4;
        private ArrayList _arrayList_5;
        private List _arrayList_6;

        private HashMap _hashMap_empty;
        private HashMap _hashMap_1;
        private Map _hashMap_2;
        private Map _hashMap_3;
        private Map _hashMap_4;
        private HashMap _hashMap_5;
        private Map _hashMap_6;

        private String[] _stringArray_empty;
        private String[] _stringArray_1;
        private Object[] _stringArray_2;
        private Object[] _stringArray_3;
        private Object[] _stringArray_4;
        private String[] _stringArray_5;
        private Object[] _stringArray_6;

        private void init()
        {
            _boolean_a = true;
            _boolean_b = false;
            _boolean_c = new Boolean(true);
            _boolean_d = new Boolean(false);
            _boolean_e = null;

            _char_a = 'a';
            _char_b = '\t';
            _char_c = '\u0004';
            _char_d = new Character('a');
            _char_e = new Character('\t');
            _char_f = new Character('\u0002');
            _char_g = null;

            _byte_a = -128;
            _byte_b = 127;
            _byte_c = new Byte((byte) -128);
            _byte_d = new Byte((byte) 127);
            _byte_e = null;

            _short_a = Short.MIN_VALUE;
            _short_b = Short.MAX_VALUE;
            _short_c = new Short(Short.MIN_VALUE);
            _short_d = new Short(Short.MAX_VALUE);
            _short_e = null;

            _int_a = Integer.MIN_VALUE;
            _int_b = Integer.MAX_VALUE;
            _int_c = new Integer(Integer.MIN_VALUE);
            _int_d = new Integer(Integer.MAX_VALUE);
            _int_e = null;

            _long_a = Long.MIN_VALUE;
            _long_b = Long.MAX_VALUE;
            _long_c = new Long(Long.MIN_VALUE);
            _long_d = new Long(Long.MAX_VALUE);
            _long_e = null;

            _float_a = Float.MIN_VALUE;
            _float_b = Float.MAX_VALUE;
            _float_c = new Float(Float.MIN_VALUE);
            _float_d = new Float(Float.MAX_VALUE);
            _float_e = null;

            _double_a = Double.MIN_VALUE;
            _double_b = Double.MAX_VALUE;
            _double_c = new Double(Double.MIN_VALUE);
            _double_d = new Double(Double.MAX_VALUE);
            _double_e = null;

            _string_a = "Hello";
            _string_b = "";
            _string_c = null;

            _date_a = _testDate;
            _date_b = new Date(0);
            _date_c = null;

            _class_a = boolean.class;
            _class_b = char.class;
            _class_c = byte.class;
            _class_d = short.class;
            _class_e = int.class;
            _class_f = long.class;
            _class_g = float.class;
            _class_h = double.class;
            _class_i = String.class;
            _class_j = null;

            _sb_a = new StringBuffer("holstein");
            _sb_b = new StringBuffer();
            _sb_c = null;

            TestObject a = new TestObject("A");
            TestObject b = new TestObject("B");
            TestObject c = new TestObject("C");
            a._other = b;
            b._other = c;
            c._other = a;
            _cycleTest = a;

            _arrayList_empty = new ArrayList();
            _arrayList_1 = new ArrayList();
            _arrayList_1.add("should be no id, no type");
            _arrayList_2 = new ArrayList();
            _arrayList_2.add("should have type, but no id");
            _arrayList_3 = new ArrayList();
            _arrayList_3.add("should have id and type");
            _arrayList_4 = _arrayList_3;
            _arrayList_5 = new ArrayList();
            _arrayList_5.add("should have id, but no type");
            _arrayList_6 = _arrayList_5;

            _hashMap_empty = new HashMap();
            _hashMap_1 = new HashMap();
            _hashMap_1.put("mapkey", "should have no id or type");
            _hashMap_2 = new HashMap();
            _hashMap_2.put("mapkey", "should have type, but no id");
            _hashMap_3 = new HashMap();
            _hashMap_3.put("mapkey", "should have id and type");
            _hashMap_4 = _hashMap_3;
            _hashMap_5 = new HashMap();
            _hashMap_5.put("mapkey", "should have id, but no type");
            _hashMap_6 = _hashMap_5;

            _stringArray_empty = new String[]{};
            _stringArray_1 = new String[]{"should have no id, no type"};
            _stringArray_2 = new String[]{"should have type, but no id"};
            _stringArray_3 = new String[]{"should have id and type"};
            _stringArray_4 = _stringArray_3;
            _stringArray_5 = new String[]{"should have id, but not type"};
            _stringArray_6 = _stringArray_5;
        }
    }

    public void testFields() throws Exception
    {
        println("\nTestJsonWriter.testFields()");
        TestFields obj = new TestFields();
        obj.init();
        String jsonOut = getJsonString(obj);
        println(jsonOut);

        JsonReader jr = new JsonReader(convertStringToInputStream(jsonOut));
        TestFields root = (TestFields) readJsonObject(jr);

        assertTrue(root._boolean_a);
        assertFalse(root._boolean_b);
        assertTrue(root._boolean_c.booleanValue());
        assertFalse(root._boolean_d.booleanValue());
        assertNull(root._boolean_e);

        assertTrue(root._char_a == 'a');
        assertTrue(root._char_b == '\t');
        assertTrue(root._char_c == '\u0004');
        assertTrue(root._char_d.equals(new Character('a')));
        assertTrue(root._char_e.equals(new Character('\t')));
        assertTrue(root._char_f.equals(new Character('\u0002')));
        assertNull(root._char_g);

        assertTrue(root._byte_a == Byte.MIN_VALUE);
        assertTrue(root._byte_b == Byte.MAX_VALUE);
        assertTrue(root._byte_c.equals(new Byte(Byte.MIN_VALUE)));
        assertTrue(root._byte_d.equals(new Byte(Byte.MAX_VALUE)));
        assertNull(root._byte_e);

        assertTrue(root._short_a == Short.MIN_VALUE);
        assertTrue(root._short_b == Short.MAX_VALUE);
        assertTrue(root._short_c.equals(new Short(Short.MIN_VALUE)));
        assertTrue(root._short_d.equals(new Short(Short.MAX_VALUE)));
        assertNull(root._short_e);

        assertTrue(root._int_a == Integer.MIN_VALUE);
        assertTrue(root._int_b == Integer.MAX_VALUE);
        assertTrue(root._int_c.equals(new Integer(Integer.MIN_VALUE)));
        assertTrue(root._int_d.equals(new Integer(Integer.MAX_VALUE)));
        assertNull(root._int_e);

        assertTrue(root._long_a == Long.MIN_VALUE);
        assertTrue(root._long_b == Long.MAX_VALUE);
        assertTrue(root._long_c.equals(new Long(Long.MIN_VALUE)));
        assertTrue(root._long_d.equals(new Long(Long.MAX_VALUE)));
        assertNull(root._long_e);

        assertTrue(root._float_a == Float.MIN_VALUE);
        assertTrue(root._float_b == Float.MAX_VALUE);
        assertTrue(root._float_c.equals(new Float(Float.MIN_VALUE)));
        assertTrue(root._float_d.equals(new Float(Float.MAX_VALUE)));
        assertNull(root._float_e);

        assertTrue(root._double_a == Double.MIN_VALUE);
        assertTrue(root._double_b == Double.MAX_VALUE);
        assertTrue(root._double_c.equals(new Double(Double.MIN_VALUE)));
        assertTrue(root._double_d.equals(new Double(Double.MAX_VALUE)));
        assertNull(root._double_e);

        assertTrue(root._string_a.equals("Hello"));
        assertTrue(root._string_b.equals(""));
        assertNull(root._string_c);

        assertTrue(root._date_a.equals(_testDate));
        assertTrue(root._date_b.equals(new Date(0)));
        assertNull(root._date_c);

        assertTrue(root._class_a.equals(boolean.class));
        assertTrue(root._class_b.equals(char.class));
        assertTrue(root._class_c.equals(byte.class));
        assertTrue(root._class_d.equals(short.class));
        assertTrue(root._class_e.equals(int.class));
        assertTrue(root._class_f.equals(long.class));
        assertTrue(root._class_g.equals(float.class));
        assertTrue(root._class_h.equals(double.class));
        assertTrue(root._class_i.equals(String.class));
        assertNull(root._class_j);

        assertTrue(root._sb_a.toString().equals("holstein"));
        assertTrue(root._sb_b.toString().equals(""));
        assertNull(root._sb_c);

        assertTrue(root._cycleTest._name.equals("A"));
        assertTrue(root._cycleTest._other._name.equals("B"));
        assertTrue(root._cycleTest._other._other._name.equals("C"));
        assertTrue(root._cycleTest._other._other._other._name.equals("A"));
        assertTrue(root._cycleTest == root._cycleTest._other._other._other);

        assertTrue(root._arrayList_empty.isEmpty());
        assertTrue(root._arrayList_1.size() == 1);
        assertTrue(root._arrayList_2.size() == 1);
        assertTrue(root._arrayList_3.size() == 1);
        assertTrue(root._arrayList_4 == root._arrayList_3);
        assertTrue(root._arrayList_5.size() == 1);
        assertTrue(root._arrayList_6 == root._arrayList_5);

        assertTrue(root._hashMap_empty.size() == 0);
        assertTrue(root._hashMap_1.size() == 1);
        assertTrue(root._hashMap_2.size() == 1);
        assertTrue(root._hashMap_3.size() == 1);
        assertTrue(root._hashMap_4 == root._hashMap_3);
        assertTrue(root._hashMap_5.size() == 1);
        assertTrue(root._hashMap_6 == root._hashMap_5);

        assertTrue(root._stringArray_empty.length == 0);
        assertTrue(root._stringArray_1.length == 1);
        assertTrue(root._stringArray_2.length == 1);
        assertTrue(root._stringArray_3.length == 1);
        assertTrue(root._stringArray_4 == root._stringArray_3);
        assertTrue(root._stringArray_5.length == 1);
        assertTrue(root._stringArray_6 == root._stringArray_5);
        time(root);
    }

    private static class TestReferences implements Serializable
    {
        // Field ordering below is vital (b, a, c).  We treat JSON keys in alphabetical
        // order, whereas Java Field walking is in declaration order.
        private TestObject _b;
        private TestObject[] _a;
        private TestObject[] _c;

        private TestObject[][] _foo;

        private TestObject _back_a;
        private TestObject _back_b;
        private TestObject _back_c;

        private TestObject _cycle_a;
        private TestObject _cycle_b;

        private TestObject _polymorphic;
        private TestObject[] _polymorphics;

        private char _big;

        private void init()
        {
            _big = '\ufbfc';
            _b = new TestObject("B");
            _a = new TestObject[]{_b};
            _c = new TestObject[]{_b};
            _foo = new TestObject[][]{null, {}, {new TestObject("alpha"), new TestObject("beta")}};
            _back_a = new TestObject("back test");
            _back_b = _back_a;
            _back_c = _back_b;

            _cycle_a = new TestObject("a");
            _cycle_b = new TestObject("b");
            _cycle_a._other = _cycle_b;
            _cycle_b._other = _cycle_a;

            _polymorphic = new TestObjectKid("dilbert", "dilbert@myotherdrive.com");
            _polymorphics = new TestObject[]{new TestObjectKid("dog", "dog@house.com"), new TestObjectKid("cat", "cat@house.com"), new TestObject("shortie")};
        }
    }

    public void testReferences() throws Exception
    {
        println("\nTestJsonWriter.testReferences()");
        TestReferences obj = new TestReferences();
        obj.init();
        String jsonOut = getJsonString(obj);
        println(jsonOut);

        JsonReader jr = new JsonReader(convertStringToInputStream(jsonOut));
        TestReferences root = (TestReferences) readJsonObject(jr);

        assertTrue(root._a.length == 1);
        assertTrue(root._b != null);
        assertTrue(root._a[0] == root._b);
        assertTrue(root._c.length == 1);
        assertTrue(root._c[0] == root._b);

        assertTrue(root._foo.length == 3);
        assertNull(root._foo[0]);
        assertTrue(root._foo[1].length == 0);
        assertTrue(root._foo[2].length == 2);

        assertTrue(root._back_b == root._back_a);
        assertTrue(root._back_c == root._back_a);

        assertTrue(root._cycle_a._name.equals("a"));
        assertTrue(root._cycle_b._name.equals("b"));
        assertTrue(root._cycle_a._other == root._cycle_b);
        assertTrue(root._cycle_b._other == root._cycle_a);

        assertTrue(root._polymorphic.getClass().equals(TestObjectKid.class));
        TestObjectKid kid = (TestObjectKid) root._polymorphic;
        assertTrue(kid._name.equals("dilbert"));
        assertTrue(kid._email.equals("dilbert@myotherdrive.com"));

        assertTrue(root._polymorphics.length == 3);
        TestObjectKid kid1 = (TestObjectKid) root._polymorphics[0];
        TestObjectKid kid2 = (TestObjectKid) root._polymorphics[1];
        TestObject kid3 = root._polymorphics[2];
        assertTrue(kid1.getClass().equals(TestObjectKid.class));
        assertTrue(kid1.getClass().equals(kid2.getClass()));
        assertTrue(kid3.getClass().equals(TestObject.class));
        assertTrue(kid1._name.equals("dog"));
        assertTrue(kid1._email.equals("dog@house.com"));
        assertTrue(kid2._name.equals("cat"));
        assertTrue(kid2._email.equals("cat@house.com"));
        assertTrue(kid3._name.equals("shortie"));
        assertTrue(root._big == '\ufbfc');

        time(root);
    }

    public void testPerformance() throws Exception
    {
        println("\nTestJsonWriter.testPerformance()    128K byte[]");
        byte[] bytes = new byte[128 * 1024];
        Random r = new Random();
        r.nextBytes(bytes);
        String json = getJsonString(bytes);

        JsonReader jr = new JsonReader(convertStringToInputStream(json));
        byte[] bytes2 = (byte[]) readJsonObject(jr);

        for (int i = 0; i < bytes.length; i++)
        {
            assertTrue(bytes[i] == bytes2[i]);
        }

        time(bytes);
    }

    public static class DateTrick
    {
        private Date _userDate;
    }

    public static class LongTrick
    {
        private long _userDate;
    }

    // Instantiate off of each others JSON String, proving Date for long substitution works.  This will work on any
    // field that is of type Date or Long.  It will not work when the Dates are inside a Collection, for example.
    // <p/>
    // This substitution trick allows Date fields to be converted to long in order to save memory 16 bytes of memory
    // per date.  (Date's are more than 8 bytes, longs are 8).

    public void testDateLongSubstitution() throws Exception
    {
        println("\nTestJsonWriter.testDateLongSubstitution()");
        long now = System.currentTimeMillis();
        DateTrick d = new DateTrick();
        d._userDate = new Date(now);
        LongTrick l = new LongTrick();
        l._userDate = now;
        String jsonOut1 = getJsonString(d);
        println(jsonOut1);
        String jsonOut2 = getJsonString(l);
        println(jsonOut2);
        jsonOut1 = jsonOut1.replace("$Date", "$Long");
        jsonOut2 = jsonOut2.replace("$Long", "$Date");
        JsonReader jr = new JsonReader(convertStringToInputStream(jsonOut1));
        l = (LongTrick) jr.readObject();
        jr.close();
        jr = new JsonReader(convertStringToInputStream(jsonOut2));
        d = (DateTrick) jr.readObject();
        jr.close();
        assertTrue(d._userDate.getTime() == l._userDate);
    }

    public void testRoots() throws Exception
    {
        println("\nTestJsonWriter.testRoots()");
        // Test Object[] as root element passed in
        Object[] foo = {new TestObject("alpha"), new TestObject("beta")};

        String jsonOut = getJsonString(foo);
        println(jsonOut);

        JsonReader jr = new JsonReader(convertStringToInputStream(jsonOut));
        Object[] bar = (Object[]) jr.readObject();

        assertTrue(bar.length == 2);
        assertTrue(bar[0].equals(new TestObject("alpha")));
        assertTrue(bar[1].equals(new TestObject("beta")));

        String json = "[\"getStartupInfo\",[\"890.022905.16112006.00024.0067ur\",\"machine info\"]]";
        jr = new JsonReader(convertStringToInputStream(json));
        Object[] baz = (Object[]) jr.readObject();
        assertTrue(baz.length == 2);
        assertTrue("getStartupInfo".equals(baz[0]));
        Object[] args = (Object[]) baz[1];
        assertTrue(args.length == 2);
        assertTrue("890.022905.16112006.00024.0067ur".equals(args[0]));
        assertTrue("machine info".equals(args[1]));

        String hw = "[\"Hello, World\"]";
        jr = new JsonReader(convertStringToInputStream(hw));
        Object[] qux = (Object[]) jr.readObject();
        assertTrue(qux != null);
        assertTrue("Hello, World".equals(qux[0]));

        // Whitespace
        jr = new JsonReader(convertStringToInputStream(" [  {  \"@type\"  :  \"com.cedarsoftware.util.io.TestJsonReaderWriter$TestObject\"  ,  \"_name\"  :  \"alpha\"  ,  \"_other\"  :  null  }  ,  {  \"@type\"  :  \"com.cedarsoftware.util.io.TestJsonReaderWriter$TestObject\"  ,  \"_name\"  :  \"beta\"  ,  \"_other\" : null  }  ]  "));
        Object[] fred = (Object[]) jr.readObject();
        assertTrue(fred != null);
        assertTrue(fred.length == 2);
        assertTrue(fred[0].equals(new TestObject("alpha")));
        assertTrue(fred[1].equals(new TestObject("beta")));

        jr = new JsonReader(convertStringToInputStream("[{\"@type\":\"com.cedarsoftware.util.io.TestJsonReaderWriter$TestObject\",\"_name\" : \"alpha\" , \"_other\":null,\"fake\":\"_typeArray\"},{\"@type\": \"com.cedarsoftware.util.io.TestJsonReaderWriter$TestObject\",\"_name\":\"beta\",\"_other\":null}]"));
        Object[] wilma = (Object[]) jr.readObject();
        assertTrue(wilma != null);
        assertTrue(wilma.length == 2);
        assertTrue(wilma[0].equals(new TestObject("alpha")));
        assertTrue(wilma[1].equals(new TestObject("beta")));
    }

    public void testRoots2() throws Exception
    {
        println("\nTestJsonWriter.testRoots2()");
        // Test root JSON type as [ ]
        Object array = new Object[]{"Hello"};
        String json = getJsonString(array);
        JsonReader jr = new JsonReader(convertStringToInputStream(json));
        Object oa = jr.readObject();
        assertTrue(oa.getClass().isArray());
        assertTrue(((Object[]) oa)[0].equals("Hello"));

        // Test root JSON type as { }
        Calendar cal = Calendar.getInstance();
        cal.set(1965, 11, 17);
        json = getJsonString(cal);
        println("json = " + json);
        jr = new JsonReader(convertStringToInputStream(json));
        Object obj = jr.readObject();
        assertTrue(!obj.getClass().isArray());
        Calendar date = (Calendar) obj;
        assertTrue(date.get(Calendar.YEAR) == 1965);
        assertTrue(date.get(Calendar.MONTH) == 11);
        assertTrue(date.get(Calendar.DAY_OF_MONTH) == 17);
    }

    public void testNoDefaultConstructor() throws Exception
    {
        println("\nTestJsonWriter.testNoDefaultConstructor()");
        Calendar c = Calendar.getInstance();
        c.set(2010, 5, 5, 5, 5, 5);
        String[] strings = new String[]{"C", "C++", "Java"};
        int[] ints = new int[]{1, 2, 4, 8, 16, 32, 64, 128};
        Object foo = new TestJsonNoDefaultOrPublicConstructor("Hello, World.", c.getTime(), (byte) 1, (short) 2, 3, 4L, 5.0f, 6.0, true, 'J', strings, ints);
        String jsonOut = getJsonString(foo);
        println(jsonOut);
        JsonReader jr = new JsonReader(convertStringToInputStream(jsonOut));
        TestJsonNoDefaultOrPublicConstructor bar = (TestJsonNoDefaultOrPublicConstructor) jr.readObject();
        assertTrue("Hello, World.".equals(bar.getString()));
        assertTrue(bar.getDate().equals(c.getTime()));
        assertTrue(bar.getByte() == 1);
        assertTrue(bar.getShort() == 2);
        assertTrue(bar.getInt() == 3);
        assertTrue(bar.getLong() == 4);
        assertTrue(bar.getFloat() == 5.0f);
        assertTrue(bar.getDouble() == 6.0);
        assertTrue(bar.getBoolean());
        assertTrue(bar.getChar() == 'J');
        assertTrue(bar.getStrings() != null);
        assertTrue(bar.getStrings().length == strings.length);
        assertTrue(bar.getInts() != null);
        assertTrue(bar.getInts().length == ints.length);
    }

    public void testUntypedArray() throws Exception
    {
        println("\nTestJsonWriter.testUntypedArray()");
        JsonReader jr = new JsonReader(convertStringToInputStream("[\"string\",17, null, true, false, [], -1273123,32131, 1e6, 3.14159, -9223372036854775808, 9223372036854775807]"));
        Object[] args = (Object[]) jr.readObject();

        for (int i = 0; i < args.length; i++)
        {
            println("args[" + i + "]=" + args[i]);
            if (args[i] != null)
            {
                println("args[" + i + "]=" + args[i].getClass().getName());
            }
        }

        assertTrue(args[0].equals("string"));
        assertTrue(args[1].equals(17L));
        assertTrue(args[2] == null);
        assertTrue(args[3].equals(Boolean.TRUE));
        assertTrue(args[4].equals(Boolean.FALSE));
        assertTrue(args[5].getClass().isArray());
        assertTrue(args[6].equals(-1273123L));
        assertTrue(args[7].equals(32131L));
        assertTrue(args[8].equals(new Double(1000000)));
        assertTrue(args[9].equals(new Double(3.14159)));
        assertTrue(args[10].equals(Long.MIN_VALUE));
        assertTrue(args[11].equals(Long.MAX_VALUE));
    }

    public void testUntypedCollections() throws Exception
    {
        println("\nTestJsonWriter.testUntypedCollections()");
        Object[] poly = new Object[]{"Road Runner", 16L, 3.1415, true, false, null, 7, "Coyote", "Coyote"};
        String json = getJsonString(poly);
        println("json=" + json);
        assertTrue("[\"Road Runner\",16,3.1415,true,false,null,{\"@type\":\"int\",\"value\":7},\"Coyote\",\"Coyote\"]".equals(json));
        Collection col = new ArrayList();
        col.add("string");
        col.add(new Long(16));
        col.add(new Double(3.14159));
        col.add(Boolean.TRUE);
        col.add(Boolean.FALSE);
        col.add(null);
        col.add(new Integer(7));
        json = getJsonString(col);
        println("json=" + json);
        assertTrue("{\"@type\":\"java.util.ArrayList\",\"elementData\":[\"string\",16,3.14159,true,false,null,{\"@type\":\"int\",\"value\":7},null,null,null],\"size\":7,\"modCount\":7}".equals(json));
    }

    private static class TestByte implements Serializable
    {
        private final Byte _arrayElement;
        private final Byte[] _typeArray;
        private final Object[] _objArray;
        private final Object _polyRefTarget;
        private final Object _polyRef;
        private final Object _polyNotRef;
        private final Byte _min;
        private final Byte _max;
        private final Byte _null;

        private TestByte()
        {
            _arrayElement = new Byte((byte) -1);
            _polyRefTarget = new Byte((byte) 71);
            _polyRef = _polyRefTarget;
            _polyNotRef = new Byte((byte) 71);
            Byte local = new Byte((byte) 75);
            _null = null;
            _typeArray = new Byte[]{_arrayElement, (byte) 44, local, _null, null, new Byte((byte) 44)};
            _objArray = new Object[]{_arrayElement, (byte) 69, local, _null, null, new Byte((byte) 69)};
            _min = Byte.MIN_VALUE;
            _max = Byte.MAX_VALUE;
        }
    }

    public void testByte() throws Exception
    {
        println("\nTestJsonWriter.testByte()");
        TestByte test = new TestByte();
        String json = getJsonString(test);
        println("json = " + json);
        JsonReader jr = new JsonReader(convertStringToInputStream(json));
        TestByte that = (TestByte) readJsonObject(jr);

        assertTrue(that._arrayElement.equals((byte) -1));
        assertTrue(that._polyRefTarget.equals((byte) 71));
        assertTrue(that._polyRef.equals((byte) 71));
        assertTrue(that._polyNotRef.equals((byte) 71));
        assertTrue(addressEquals(that._polyRef, that._polyRefTarget));
        assertTrue(addressEquals(that._polyNotRef, that._polyRef));             // byte cache is working

        assertTrue(that._typeArray.length == 6);
        assertTrue(addressEquals(that._typeArray[0], that._arrayElement));  // byte cache is working
        assertTrue(that._typeArray[1] instanceof Byte);
        assertTrue(that._typeArray[1] instanceof Byte);
        assertTrue(that._typeArray[1].equals((byte) 44));
        assertTrue(that._objArray.length == 6);
        assertTrue(addressEquals(that._objArray[0], that._arrayElement));   // byte cache is working
        assertTrue(that._objArray[1] instanceof Byte);
        assertTrue(that._objArray[1].equals((byte) 69));
        assertTrue(that._polyRefTarget instanceof Byte);
        assertTrue(that._polyNotRef instanceof Byte);
        assertTrue(that._objArray[2].equals((byte) 75));

        assertTrue(that._null == null);
        assertTrue(that._typeArray[3] == null);
        assertTrue(that._typeArray[4] == null);
        assertTrue(that._objArray[3] == null);
        assertTrue(that._objArray[4] == null);

        assertTrue(that._min.equals(Byte.MIN_VALUE));
        assertTrue(that._max.equals(Byte.MAX_VALUE));
        assertTrue(addressEquals(that._min, Byte.MIN_VALUE));   // Verifies non-referenced byte caching is working
        assertTrue(addressEquals(that._max, Byte.MAX_VALUE)); // Verifies non-referenced byte caching is working

        time(test);
    }

    private static class TestShort implements Serializable
    {
        private final Short _arrayElement;
        private final Short[] _typeArray;
        private final Object[] _objArray;
        private final Object _polyRefTarget;
        private final Object _polyRef;
        private final Object _polyNotRef;
        private final Short _min;
        private final Short _max;
        private final Short _null;

        private TestShort()
        {
            _arrayElement = new Short((short) -1);
            _polyRefTarget = new Short((short) 710);
            _polyRef = _polyRefTarget;
            _polyNotRef = new Short((short) 710);
            Short local = new Short((short) 75);
            _null = null;
            _typeArray = new Short[]{_arrayElement, (short) 44, local, _null, null, new Short((short) 44)};
            _objArray = new Object[]{_arrayElement, (short) 69, local, _null, null, new Short((short) 69)};
            _min = Short.MIN_VALUE;
            _max = Short.MAX_VALUE;
        }
    }

    public void testShort() throws Exception
    {
        println("\nTestJsonWriter.testShort()");
        TestShort test = new TestShort();
        String json = getJsonString(test);
        println("json = " + json);
        JsonReader jr = new JsonReader(convertStringToInputStream(json));
        TestShort that = (TestShort) readJsonObject(jr);

        assertTrue(that._arrayElement.equals((short) -1));
        assertTrue(that._polyRefTarget.equals((short) 710));
        assertTrue(that._polyRef.equals((short) 710));
        assertTrue(that._polyNotRef.equals((short) 710));
        assertTrue(addressEquals(that._polyRef, that._polyRefTarget));
        assertFalse(addressEquals(that._polyNotRef, that._polyRef));

        assertTrue(that._typeArray.length == 6);
        assertTrue(addressEquals(that._typeArray[0], that._arrayElement));
        assertTrue(that._typeArray[1] instanceof Short);
        assertTrue(that._typeArray[1] instanceof Short);
        assertTrue(that._typeArray[1].equals((short) 44));
        assertTrue(that._objArray.length == 6);
        assertTrue(addressEquals(that._objArray[0], that._arrayElement));
        assertTrue(that._objArray[1] instanceof Short);
        assertTrue(that._objArray[1].equals((short) 69));
        assertTrue(that._polyRefTarget instanceof Short);
        assertTrue(that._polyNotRef instanceof Short);

        assertTrue(addressEquals(that._objArray[2], that._typeArray[2]));
        assertTrue(that._objArray[2].equals((short) 75));

        // Because of cache in Short.valueOf(), these values between -128 and 127 will have same address.
        assertTrue(addressEquals(that._typeArray[1], that._typeArray[5]));
        assertTrue(addressEquals(that._objArray[1], that._objArray[5]));

        assertTrue(that._null == null);
        assertTrue(that._typeArray[3] == null);
        assertTrue(that._typeArray[4] == null);
        assertTrue(that._objArray[3] == null);
        assertTrue(that._objArray[4] == null);

        assertTrue(that._min.equals(Short.MIN_VALUE));
        assertTrue(that._max.equals(Short.MAX_VALUE));
        assertFalse(addressEquals(that._min, Short.MIN_VALUE));
        assertFalse(addressEquals(that._max, Short.MAX_VALUE));
        time(test);
    }

    private static class TestInteger implements Serializable
    {
        private final Integer _arrayElement;
        private final Integer[] _typeArray;
        private final Object[] _objArray;
        private final Object _polyRefTarget;
        private final Object _polyRef;
        private final Object _polyNotRef;
        private final Integer _min;
        private final Integer _max;
        private final Integer _null;

        private TestInteger()
        {
            _arrayElement = new Integer(-1);
            _polyRefTarget = new Integer(710);
            _polyRef = _polyRefTarget;
            _polyNotRef = new Integer(710);
            Integer local = new Integer(75);
            _null = null;
            _typeArray = new Integer[]{_arrayElement, 44, local, _null, null, new Integer(44), 0, new Integer(0)};
            _objArray = new Object[]{_arrayElement, 69, local, _null, null, new Integer(69), 0, new Integer(0)};
            _min = Integer.MIN_VALUE;
            _max = Integer.MAX_VALUE;
        }
    }

    public void testInteger() throws Exception
    {
        println("\nTestJsonWriter.testInteger()");
        TestInteger test = new TestInteger();
        String json = getJsonString(test);
        println("json = " + json);
        JsonReader jr = new JsonReader(convertStringToInputStream(json));
        TestInteger that = (TestInteger) readJsonObject(jr);

        assertTrue(that._arrayElement.equals(-1));
        assertTrue(that._polyRefTarget.equals(710));
        assertTrue(that._polyRef.equals(710));
        assertTrue(that._polyNotRef.equals(710));
        assertTrue(addressEquals(that._polyRef, that._polyRefTarget));
        assertFalse(addressEquals(that._polyNotRef, that._polyRef));

        assertTrue(that._typeArray.length == 8);
        assertTrue(addressEquals(that._typeArray[0], that._arrayElement));
        assertTrue(that._typeArray[1] instanceof Integer);
        assertTrue(that._typeArray[1] instanceof Integer);
        assertTrue(that._typeArray[1].equals(44));
        assertTrue(that._objArray.length == 8);
        assertTrue(addressEquals(that._objArray[0], that._arrayElement));
        assertTrue(that._objArray[1] instanceof Integer);
        assertTrue(that._objArray[1].equals(69));
        assertTrue(that._polyRefTarget instanceof Integer);
        assertTrue(that._polyNotRef instanceof Integer);
        assertTrue(that._objArray[2].equals(75));

        assertTrue(addressEquals(that._objArray[2], that._typeArray[2]));
        assertTrue(addressEquals(that._typeArray[1], that._typeArray[5]));
        assertTrue(addressEquals(that._objArray[1], that._objArray[5]));
        // an unreferenced 0 is cached
        assertTrue(addressEquals(that._typeArray[6], that._typeArray[7]));
        assertTrue(addressEquals(that._objArray[6], that._objArray[7]));

        assertTrue(that._null == null);
        assertTrue(that._typeArray[3] == null);
        assertTrue(that._typeArray[4] == null);
        assertTrue(that._objArray[3] == null);
        assertTrue(that._objArray[4] == null);

        assertTrue(that._min.equals(Integer.MIN_VALUE));
        assertTrue(that._max.equals(Integer.MAX_VALUE));
        assertFalse(addressEquals(that._min, Integer.MIN_VALUE));
        assertFalse(addressEquals(that._max, Integer.MAX_VALUE));
        time(test);
    }

    private static class TestLong implements Serializable
    {
        private final Long _arrayElement;
        private final Long[] _typeArray;
        private final Object[] _objArray;
        private final Object _polyRefTarget;
        private final Object _polyRef;
        private final Object _polyNotRef;
        private final Long _min;
        private final Long _max;
        private final Long _null;

        private TestLong()
        {
            _arrayElement = new Long(-1);
            _polyRefTarget = new Long(710);
            _polyRef = _polyRefTarget;
            _polyNotRef = new Long(710);
            Long local = new Long(75);
            _null = null;
            // 44 below is between -128 and 127, values cached by     Long Long.valueOf(long l)
            _typeArray = new Long[]{_arrayElement, 44L, local, _null, null, new Long(44)};
            _objArray = new Object[]{_arrayElement, 69L, local, _null, null, new Long(69)};
            _min = Long.MIN_VALUE;
            _max = Long.MAX_VALUE;
        }
    }

    public void testLong() throws Exception
    {
        println("\nTestJsonWriter.testLong()");
        TestLong test = new TestLong();
        String json = getJsonString(test);
        println("json = " + json);
        JsonReader jr = new JsonReader(convertStringToInputStream(json));
        TestLong that = (TestLong) readJsonObject(jr);

        assertTrue(that._arrayElement.equals(-1L));
        assertTrue(that._polyRefTarget.equals(710L));
        assertTrue(that._polyRef.equals(710L));
        assertTrue(that._polyNotRef.equals(710L));
        assertTrue(addressEquals(that._polyRef, that._polyRefTarget));
        assertFalse(addressEquals(that._polyNotRef, that._polyRef));

        assertTrue(that._typeArray.length == 6);
        assertTrue(addressEquals(that._typeArray[0], that._arrayElement));
        assertTrue(that._typeArray[1] instanceof Long);
        assertTrue(that._typeArray[1] instanceof Long);
        assertTrue(that._typeArray[1].equals(44L));
        assertTrue(that._objArray.length == 6);
        assertTrue(addressEquals(that._objArray[0], that._arrayElement));
        assertTrue(that._objArray[1] instanceof Long);
        assertTrue(that._objArray[1].equals(69L));
        assertTrue(that._polyRefTarget instanceof Long);
        assertTrue(that._polyNotRef instanceof Long);

        assertTrue(addressEquals(that._objArray[2], that._typeArray[2]));
        assertTrue(addressEquals(that._typeArray[1], that._typeArray[5]));
        assertTrue(addressEquals(that._objArray[1], that._objArray[5]));

        assertTrue(that._null == null);
        assertTrue(that._typeArray[3] == null);
        assertTrue(that._typeArray[4] == null);
        assertTrue(that._objArray[3] == null);
        assertTrue(that._objArray[4] == null);

        assertTrue(that._min.equals(Long.MIN_VALUE));
        assertTrue(that._max.equals(Long.MAX_VALUE));
        assertFalse(addressEquals(that._min, Long.MIN_VALUE));
        assertFalse(addressEquals(that._max, Long.MAX_VALUE));
        time(test);
    }

    private static class TestDouble implements Serializable
    {
        private final Double _arrayElement;
        private final Double[] _typeArray;
        private final Object[] _objArray;
        private final Object _polyRefTarget;
        private final Object _polyRef;
        private final Object _polyNotRef;
        private final Double _min;
        private final Double _max;
        private final Double _null;

        private TestDouble()
        {
            _arrayElement = new Double(-1);
            _polyRefTarget = new Double(71);
            _polyRef = _polyRefTarget;
            _polyNotRef = new Double(71);
            Double local = new Double(75);
            _null = null;
            _typeArray = new Double[]{_arrayElement, 44.0, local, _null, null, new Double(44)};
            _objArray = new Object[]{_arrayElement, 69.0, local, _null, null, new Double(69)};
            _min = Double.MIN_VALUE;
            _max = Double.MAX_VALUE;
        }
    }

    public void testDouble() throws Exception
    {
        println("\nTestJsonWriter.testDouble()");
        TestDouble test = new TestDouble();
        String json = getJsonString(test);
        println("json = " + json);
        JsonReader jr = new JsonReader(convertStringToInputStream(json));
        TestDouble that = (TestDouble) readJsonObject(jr);

        assertTrue(that._arrayElement.equals(-1.0));
        assertTrue(that._polyRefTarget.equals(71.0));
        assertTrue(that._polyRef.equals(71.0));
        assertTrue(that._polyNotRef.equals(71.0));
        assertTrue(addressEquals(that._polyRef, that._polyRefTarget));
        assertFalse(addressEquals(that._polyNotRef, that._polyRef));

        assertTrue(that._typeArray.length == 6);
        assertTrue(that._typeArray[1] instanceof Double);
        assertTrue(that._typeArray[1] instanceof Double);
        assertTrue(that._typeArray[1].equals(44.0));
        assertTrue(that._objArray.length == 6);
        assertTrue(that._objArray[1] instanceof Double);
        assertTrue(that._objArray[1].equals(69.0));
        assertTrue(that._polyRefTarget instanceof Double);
        assertTrue(that._polyNotRef instanceof Double);

        assertTrue(that._null == null);
        assertTrue(that._typeArray[3] == null);
        assertTrue(that._typeArray[4] == null);
        assertTrue(that._objArray[3] == null);
        assertTrue(that._objArray[4] == null);

        assertTrue(that._min.equals(Double.MIN_VALUE));
        assertTrue(that._max.equals(Double.MAX_VALUE));
        assertFalse(addressEquals(that._min, Double.MIN_VALUE));
        assertFalse(addressEquals(that._max, Double.MAX_VALUE));
        time(test);
    }

    private static class TestFloat implements Serializable
    {
        private final Float _arrayElement;
        private final Float[] _typeArray;
        private final Object[] _objArray;
        private final Object _polyRefTarget;
        private final Object _polyRef;
        private final Object _polyNotRef;
        private final Float _min;
        private final Float _max;
        private final Float _null;

        private TestFloat()
        {
            _arrayElement = new Float(-1);
            _polyRefTarget = new Float(71);
            _polyRef = _polyRefTarget;
            _polyNotRef = new Float(71);
            Float local = new Float(75);
            _null = null;
            _typeArray = new Float[]{_arrayElement, 44f, local, _null, null, new Float(44f)};
            _objArray = new Object[]{_arrayElement, 69f, local, _null, null, new Float(69f)};
            _min = Float.MIN_VALUE;
            _max = Float.MAX_VALUE;
        }
    }

    public void testFloat() throws Exception
    {
        println("\nTestJsonWriter.testFloat()");
        TestFloat test = new TestFloat();
        String json = getJsonString(test);
        println("json = " + json);
        JsonReader jr = new JsonReader(convertStringToInputStream(json));
        TestFloat that = (TestFloat) readJsonObject(jr);

        assertTrue(that._arrayElement.equals(-1.0f));
        assertTrue(that._polyRefTarget.equals(71.0f));
        assertTrue(that._polyRef.equals(71.0f));
        assertTrue(that._polyNotRef.equals(71.0f));
        assertTrue(addressEquals(that._polyRef, that._polyRefTarget));
        assertFalse(addressEquals(that._polyNotRef, that._polyRef));

        assertTrue(that._typeArray.length == 6);
        assertTrue(that._typeArray[1] instanceof Float);
        assertTrue(that._typeArray[1] instanceof Float);
        assertTrue(that._typeArray[1].equals(44.0f));
        assertTrue(that._objArray.length == 6);
        assertTrue(that._objArray[1] instanceof Float);
        assertTrue(that._objArray[1].equals(69.0f));
        assertTrue(that._polyRefTarget instanceof Float);
        assertTrue(that._polyNotRef instanceof Float);

        assertTrue(that._objArray[2].equals(that._typeArray[2]));
        assertFalse(addressEquals(that._typeArray[1], that._typeArray[5]));
        assertFalse(addressEquals(that._objArray[1], that._objArray[5]));
        assertFalse(addressEquals(that._typeArray[1], that._objArray[1]));
        assertFalse(addressEquals(that._typeArray[5], that._objArray[5]));

        assertTrue(that._null == null);
        assertTrue(that._typeArray[3] == null);
        assertTrue(that._typeArray[4] == null);
        assertTrue(that._objArray[3] == null);
        assertTrue(that._objArray[4] == null);

        assertTrue(that._min.equals(Float.MIN_VALUE));
        assertTrue(that._max.equals(Float.MAX_VALUE));
        assertFalse(addressEquals(that._min, Float.MIN_VALUE));
        assertFalse(addressEquals(that._max, Float.MAX_VALUE));
        time(test);
    }

    private static class TestBoolean implements Serializable
    {
        private final Boolean _arrayElement;
        private final Boolean[] _typeArray;
        private final Object[] _objArray;
        private final Object _polyRefTarget;
        private final Object _polyRef;
        private final Object _polyNotRef;
        private final Boolean _null;

        private TestBoolean()
        {
            _arrayElement = new Boolean(true);
            _polyRefTarget = new Boolean(true);
            _polyRef = _polyRefTarget;
            _polyNotRef = new Boolean(true);
            Boolean local = new Boolean(true);
            _null = null;
            _typeArray = new Boolean[]{_arrayElement, true, local, _null, null, Boolean.FALSE, new Boolean(false)};
            _objArray = new Object[]{_arrayElement, true, local, _null, null, Boolean.FALSE, new Boolean(false)};
        }
    }

    public void testBoolean() throws Exception
    {
        println("\nTestJsonWriter.testBoolean()");
        TestBoolean test = new TestBoolean();
        String json = getJsonString(test);
        println("json = " + json);
        JsonReader jr = new JsonReader(convertStringToInputStream(json));
        TestBoolean that = (TestBoolean) readJsonObject(jr);

        assertTrue(that._arrayElement.equals(true));
        assertTrue(that._polyRefTarget.equals(true));
        assertTrue(that._polyRef.equals(true));
        assertTrue(that._polyNotRef.equals(true));
        assertTrue(addressEquals(that._polyRef, that._polyRefTarget));
        assertTrue(addressEquals(that._polyNotRef, that._polyRef)); // because only Boolean.TRUE or Boolean.FALSE used

        assertTrue(that._typeArray.length == 7);
        assertTrue(addressEquals(that._typeArray[0], that._arrayElement));
        assertTrue(that._typeArray[1] instanceof Boolean);
        assertTrue(that._typeArray[1] instanceof Boolean);
        assertTrue(that._typeArray[1].equals(true));
        assertTrue(that._objArray.length == 7);
        assertTrue(addressEquals(that._objArray[0], that._arrayElement));
        assertTrue(that._objArray[1] instanceof Boolean);
        assertTrue(that._objArray[1].equals(true));
        assertTrue(that._polyRefTarget instanceof Boolean);
        assertTrue(that._polyNotRef instanceof Boolean);

        assertTrue(addressEquals(that._objArray[2], that._typeArray[2]));
        assertTrue(addressEquals(that._typeArray[5], that._objArray[5]));
        assertTrue(addressEquals(that._typeArray[6], that._objArray[6]));
        assertTrue(addressEquals(that._typeArray[5], that._typeArray[6]));
        assertTrue(addressEquals(that._objArray[5], that._objArray[6]));

        assertTrue(that._null == null);
        assertTrue(that._typeArray[3] == null);
        assertTrue(that._typeArray[4] == null);
        assertTrue(that._objArray[3] == null);
        assertTrue(that._objArray[4] == null);
        time(test);
    }

    private static class TestCharacter implements Serializable
    {
        private final Character _arrayElement;
        private final Character[] _typeArray;
        private final Object[] _objArray;
        private final Object _polyRefTarget;
        private final Object _polyRef;
        private final Object _polyNotRef;
        private final Character _min;
        private final Character _max;
        private final Character _null;

        private TestCharacter()
        {
            _arrayElement = new Character((char) 1);
            _polyRefTarget = new Character((char) 71);
            _polyRef = _polyRefTarget;
            _polyNotRef = new Character((char) 71);
            Character local = new Character((char) 75);
            _null = null;
            _typeArray = new Character[]{_arrayElement, 'a', local, _null, null};
            _objArray = new Object[]{_arrayElement, 'b', local, _null, null};
            _min = Character.MIN_VALUE;
            _max = Character.MAX_VALUE;
        }
    }

    public void testCharacter() throws Exception
    {
        println("\nTestJsonWriter.testCharacter()");
        TestCharacter test = new TestCharacter();
        String json = getJsonString(test);
        println("json = " + json);
        JsonReader jr = new JsonReader(convertStringToInputStream(json));
        TestCharacter that = (TestCharacter) readJsonObject(jr);

        assertTrue(that._arrayElement.equals((char) 1));
        assertTrue(that._polyRefTarget.equals((char) 71));
        assertTrue(that._polyRef.equals((char) 71));
        assertTrue(that._polyNotRef.equals((char) 71));
        assertTrue(addressEquals(that._polyRef, that._polyRefTarget));
        assertTrue(addressEquals(that._polyNotRef, that._polyRef));    // Character cache working

        assertTrue(that._typeArray.length == 5);
        assertTrue(addressEquals(that._typeArray[0], that._arrayElement));
        assertTrue(that._typeArray[1] instanceof Character);
        assertTrue(that._typeArray[1] instanceof Character);
        assertTrue(that._typeArray[1].equals('a'));
        assertTrue(that._objArray.length == 5);
        assertTrue(addressEquals(that._objArray[0], that._arrayElement));
        assertTrue(that._objArray[1] instanceof Character);
        assertTrue(that._objArray[1].equals('b'));
        assertTrue(that._polyRefTarget instanceof Character);
        assertTrue(that._polyNotRef instanceof Character);

        assertTrue(addressEquals(that._objArray[2], that._typeArray[2]));
        assertTrue(that._objArray[2].equals((char) 75));

        assertTrue(that._null == null);
        assertTrue(that._typeArray[3] == null);
        assertTrue(that._typeArray[4] == null);
        assertTrue(that._objArray[3] == null);
        assertTrue(that._objArray[4] == null);

        assertTrue(that._min.equals(Character.MIN_VALUE));
        assertTrue(that._max.equals(Character.MAX_VALUE));
        assertTrue(addressEquals(that._min, Character.MIN_VALUE));
        assertFalse(addressEquals(that._max, Character.MAX_VALUE));
        time(test);
    }

    private static class TestString implements Serializable
    {
        private static final int MAX_UTF8_CHAR = 1000;
        // Foreign characters test (UTF8 multi-byte chars)
        private final String _range;
        private String _utf8HandBuilt;
        private final String[] _strArray;
        private final Object[] _objArray;
        private final Object[] _objStrArray;
        private final Object[] _cache;
        private final Object _poly;
        private final String _null;

        private TestString()
        {
            _null = null;
            StringBuffer s = new StringBuffer();
            for (int i = 0; i < MAX_UTF8_CHAR; i++)
            {
                s.append((char) i);
            }
            _range = s.toString();

            // BYZANTINE MUSICAL SYMBOL PSILI
            try
            {
                byte[] symbol = {(byte) 0xf0, (byte) 0x9d, (byte) 0x80, (byte) 0x80};
                _utf8HandBuilt = new String(symbol, "UTF-8");
            }
            catch (UnsupportedEncodingException e)
            {
                System.out.println("Get a new JVM that supports UTF-8");
            }

            _strArray = new String[]{"1st", "2nd", _null, null, new String("3rd")};
            _objArray = new Object[]{"1st", "2nd", _null, null, new String("3rd")};
            _objStrArray = new String[]{"1st", "2nd", _null, null, new String("3rd")};
            _cache = new Object[]{"true", "true", "golf", "golf"};
            _poly = "Poly";
        }
    }

    public void testString() throws Exception
    {
        println("\nTestJsonWriter.testString()");
        TestString test = new TestString();
        String jsonOut = getJsonString(test);
        println(jsonOut);
        JsonReader jr = new JsonReader(convertStringToInputStream(jsonOut));
        TestString that = (TestString) readJsonObject(jr);

        for (int i = 0; i < TestString.MAX_UTF8_CHAR; i++)
        {
            assertTrue(that._range.charAt(i) == (char) i);
        }

        // UTF-8 serialization makes it through clean.
        byte[] bytes = that._utf8HandBuilt.getBytes("UTF-8");
        assertTrue(bytes[0] == (byte) 0xf0);
        assertTrue(bytes[1] == (byte) 0x9d);
        assertTrue(bytes[2] == (byte) 0x80);
        assertTrue(bytes[3] == (byte) 0x80);

        assertTrue(that._strArray.length == 5);
        assertTrue(that._objArray.length == 5);
        assertTrue(that._objStrArray.length == 5);
        assertTrue(that._strArray[2] == null);
        assertTrue(that._objArray[2] == null);
        assertTrue(that._objStrArray[2] == null);
        assertTrue(that._strArray[3] == null);
        assertTrue(that._objArray[3] == null);
        assertTrue(that._objStrArray[3] == null);
        assertTrue("Poly".equals(that._poly));

        assertTrue(addressEquals(that._cache[0], that._cache[1]));
        assertFalse(addressEquals(that._cache[2], that._cache[3]));

        time(test);
    }

    private static class TestDate implements Serializable
    {
        private final Date _arrayElement;
        private final Date[] _typeArray;
        private final Object[] _objArray;
        private final Object _polyRefTarget;
        private final Object _polyRef;
        private final Object _polyNotRef;
        private final Date _min;
        private final Date _max;
        private final Date _null;

        private TestDate()
        {
            _arrayElement = new Date(-1);
            _polyRefTarget = new Date(71);
            _polyRef = _polyRefTarget;
            _polyNotRef = new Date(71);
            Date local = new Date(75);
            _null = null;
            _typeArray = new Date[]{_arrayElement, new Date(69), local, _null, null, new Date(69)};
            _objArray = new Object[]{_arrayElement, new Date(69), local, _null, null, new Date(69)};
            _min = new Date(Long.MIN_VALUE);
            _max = new Date(Long.MAX_VALUE);
        }
    }

    public void testDate() throws Exception
    {
        println("\nTestJsonWriter.testDate()");
        TestDate test = new TestDate();
        String json = getJsonString(test);
        println("json = " + json);
        JsonReader jr = new JsonReader(convertStringToInputStream(json));
        TestDate that = (TestDate) readJsonObject(jr);

        assertTrue(that._arrayElement.equals(new Date(-1)));
        assertTrue(that._polyRefTarget.equals(new Date(71)));
        assertTrue(that._polyRef.equals(new Date(71)));
        assertTrue(that._polyNotRef.equals(new Date(71)));
        assertFalse(addressEquals(that._polyRef, that._polyRefTarget));
        assertFalse(addressEquals(that._polyNotRef, that._polyRef));

        assertTrue(that._typeArray.length == 6);
        assertFalse(addressEquals(that._typeArray[0], that._arrayElement));
        assertTrue(that._typeArray[1] instanceof Date);
        assertTrue(that._typeArray[1] instanceof Date);
        assertTrue(that._typeArray[1].equals(new Date(69)));
        assertTrue(that._objArray.length == 6);
        assertFalse(addressEquals(that._objArray[0], that._arrayElement));
        assertTrue(that._objArray[1] instanceof Date);
        assertTrue(that._objArray[1].equals(new Date(69)));
        assertTrue(that._polyRefTarget instanceof Date);
        assertTrue(that._polyNotRef instanceof Date);
        assertFalse(addressEquals(that._typeArray[1], that._typeArray[5]));
        assertFalse(addressEquals(that._objArray[1], that._objArray[5]));
        assertFalse(addressEquals(that._typeArray[1], that._objArray[1]));
        assertFalse(addressEquals(that._typeArray[5], that._objArray[5]));

        assertFalse(addressEquals(that._objArray[2], that._typeArray[2]));
        assertTrue(that._objArray[2].equals(new Date(75)));

        assertTrue(that._null == null);
        assertTrue(that._typeArray[3] == null);
        assertTrue(that._typeArray[4] == null);
        assertTrue(that._objArray[3] == null);
        assertTrue(that._objArray[4] == null);

        assertTrue(that._min.equals(new Date(Long.MIN_VALUE)));
        assertTrue(that._max.equals(new Date(Long.MAX_VALUE)));
        time(test);
    }

    private static class TestClass implements Serializable
    {
        private List _classes_a;

        private final Class _booleanClass;
        private final Class _BooleanClass;
        private final Object _booleanClassO;
        private final Object _BooleanClassO;
        private final Class[] _booleanClassArray;
        private final Class[] _BooleanClassArray;
        private final Object[] _booleanClassArrayO;
        private final Object[] _BooleanClassArrayO;

        private final Class _charClass;
        private final Class _CharacterClass;
        private final Object _charClassO;
        private final Object _CharacterClassO;
        private final Class[] _charClassArray;
        private final Class[] _CharacterClassArray;
        private final Object[] _charClassArrayO;
        private final Object[] _CharacterClassArrayO;

        private TestClass()
        {
            _classes_a = new ArrayList();
            _classes_a.add(char.class);
            _booleanClass = boolean.class;
            _BooleanClass = Boolean.class;
            _booleanClassO = boolean.class;
            _BooleanClassO = Boolean.class;
            _booleanClassArray = new Class[]{boolean.class};
            _BooleanClassArray = new Class[]{Boolean.class};
            _booleanClassArrayO = new Object[]{boolean.class};
            _BooleanClassArrayO = new Object[]{Boolean.class};

            _charClass = char.class;
            _CharacterClass = Character.class;
            _charClassO = char.class;
            _CharacterClassO = Character.class;
            _charClassArray = new Class[]{char.class};
            _CharacterClassArray = new Class[]{Character.class};
            _charClassArrayO = new Object[]{char.class};
            _CharacterClassArrayO = new Object[]{Character.class};
        }
    }

    public void testClass() throws Exception
    {
        println("\nTestJsonWriter.testClass()");
        TestClass test = new TestClass();
        String json = getJsonString(test);
        println("json = " + json);
        JsonReader jr = new JsonReader(convertStringToInputStream(json));
        TestClass that = (TestClass) readJsonObject(jr);

        assertTrue(that._classes_a.get(0) == char.class);

        assertTrue(boolean.class == that._booleanClass);
        assertTrue(Boolean.class == that._BooleanClass);
        assertTrue(boolean.class == that._booleanClassO);
        assertTrue(Boolean.class == that._BooleanClassO);
        assertTrue(boolean.class == that._booleanClassArray[0]);
        assertTrue(Boolean.class == that._BooleanClassArray[0]);
        assertTrue(boolean.class == that._booleanClassArrayO[0]);
        assertTrue(Boolean.class == that._BooleanClassArrayO[0]);

        assertTrue(char.class == that._charClass);
        assertTrue(Character.class == that._CharacterClass);
        assertTrue(char.class == that._charClassO);
        assertTrue(Character.class == that._CharacterClassO);
        assertTrue(char.class == that._charClassArray[0]);
        assertTrue(Character.class == that._CharacterClassArray[0]);
        assertTrue(char.class == that._charClassArrayO[0]);
        assertTrue(Character.class == that._CharacterClassArrayO[0]);
        time(test);
    }

    private static class TestSet implements Serializable
    {
        private Set _hashSet;
        private Set _treeSet;

        private void init()
        {
            _hashSet = new HashSet();
            _hashSet.add("alpha");
            _hashSet.add("bravo");
            _hashSet.add("charlie");
            _hashSet.add("delta");
            _hashSet.add("echo");
            _hashSet.add("foxtrot");
            _hashSet.add("golf");
            _hashSet.add("hotel");
            _hashSet.add("indigo");
            _hashSet.add("juliet");
            _hashSet.add("kilo");
            _hashSet.add("lima");
            _hashSet.add("mike");
            _hashSet.add("november");
            _hashSet.add("oscar");
            _hashSet.add("papa");
            _hashSet.add("quebec");
            _hashSet.add("romeo");
            _hashSet.add("sierra");
            _hashSet.add("tango");
            _hashSet.add("uniform");
            _hashSet.add("victor");
            _hashSet.add("whiskey");
            _hashSet.add("xray");
            _hashSet.add("yankee");
            _hashSet.add("zulu");

            _treeSet = new TreeSet();
            _treeSet.addAll(_hashSet);
        }

        private TestSet()
        {
        }
    }

    public void testSet() throws Exception
    {
        println("\nTestJsonWriter.testSet()");
        TestSet set = new TestSet();
        set.init();
        String json = getJsonString(set);

        JsonReader jr = new JsonReader(convertStringToInputStream(json));
        TestSet testSet = (TestSet) readJsonObject(jr);
        println("json = " + json);

        assertTrue(testSet._treeSet.size() == 26);
        assertTrue(testSet._hashSet.size() == 26);
        assertTrue(testSet._treeSet.containsAll(testSet._hashSet));
        assertTrue(testSet._hashSet.containsAll(testSet._treeSet));

        time(testSet);
    }

    public void testMap2() throws Exception
    {
        TestObject a = new TestObject("A");
        TestObject b = new TestObject("B");
        TestObject c = new TestObject("C");
        a._other = b;
        b._other = c;
        c._other = a;

        Map map = new HashMap();
        map.put(a, b);
        String json = getJsonString(map);
        println("json = " + json);
        JsonReader jr = new JsonReader(convertStringToInputStream(json));
        map = (Map) readJsonObject(jr);
        assertTrue(map != null);
        assertTrue(map.size() == 1);
        TestObject bb = (TestObject) map.get(new TestObject("A"));
        assertTrue(bb._other.equals(new TestObject("C")));
        TestObject aa = (TestObject) map.keySet().toArray()[0];
        assertTrue(aa._other == bb);
    }

    /**
     * Test the ability to use custom Json writer/reader to permit objects to
     * do their own output.  The scope of what is to be written is
     * that an object or array is already started.  It is expecting a valid
     * field / value pairings.  Note the field could have a JSON subobject if the
     * writer chooses to output the contents of an collection, for example.
     */
    public static class TestCustom implements Serializable
    {
        private String _nerf;
        private String _slime;
        private int _age;
        private boolean _truth;
        private Date _christBday;
        private Double _cost;
        private Float _x;
        private Class _klass;
        private char _letter;

        public TestCustom()
        {
        }

        private void init()
        {
            _nerf = "ball";
            _slime = "green";
            _age = 42;
            _truth = true;
            Calendar c = Calendar.getInstance();
            c.set(2010, 11, 25, 16, 20, 0);
            _christBday = c.getTime();
            _cost = 57.0;
            _x = 3.14159f;
            _klass = Rectangle.class;
            _letter = 'j';
        }

        public void _writeJson(Writer writer) throws IOException
        {
            writer.write("\"nurf\":\"" + _nerf + "\",");
            writer.write("\"slime\":\"" + _slime + "\",");
            writer.write("\"Age\":" + _age + ',');
            writer.write("\"truth\":" + _truth + ',');
            writer.write("\"christmas\":" + _christBday.getTime() + ',');
            writer.write("\"dollars\":" + _cost + ',');
            writer.write("\"x\":" + _x + ',');
            writer.write("\"class\":\"" + _klass.getClass().getName() + "\",");
            writer.write("\"letter\":\"" + _letter + '\"');
        }

        public void _readJson(Map obj)
        {
            _nerf = (String) obj.get("nurf");
            _slime = (String) obj.get("slime");
            _age = ((Long) obj.get("Age")).intValue();
            _truth = (Boolean) obj.get("truth");
            _christBday = new Date((Long) obj.get("christmas"));
            _cost = (Double) obj.get("dollars");
            _x = ((Double) obj.get("x")).floatValue();
            try
            {
                _klass = Class.forName((String) obj.get("class"));
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            _letter = ((String) obj.get("letter")).charAt(0);
        }
    }

    public void testCustom() throws Exception
    {
        println("\nTestJsonWriter.testCustom()");
        TestCustom custom = new TestCustom();
        custom.init();
        time(custom);
        String json = getJsonString(custom);
        println("json = " + json);

        JsonReader jr = new JsonReader(convertStringToInputStream(json));
        TestCustom custom1 = (TestCustom) readJsonObject(jr);
        assertTrue("ball".equals(custom1._nerf));
        assertTrue("green".equals(custom1._slime));
        assertTrue(42 == custom1._age);
        assertTrue(custom1._truth);
    }

    public class A
    {
        public String a;

        class B
        {

            public String b;

            public B()
            {
                // No args constructor for B
            }
        }
    }

    public void testInner() throws Exception
    {
        A a = new A();
        a.a = "aaa";

        String json = getJsonString(a);
        JsonReader jr = new JsonReader(convertStringToInputStream(json));
        A o1 = (A) readJsonObject(jr);
        assertTrue(o1.a.equals("aaa"));

        TestJsonReaderWriter.A.B b = a.new B();
        b.b = "bbb";
        json = getJsonString(b);
        jr = new JsonReader(convertStringToInputStream(json));
        TestJsonReaderWriter.A.B o2 = (TestJsonReaderWriter.A.B) readJsonObject(jr);
        assertTrue(o2.b.equals("bbb"));
    }

    private static void println(Object... args)
    {
        if (_debug)
        {
            for (Object arg : args)
            {
                System.out.println(arg);
            }
        }
    }

    private static boolean addressEquals(Object one, Object two)
    {
        return System.identityHashCode(one) == System.identityHashCode(two);
    }

    private String getJsonString(Object obj) throws Exception
    {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        JsonWriter jsonWriter = new JsonWriter(bout);
        _startWrite = System.nanoTime();
        jsonWriter.write(obj);
        _endWrite = System.nanoTime();
        return new String(bout.toByteArray(), "UTF-8");
    }

    private static InputStream convertStringToInputStream(String in) throws Exception
    {
        return new ByteArrayInputStream(in.getBytes("UTF-8"));
    }

    private void time(Object root) throws Exception
    {
        long startWrite = System.nanoTime();
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bo);
        out.writeObject(root);
        out.flush();
        long endWrite = System.nanoTime();
        out.close();

        long startRead = System.nanoTime();
        ByteArrayInputStream bin = new ByteArrayInputStream(bo.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bin);
        in.readObject();
        long endRead = System.nanoTime();
        in.close();

        println("JSON write time = " + (_endWrite - _startWrite) / 1000000 + " ms");
        println("ObjectOutputStream time = " + (endWrite - startWrite) / 1000000 + " ms");
        println("JSON  read time  = " + (_endRead - _startRead) / 1000000 + " ms");
        println("ObjectInputStream time = " + (endRead - startRead) / 1000000 + " ms");
    }

    private Object readJsonObject(JsonReader jr) throws Exception
    {
        _startRead = System.nanoTime();
        Object ret = jr.readObject();
        _endRead = System.nanoTime();
        return ret;
    }
}
