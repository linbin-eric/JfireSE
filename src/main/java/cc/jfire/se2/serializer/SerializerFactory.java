package cc.jfire.se2.serializer;

import cc.jfire.se2.JfireSE;
import cc.jfire.se2.serializer.impl.ArraySerializer;
import cc.jfire.se2.serializer.impl.BoxedArraySerializer;
import cc.jfire.se2.serializer.impl.BoxedTypeSerializer;
import cc.jfire.se2.serializer.impl.PrimitiveArraySerializer;
import cc.jfire.se2.serializer.impl.jdk.*;
import cc.jfire.se2.serializer.impl.ObjectSerializer.ObjectSerializer;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class SerializerFactory
{
    private Map<Class<?>, Serializer> store = new ConcurrentHashMap<>();
    private JfireSE                   jfireSE;

    public SerializerFactory(JfireSE jfireSE)
    {
        this.jfireSE = jfireSE;
        store.put(int[].class, new PrimitiveArraySerializer.IntArraySerializer());
        store.put(long[].class, new PrimitiveArraySerializer.LongArraySerializer());
        store.put(byte[].class, new PrimitiveArraySerializer.ByteArraySerializer());
        store.put(boolean[].class, new PrimitiveArraySerializer.BooleanArraySerializer());
        store.put(char[].class, new PrimitiveArraySerializer.CharArraySerializer());
        store.put(short[].class, new PrimitiveArraySerializer.ShortArraySerializer());
        store.put(double[].class, new PrimitiveArraySerializer.DoubleArraySerializer());
        store.put(float[].class, new PrimitiveArraySerializer.FloatArraySerializer());
        store.put(Integer[].class, new BoxedArraySerializer.IntegerArraySerializer());
        store.put(Long[].class, new BoxedArraySerializer.LongArraySerializer());
        store.put(Byte[].class, new BoxedArraySerializer.ByteArraySerializer());
        store.put(Boolean[].class, new BoxedArraySerializer.BooleanArraySerializer());
        store.put(Character[].class, new BoxedArraySerializer.CharArraySerializer());
        store.put(Short[].class, new BoxedArraySerializer.ShortArraySerializer());
        store.put(Double[].class, new BoxedArraySerializer.DoubleArraySerializer());
        store.put(Float[].class, new BoxedArraySerializer.FloatArraySerializer());
        store.put(String[].class, new BoxedArraySerializer.StringArraySerializer());
        store.put(String.class, new BoxedTypeSerializer.StringSerializer());
        store.put(Byte.class, new BoxedTypeSerializer.ByteSerializer());
        store.put(Integer.class, new BoxedTypeSerializer.IntegerSerializer());
        store.put(Character.class, new BoxedTypeSerializer.CharacterSerializer());
        store.put(Long.class, new BoxedTypeSerializer.LongSerializer());
        store.put(Float.class, new BoxedTypeSerializer.FloatSerializer());
        store.put(Double.class, new BoxedTypeSerializer.FloatSerializer());
        store.put(Boolean.class, new BoxedTypeSerializer.BooleanSerializer());
        store.put(Short.class, new BoxedTypeSerializer.ShortSerializer());
        store.put(ArrayList.class, new ListSerializer(jfireSE, ArrayList.class));
        store.put(LinkedList.class, new ListSerializer(jfireSE, LinkedList.class));
        store.put(HashMap.class, new MapSerializer(jfireSE, HashMap.class));
        store.put(TreeMap.class, new MapSerializer(jfireSE, TreeMap.class));
        store.put(ConcurrentHashMap.class, new MapSerializer(jfireSE, ConcurrentHashMap.class));
        store.put(LinkedHashMap.class, new MapSerializer(jfireSE, LinkedHashMap.class));
        store.put(HashSet.class, new SetSerializer(HashSet.class, jfireSE));
        store.put(TreeSet.class, new SetSerializer(TreeSet.class, jfireSE));
        store.put(ConcurrentSkipListSet.class, new SetSerializer(ConcurrentSkipListSet.class, jfireSE));
        store.put(LinkedHashSet.class, new SetSerializer(LinkedHashSet.class, jfireSE));
        store.put(Date.class, new DateSerializer());
        store.put(java.sql.Date.class, new SqlDateSerializer());
        store.put(Calendar.getInstance().getClass(), new CalendarSerializer());
        store.put(Method.class, new MethodSerializer());
        store.put(Class.class, new ClassSerializer());
    }

    public Serializer getSerializer(Class<?> clazz)
    {
        Serializer match = store.get(clazz);
        if (match != null)
        {
            return match;
        }
        else
        {
            if (clazz.isArray())
            {
                ArraySerializer arraySerializer = new ArraySerializer(clazz, jfireSE);
                store.putIfAbsent(clazz, arraySerializer);
                arraySerializer.init();
                return arraySerializer;
            }
            else
            {
                Serializer serializer = ObjectSerializer.buildCompileVersion(clazz, jfireSE);
                store.putIfAbsent(clazz, serializer);
                serializer.init();
                return serializer;
            }
        }
    }
}
