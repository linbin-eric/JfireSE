package com.jfirer.se2.serializer.impl.ObjectSerializer;

import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.baseutil.smc.compiler.CompileHelper;
import com.jfirer.baseutil.smc.model.ClassModel;
import com.jfirer.baseutil.smc.model.ConstructorModel;
import com.jfirer.baseutil.smc.model.FieldModel;
import com.jfirer.baseutil.smc.model.MethodModel;
import com.jfirer.se2.ByteArray;
import com.jfirer.se2.JfireSE;
import com.jfirer.se2.JfireSEImpl;
import com.jfirer.se2.classinfo.ClassInfo;
import com.jfirer.se2.classinfo.RefTracking;
import com.jfirer.se2.serializer.Serializer;
import io.github.karlatemp.unsafeaccessor.Unsafe;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Predicate;

public class ObjectSerializer implements Serializer
{
    private final        FieldInfo[] fieldInfos;
    private final        Class<?>    clazz;
    private static       int         COMPILE_COUNT = 1;
    private static final Unsafe      UNSAFE        = Unsafe.getUnsafe();

    private ObjectSerializer(Class<?> clazz, JfireSE jfireSE)
    {
        fieldInfos = parse(clazz, jfireSE).toArray(FieldInfo[]::new);
        this.clazz = clazz;
    }

    @Override
    public void init()
    {
        for (FieldInfo each : fieldInfos)
        {
            each.init();
        }
    }

    @Override
    public void writeBytes(ByteArray byteArray, Object instance)
    {
        for (FieldInfo each : fieldInfos)
        {
            each.write(byteArray, instance);
        }
    }

    @Override
    public Object read(ByteArray byteArray, RefTracking refTracking)
    {
        try
        {
            Object instance = UNSAFE.allocateInstance(clazz);
            if (refTracking != null)
            {
                refTracking.addTracking(instance);
            }
            for (FieldInfo each : fieldInfos)
            {
                each.read(byteArray, instance);
            }
            return instance;
        }
        catch (InstantiationException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static List<FieldInfo> parse(Class<?> clazz, JfireSE jfireSE)
    {
        Class       type   = clazz;
        List<Field> fields = new ArrayList<>();
        while (type != Object.class)
        {
            fields.addAll(Arrays.stream(type.getDeclaredFields()).filter(Predicate.not(field -> Modifier.isStatic(field.getModifiers()))) //
                                .filter(field -> !Modifier.isTransient(field.getModifiers())) //
                                .toList());
            type = type.getSuperclass();
        }
        List<PrimitiveFieldInfo>    primitiveFieldInfos    = new LinkedList<>();
        List<BoxedFieldInfo>        boxedFieldInfos        = new LinkedList<>();
        List<VariableFieldInfo>     variableFieldInfos     = new LinkedList<>();
        List<FinalFieldInfo>        finalFieldInfos        = new LinkedList<>();
        List<UnknowObjectFieldInfo> unknowObjectFieldInfos = new LinkedList<>();
        for (Field each : fields)
        {
            if (each.getType().isPrimitive())
            {
                primitiveFieldInfos.add(new PrimitiveFieldInfo(each));
            }
            else if (ReflectUtil.isPrimitiveBox(each.getType()) || each.getType() == String.class)
            {
                boxedFieldInfos.add(new BoxedFieldInfo(each));
            }
            else if (each.getType() == Object.class || each.getType().isInterface())
            {
                unknowObjectFieldInfos.add(new UnknowObjectFieldInfo(each, jfireSE));
            }
            else if (Modifier.isFinal(each.getType().getModifiers()))
            {
                finalFieldInfos.add(new FinalFieldInfo(each, jfireSE));
            }
            else
            {
                variableFieldInfos.add(new VariableFieldInfo(each, jfireSE));
            }
        }
        List<FieldInfo> list = new LinkedList<>();
        list.addAll(primitiveFieldInfos);
        list.addAll(boxedFieldInfos);
        list.addAll(finalFieldInfos);
        list.addAll(variableFieldInfos);
        list.addAll(unknowObjectFieldInfos);
        list.sort(Comparator.comparing(o -> o.getField().getType().getName()));
        return list;
    }

    public static Serializer buildSlowVersion(Class<?> clazz, JfireSE jfireSE)
    {
        return new ObjectSerializer(clazz, jfireSE);
    }

    public static Serializer buildCompileVersion(Class<?> clazz, JfireSE jfireSE)
    {
        List<FieldInfo> parse      = parse(clazz, jfireSE);
        ClassModel      classModel = new ClassModel("ObjectSerializer_compile_" + COMPILE_COUNT);
        COMPILE_COUNT++;
        classModel.addInterface(Serializer.class);
        classModel.addImport(Unsafe.class);
        classModel.addImport(RefTracking.class);
        classModel.addImport(JfireSEImpl.class);
        classModel.addImport(JfireSE.class);
        classModel.addImport(Integer.class);
        classModel.addImport(Short.class);
        classModel.addImport(Byte.class);
        classModel.addImport(Long.class);
        classModel.addImport(Double.class);
        classModel.addImport(Float.class);
        classModel.addImport(Boolean.class);
        classModel.addImport(Character.class);
        classModel.addImport(String.class);
        classModel.addImport(List.class);
        classModel.addImport(FieldInfo.class);
        classModel.addImport(ByteArray.class);
        classModel.addField(new FieldModel("UNSAFE", Unsafe.class, "Unsafe.getUnsafe()", classModel));
        classModel.addField(new FieldModel("jfireSE", JfireSE.class, classModel));
        classModel.addField(new FieldModel("clazz", Class.class, classModel));
        classModel.addField(new FieldModel("list_serializer_compile", List.class, classModel));
        ConstructorModel constructorModel = new ConstructorModel(classModel);
        constructorModel.setParamTypes(Class.class, JfireSE.class, List.class);
        constructorModel.setParamNames("clazz", "jfireSE", "list");
        constructorModel.setBody("""
                                         this.jfireSE=jfireSE;
                                         this.clazz=clazz;
                                         this.list_serializer_compile = list;""");
        classModel.addConstructor(constructorModel);
        StringBuilder initBody = new StringBuilder();
        try
        {
            MethodModel writeMethod = new MethodModel(Serializer.class.getDeclaredMethod("writeBytes", ByteArray.class, Object.class), classModel);
            writeMethod.setParamterNames("byteArray", "instance");
            StringBuilder writeBody = new StringBuilder();
            MethodModel readMethod = new MethodModel(Serializer.class.getDeclaredMethod("read", ByteArray.class, RefTracking.class), classModel);
            readMethod.setParamterNames("byteArray", "refTracking");
            StringBuilder readBody   = new StringBuilder();
            int           fieldIndex = 0;
            for (FieldInfo fieldInfo : parse)
            {
                long l = UNSAFE.objectFieldOffset(fieldInfo.field);
                if (fieldInfo instanceof PrimitiveFieldInfo)
                {
                    switch (fieldInfo.classId)
                    {
                        case ReflectUtil.PRIMITIVE_BYTE ->
                        {
                            writeBody.append("byteArray.put(UNSAFE.getByte(instance, " + l + "));\r\n");
                            readBody.append("UNSAFE.putByte(instance," + l + ", byteArray.get());\r\n");
                        }
                        case ReflectUtil.PRIMITIVE_INT ->
                        {
                            writeBody.append("byteArray.writeVarInt(UNSAFE.getInt(instance, " + l + "));\r\n");
                            readBody.append("UNSAFE.putInt(instance," + l + ", byteArray.readVarInt());\r\n");
                        }
                        case ReflectUtil.PRIMITIVE_SHORT ->
                        {
                            writeBody.append("byteArray.writeVarInt(UNSAFE.getShort(instance, " + l + "));\r\n");
                            readBody.append("UNSAFE.putShort(instance," + l + ", (short) byteArray.readVarInt());\r\n");
                        }
                        case ReflectUtil.PRIMITIVE_LONG ->
                        {
                            writeBody.append("byteArray.writeVarLong(UNSAFE.getLong(instance, " + l + "));\r\n");
                            readBody.append("UNSAFE.putLong(instance," + l + ", byteArray.readVarLong());\r\n");
                        }
                        case ReflectUtil.PRIMITIVE_FLOAT ->
                        {
                            writeBody.append("byteArray.writeFloat(UNSAFE.getFloat(instance, " + l + "));\r\n");
                            readBody.append("UNSAFE.putFloat(instance," + l + ", byteArray.readFloat());\r\n");
                        }
                        case ReflectUtil.PRIMITIVE_DOUBLE ->
                        {
                            writeBody.append("byteArray.writeDouble(UNSAFE.getDouble(instance, " + l + "));\r\n");
                            readBody.append("UNSAFE.putDouble(instance," + l + ", byteArray.readDouble());\r\n");
                        }
                        case ReflectUtil.PRIMITIVE_BOOL ->
                        {
                            writeBody.append("byteArray.writeBoolean(UNSAFE.getBoolean(instance, " + l + "));\r\n");
                            readBody.append("UNSAFE.putBoolean(instance," + l + ", byteArray.readBoolean());\r\n");
                        }
                        case ReflectUtil.PRIMITIVE_CHAR ->
                        {
                            writeBody.append("byteArray.writeChar(UNSAFE.getChar(instance, " + l + "));\r\n");
                            readBody.append("UNSAFE.putChar(instance," + l + ", byteArray.readChar());\r\n");
                        }
                        default -> throw new RuntimeException("不支持的类型");
                    }
                }
                else if (fieldInfo instanceof BoxedFieldInfo)
                {
                    switch (fieldInfo.classId)
                    {
                        case ReflectUtil.CLASS_BYTE ->
                        {
                            writeBody.append("""
                                                     {
                                                       Byte obj = (Byte)UNSAFE.getReference(instance,offset);
                                                       if(obj==null)
                                                       {
                                                           byteArray.put(JfireSE.NULL);
                                                       }
                                                       else
                                                       {
                                                           byteArray.put(JfireSE.NOT_NULL);
                                                           byteArray.put(obj);
                                                       }
                                                     }
                                                     """.replace("offset", String.valueOf(l)));
                            readBody.append("""
                                                    {
                                                        if (byteArray.get() == JfireSE.NOT_NULL)
                                                        {
                                                            UNSAFE.putReference(instance,offset, byteArray.get());
                                                        }
                                                    }
                                                    """.replace("offset", String.valueOf(l)));
                        }
                        case ReflectUtil.CLASS_INT ->
                        {
                            writeBody.append("""
                                                     {
                                                       Integer obj = (Integer)UNSAFE.getReference(instance,offset);
                                                       if(obj==null)
                                                       {
                                                           byteArray.put(JfireSE.NULL);
                                                       }
                                                       else
                                                       {
                                                           byteArray.put(JfireSE.NOT_NULL);
                                                           byteArray.writeVarInt(obj);
                                                       }
                                                     }
                                                     """.replace("offset", String.valueOf(l)));
                            readBody.append("""
                                                    {
                                                        if (byteArray.get() == JfireSE.NOT_NULL)
                                                        {
                                                            UNSAFE.putReference(instance,offset, byteArray.readVarInt());
                                                        }
                                                    }
                                                    """.replace("offset", String.valueOf(l)));
                        }
                        case ReflectUtil.CLASS_SHORT ->
                        {
                            writeBody.append("""
                                                     {
                                                       Short obj = (Short)UNSAFE.getReference(instance,offset);
                                                       if(obj==null)
                                                       {
                                                           byteArray.put(JfireSE.NULL);
                                                       }
                                                       else
                                                       {
                                                           byteArray.put(JfireSE.NOT_NULL);
                                                           byteArray.writeVarInt(obj);
                                                       }
                                                     }
                                                     """.replace("offset", String.valueOf(l)));
                            readBody.append("""
                                                    {
                                                        if (byteArray.get() == JfireSE.NOT_NULL)
                                                        {
                                                            UNSAFE.putReference(instance,offset, Short.valueOf((short)byteArray.readVarInt()));
                                                        }
                                                    }
                                                    """.replace("offset", String.valueOf(l)));
                        }
                        case ReflectUtil.CLASS_LONG ->
                        {
                            writeBody.append("""
                                                     {
                                                       Long obj = (Long)UNSAFE.getReference(instance,offset);
                                                       if(obj==null)
                                                       {
                                                           byteArray.put(JfireSE.NULL);
                                                       }
                                                       else
                                                       {
                                                           byteArray.put(JfireSE.NOT_NULL);
                                                           byteArray.writeVarLong(obj);
                                                       }
                                                     }
                                                     """.replace("offset", String.valueOf(l)));
                            readBody.append("""
                                                    {
                                                        if (byteArray.get() == JfireSE.NOT_NULL)
                                                        {
                                                            UNSAFE.putReference(instance,offset, byteArray.readVarLong());
                                                        }
                                                    }
                                                    """.replace("offset", String.valueOf(l)));
                        }
                        case ReflectUtil.CLASS_FLOAT ->
                        {
                            writeBody.append("""
                                                     {
                                                       Float obj = (Float)UNSAFE.getReference(instance,offset);
                                                       if(obj==null)
                                                       {
                                                           byteArray.put(JfireSE.NULL);
                                                       }
                                                       else
                                                       {
                                                           byteArray.put(JfireSE.NOT_NULL);
                                                           byteArray.writeFloat(obj);
                                                       }
                                                     }
                                                     """.replace("offset", String.valueOf(l)));
                            readBody.append("""
                                                    {
                                                        if (byteArray.get() == JfireSE.NOT_NULL)
                                                        {
                                                            UNSAFE.putReference(instance,offset, byteArray.readFloat());
                                                        }
                                                    }
                                                    """.replace("offset", String.valueOf(l)));
                        }
                        case ReflectUtil.CLASS_DOUBLE ->
                        {
                            writeBody.append("""
                                                     {
                                                       Double obj = (Double)UNSAFE.getReference(instance,offset);
                                                       if(obj==null)
                                                       {
                                                           byteArray.put(JfireSE.NULL);
                                                       }
                                                       else
                                                       {
                                                           byteArray.put(JfireSE.NOT_NULL);
                                                           byteArray.writeDouble(obj);
                                                       }
                                                     }
                                                     """.replace("offset", String.valueOf(l)));
                            readBody.append("""
                                                    {
                                                        if (byteArray.get() == JfireSE.NOT_NULL)
                                                        {
                                                            UNSAFE.putReference(instance,offset, byteArray.readDouble());
                                                        }
                                                    }
                                                    """.replace("offset", String.valueOf(l)));
                        }
                        case ReflectUtil.CLASS_BOOL ->
                        {
                            writeBody.append("""
                                                     {
                                                       Boolean obj = (Boolean)UNSAFE.getReference(instance,offset);
                                                       if(obj==null)
                                                       {
                                                           byteArray.put(JfireSE.NULL);
                                                       }
                                                       else
                                                       {
                                                           byteArray.put(JfireSE.NOT_NULL);
                                                           byteArray.writeBoolean(obj);
                                                       }
                                                     }
                                                     """.replace("offset", String.valueOf(l)));
                            readBody.append("""
                                                    {
                                                        if (byteArray.get() == JfireSE.NOT_NULL)
                                                        {
                                                            UNSAFE.putReference(instance,offset, byteArray.readBoolean());
                                                        }
                                                    }
                                                    """.replace("offset", String.valueOf(l)));
                        }
                        case ReflectUtil.CLASS_CHAR ->
                        {
                            writeBody.append("""
                                                     {
                                                       Character obj = (Character)UNSAFE.getReference(instance,offset);
                                                       if(obj==null)
                                                       {
                                                           byteArray.put(JfireSE.NULL);
                                                       }
                                                       else
                                                       {
                                                           byteArray.put(JfireSE.NOT_NULL);
                                                           byteArray.writeChar(obj);
                                                       }
                                                     }
                                                     """.replace("offset", String.valueOf(l)));
                            readBody.append("""
                                                    {
                                                        if (byteArray.get() == JfireSE.NOT_NULL)
                                                        {
                                                            UNSAFE.putReference(instance,offset, byteArray.readChar());
                                                        }
                                                    }
                                                    """.replace("offset", String.valueOf(l)));
                        }
                        case ReflectUtil.CLASS_STRING ->
                        {
                            writeBody.append("""
                                                     {
                                                       String obj = (String)UNSAFE.getReference(instance,offset);
                                                       if(obj==null)
                                                       {
                                                           byteArray.put(JfireSE.NULL);
                                                       }
                                                       else
                                                       {
                                                           byteArray.put(JfireSE.NOT_NULL);
                                                           byteArray.writeString(obj);
                                                       }
                                                     }
                                                     """.replace("offset", String.valueOf(l)));
                            readBody.append("""
                                                    {
                                                        if (byteArray.get() == JfireSE.NOT_NULL)
                                                        {
                                                            UNSAFE.putReference(instance,offset, byteArray.readString());
                                                        }
                                                    }
                                                    """.replace("offset", String.valueOf(l)));
                        }
                        default -> throw new RuntimeException("不支持的类型");
                    }
                }
                else if (fieldInfo instanceof VariableFieldInfo)
                {
                    String classInfoProperty      = "classInfo_$_" + fieldIndex;
                    String firstClassInfoProperty = "firstClassInfo_$_" + fieldIndex;
                    FieldModel classInfoModel = new FieldModel(classInfoProperty, ClassInfo.class, classModel);
                    FieldModel firstClassInfoModel = new FieldModel(firstClassInfoProperty, ClassInfo.class, classModel);
                    classModel.addField(classInfoModel, firstClassInfoModel);
                    initBody.append(classInfoProperty).append("=jfireSE.getOrCreateClassInfo(((FieldInfo)list_serializer_compile.get(").append(fieldIndex).append(")).getField().getType());\r\n");
                    initBody.append(" if( ((FieldInfo)list_serializer_compile.get(").append(fieldIndex).append(")).getField().getType().isInterface()) {");
                    initBody.append(firstClassInfoProperty).append("=null;\r\n}");
                    initBody.append("else{\r\n");
                    initBody.append(firstClassInfoProperty).append("=").append(classInfoProperty).append(";\r\n}");
                    String objName = "obj_" + fieldIndex;
                    writeBody.append("Object ").append(objName).append("= UNSAFE.getReference(instance,").append(l).append(");\r\n");
                    writeBody.append("if(").append(objName).append("==null){ byteArray.put(JfireSE.NULL);}\r\n");
                    writeBody.append("else{\r\n");
                    String objClassName = "objClass_$_" + fieldIndex;
                    writeBody.append("Class ").append(objClassName).append(" = ").append(objName).append(".getClass();\r\n");
                    writeBody.append("classInfo = classInfo.getClazz() == objClass ? classInfo : jfireSE.getOrCreateClassInfo(objClass);".replace("objClass", objClassName) //
                                                                                                                                         .replace("classInfo", classInfoProperty));
                    writeBody.append("if(").append(classInfoProperty).append("==").append(firstClassInfoProperty).append("){\r\n");
                    writeBody.append(classInfoProperty).append(".writeKnownClazz(byteArray,").append(objName).append(");\r\n");
                    writeBody.append("}\r\n");
                    writeBody.append("else{\r\n");
                    writeBody.append(classInfoProperty).append(".write(byteArray,").append(objName).append(");\r\n");
                    writeBody.append("}\r\n");
                    writeBody.append("}\r\n");
                    String flagName = "flag_$_" + fieldIndex;
                    readBody.append("byte ").append(flagName).append(" = byteArray.get();\r\n");
                    readBody.append("if(").append(flagName).append("==JfireSE.NULL){UNSAFE.putReference(instance,").append(l).append(",null);}\r\n");
                    readBody.append("else{\r\n");
                    readBody.append("switch(").append(flagName).append("){\r\n");
                    readBody.append("""
                                            case JfireSE.NAME_ID_CONTENT_TRACK,JfireSE.NAME_ID_CONTENT_UN_TRACK,JfireSE.ID_INSTANCE_ID,JfireSE.ID_CONTENT_TRACK,JfireSE.ID_CONTENT_UN_TRACK-> UNSAFE.putReference(instance,offset, jfireSE.readByUnderInstanceIdFlag(byteArray, flag));
                                            case JfireSE.INSTANCE_ID -> UNSAFE.putReference(instance,offset, firstClassInfo.getInstanceById(byteArray.readPositiveVarInt()));
                                            case JfireSE.CONTENT_TRACK -> UNSAFE.putReference(instance,offset, firstClassInfo.readWithTrack(byteArray));
                                            case JfireSE.CONTENT_UN_TRACK -> UNSAFE.putReference(instance,offset, firstClassInfo.readWithoutTrack(byteArray));
                                            """.replace("offset", String.valueOf(l)).replace("firstClassInfo", firstClassInfoProperty).replace("flag", flagName));
                    readBody.append("default -> throw new RuntimeException(\"flag:\" + ").append(flagName).append(");\r\n");
                    readBody.append("}\r\n");
                    readBody.append("}\r\n");
                }
                else if (fieldInfo instanceof FinalFieldInfo)
                {
                    String classInfoProperty = "classInfo_$_" + fieldIndex;
                    FieldModel classInfoModel = new FieldModel(classInfoProperty, ClassInfo.class, classModel);
                    classModel.addField(classInfoModel);
                    initBody.append(classInfoProperty + "=jfireSE.getOrCreateClassInfo(((FieldInfo)list_serializer_compile.get(" + fieldIndex + ")).getField().getType());\r\n");
                    writeBody.append("""
                                             {
                                                 Object obj = UNSAFE.getReference(instance,offset);
                                                        if (obj == null)
                                                        {
                                                            byteArray.put(JfireSE.NULL);
                                                        }
                                                        else
                                                        {
                                                            classInfo.writeKnownClazz(byteArray, obj);
                                                        }
                                             }
                                             """.replace("offset", String.valueOf(l)).replace("classInfo", classInfoProperty));
                    readBody.append("""
                                            {
                                                byte flag = byteArray.get();
                                                if (flag == JfireSE.NULL)
                                                {
                                                    UNSAFE.putReference(instance,offset,null);
                                                }
                                                else
                                                {
                                                        switch (flag)
                                                        {
                                                            case JfireSE.INSTANCE_ID -> UNSAFE.putReference(instance,offset,classInfo.getInstanceById(byteArray.readPositiveVarInt()));
                                                            case JfireSE.CONTENT_TRACK -> UNSAFE.putReference(instance,offset,classInfo.readWithTrack(byteArray));
                                                            case JfireSE.CONTENT_UN_TRACK -> UNSAFE.putReference(instance,offset,classInfo.readWithoutTrack(byteArray));
                                                            default -> throw new RuntimeException("flag:" + flag);
                                                        }
                                                }
                                            }
                                            """.replace("offset", String.valueOf(l)).replace("classInfo", classInfoProperty));
                }
                else if (fieldInfo instanceof UnknowObjectFieldInfo)
                {
                    writeBody.append("""
                                             {
                                                 Object obj = UNSAFE.getReference(instance,offset);
                                                        if (obj == null)
                                                        {
                                                            byteArray.put(JfireSE.NULL);
                                                        }
                                                        else
                                                        {
                                                             jfireSE.getOrCreateClassInfo(obj.getClass()).write(byteArray, obj);
                                                        }
                                             }
                                             """.replace("offset", String.valueOf(l)));
                    readBody.append("""
                                            {
                                                byte flag = byteArray.get();
                                                if (flag == JfireSE.NULL)
                                                {
                                                    UNSAFE.putReference(instance,offset,null);
                                                }
                                                else
                                                {
                                                    UNSAFE.putReference(instance, offset,jfireSE.readByUnderInstanceIdFlag(byteArray, flag));
                                                }
                                            }
                                            """.replace("offset", String.valueOf(l)));
                }
                fieldIndex++;
            }
            writeMethod.setBody(writeBody.toString());
            readBody.append("return instance;\r\n");
            readMethod.setBody("""
                                       Object instance;
                                       try{
                                          instance = UNSAFE.allocateInstance(clazz);
                                        }catch (InstantiationException e)
                                               {
                                                   throw new RuntimeException(e);
                                               }
                                       if(refTracking!=null)
                                       {
                                           refTracking.addTracking(instance);
                                       }
                                       """ + readBody);
            classModel.putMethodModel(writeMethod);
            classModel.putMethodModel(readMethod);
            MethodModel initModel = new MethodModel(Serializer.class.getDeclaredMethod("init"), classModel);
            initModel.setBody(initBody.toString());
            classModel.putMethodModel(initModel);
            CompileHelper compiler = new CompileHelper(Thread.currentThread().getContextClassLoader());
            Class<?>      compile  = compiler.compile(classModel);
            Serializer compiledObjectSerializer = (Serializer) compile.getDeclaredConstructor(Class.class, JfireSE.class, List.class).newInstance(clazz, jfireSE, parse);
            return compiledObjectSerializer;
        }
        catch (NoSuchMethodException | IOException | ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
    }
}
