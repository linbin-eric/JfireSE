package com.jfirer.se2.serializer.impl.ObjectSerializer;

import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.baseutil.smc.compiler.CompileHelper;
import com.jfirer.baseutil.smc.model.ClassModel;
import com.jfirer.baseutil.smc.model.ConstructorModel;
import com.jfirer.baseutil.smc.model.FieldModel;
import com.jfirer.baseutil.smc.model.MethodModel;
import com.jfirer.se2.ByteArray;
import com.jfirer.se2.JfireSEImpl;
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
    private              Class<?>    clazz;
    private              FieldInfo[] fieldInfos;
    static               int         COMPILE_COUNT = 1;
    private static final Unsafe      UNSAFE        = Unsafe.getUnsafe();

    public ObjectSerializer(Class<?> clazz, JfireSEImpl jfireSE)
    {
        fieldInfos = parse(clazz, jfireSE).toArray(FieldInfo[]::new);
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
    public void read(ByteArray stream, Object instance)
    {
        for (FieldInfo each : fieldInfos)
        {
            each.read(stream, instance);
        }
    }

    public static List<FieldInfo> parse(Class<?> clazz, JfireSEImpl jfireSE)
    {
        Class       type   = clazz;
        List<Field> fields = new ArrayList<>();
        while (type != Object.class)
        {
            fields.addAll(Arrays.stream(type.getDeclaredFields()).filter(Predicate.not(field -> Modifier.isStatic(field.getModifiers()))).toList());
            type = type.getSuperclass();
        }
        List<PrimitiveFieldInfo> primitiveFieldInfos = fields.stream().filter(field -> field.getType().isPrimitive())//
                                                             .sorted(Comparator.comparing(o -> o.getType().getName()))//
                                                             .map(PrimitiveFieldInfo::new)//
                                                             .toList();
        List<BoxedFieldInfo> boxedFieldInfos = fields.stream().filter(field -> ReflectUtil.isPrimitiveBox(field.getType()) || field.getType() == String.class)//
                                                     .sorted(Comparator.comparing(o -> o.getType().getName()))//
                                                     .map(BoxedFieldInfo::new)//
                                                     .toList();
        List<FinalFieldInfo> finalFieldInfos = fields.stream().filter(Predicate.not(field -> ReflectUtil.isPrimitiveBox(field.getType())))//
                                                     .filter(Predicate.not(field -> ReflectUtil.isPrimitive(field.getType())))//
                                                     .filter(field -> field.getType() != String.class)//
                                                     .filter(field -> field.getType() != void.class && field.getType() != Void.class)//
                                                     .filter(field -> Modifier.isFinal(field.getType().getModifiers()))//
                                                     .sorted(Comparator.comparing(o -> o.getType().getName()))//
                                                     .map(field -> new FinalFieldInfo(field, jfireSE))//
                                                     .toList();
        List<VariableFieldInfo> variableFieldInfos = fields.stream().filter(Predicate.not(field -> ReflectUtil.isPrimitiveBox(field.getType())))//
                                                           .filter(Predicate.not(field -> ReflectUtil.isPrimitive(field.getType())))//
                                                           .filter(field -> field.getType() != String.class)//
                                                           .filter(field -> field.getType() != void.class && field.getType() != Void.class)//
                                                           .filter(field -> !Modifier.isFinal(field.getType().getModifiers()))//
                                                           .sorted(Comparator.comparing(o -> o.getType().getName()))//
                                                           .map(field -> new VariableFieldInfo(field, jfireSE))//
                                                           .toList();
        List<FieldInfo> list = new LinkedList<>();
        list.addAll(primitiveFieldInfos);
        list.addAll(boxedFieldInfos);
        list.addAll(finalFieldInfos);
        list.addAll(variableFieldInfos);
        return list;
    }

    public static Serializer buildCompileVersion(Class<?> clazz, JfireSEImpl jfireSE)
    {
        List<FieldInfo> parse      = parse(clazz, jfireSE);
        ClassModel      classModel = new ClassModel("ObjectSerializer_compile_" + COMPILE_COUNT);
        COMPILE_COUNT++;
        classModel.addInterface(Serializer.class);
        classModel.addImport(Unsafe.class);
        classModel.addImport(List.class);
        classModel.addImport(FieldInfo.class);
        classModel.addImport(ByteArray.class);
        classModel.addField(new FieldModel("UNSAFE", Unsafe.class, "Unsafe.getUnsafe()", classModel));
        ConstructorModel constructorModel = new ConstructorModel(classModel);
        constructorModel.setParamTypes(Class.class, JfireSEImpl.class, List.class);
        constructorModel.setParamNames("clazz", "jfireSE", "list");
        StringBuilder constructorBody = new StringBuilder();
        try
        {
            MethodModel writeMethod = new MethodModel(Serializer.class.getDeclaredMethod("writeBytes", ByteArray.class, Object.class), classModel);
            writeMethod.setParamterNames("byteArray", "instance");
            StringBuilder writeBody  = new StringBuilder();
            MethodModel   readMethod = new MethodModel(Serializer.class.getDeclaredMethod("read", ByteArray.class, Object.class), classModel);
            readMethod.setParamterNames("byteArray", "instance");
            StringBuilder readBody   = new StringBuilder();
            int           fieldIndex = 0;
            for (FieldInfo fieldInfo : parse)
            {
                long l = UNSAFE.objectFieldOffset(fieldInfo.field);
                if (fieldInfo instanceof PrimitiveFieldInfo primitiveFieldInfo)
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
                else if (fieldInfo instanceof BoxedFieldInfo boxedFieldInfo)
                {
                    switch (fieldInfo.classId)
                    {
                        case ReflectUtil.PRIMITIVE_BYTE ->
                        {
                            writeBody.append("byteArray.writeByte((Byte) UNSAFE.getObject(instance, " + l + "));\r\n");
                            readBody.append("UNSAFE.putObject(instance," + l + ", byteArray.readByte());\r\n");
                        }
                        case ReflectUtil.PRIMITIVE_INT ->
                        {
                            writeBody.append("byteArray.writeVarInt((Integer) UNSAFE.getObject(instance, " + l + "));\r\n");
                            readBody.append("UNSAFE.putObject(instance," + l + ", byteArray.readVarInt());\r\n");
                        }
                        case ReflectUtil.PRIMITIVE_SHORT ->
                        {
                            writeBody.append("byteArray.writeVarInt((Short) UNSAFE.getObject(instance, " + l + "));\r\n");
                            readBody.append("UNSAFE.putObject(instance," + l + ", (short) byteArray.readVarInt());\r\n");
                        }
                        case ReflectUtil.PRIMITIVE_LONG ->
                        {
                            writeBody.append("byteArray.writeVarLong((Long) UNSAFE.getObject(instance, " + l + "));\r\n");
                            readBody.append("UNSAFE.putObject(instance," + l + ", byteArray.readVarLong());\r\n");
                        }
                        case ReflectUtil.PRIMITIVE_FLOAT ->
                        {
                            writeBody.append("byteArray.writeFloat((Float) UNSAFE.getObject(instance, " + l + "));\r\n");
                            readBody.append("UNSAFE.putObject(instance," + l + ", byteArray.readFloat());");
                        }
                        case ReflectUtil.PRIMITIVE_DOUBLE ->
                        {
                            writeBody.append("byteArray.writeDouble((Double) UNSAFE.getObject(instance, " + l + "));\r\n");
                            readBody.append("UNSAFE.putObject(instance," + l + ", byteArray.readDouble());\r\n");
                        }
                        case ReflectUtil.PRIMITIVE_BOOL ->
                        {
                            writeBody.append("byteArray.writeBoolean((Boolean) UNSAFE.getObject(instance, " + l + "));\r\n");
                            readBody.append("UNSAFE.putObject(instance," + l + ", byteArray.readBoolean());\r\n");
                        }
                        case ReflectUtil.PRIMITIVE_CHAR ->
                        {
                            writeBody.append("byteArray.writeChar((Character) UNSAFE.getObject(instance, " + l + "));\r\n");
                            readBody.append("UNSAFE.putObject(instance," + l + ", byteArray.readChar());\r\n");
                        }
                        case ReflectUtil.CLASS_STRING ->
                        {
                            writeBody.append("byteArray.writeString((String) UNSAFE.getObject(instance, " + l + "));\r\n");
                            readBody.append("UNSAFE.putObject(instance," + l + ", byteArray.readString());\r\n");
                        }
                        default -> throw new RuntimeException("不支持的类型");
                    }
                }
                else if (fieldInfo instanceof VariableFieldInfo || fieldInfo instanceof FinalFieldInfo)
                {
                    FieldModel fieldModel = new FieldModel("fieldInfo_$_" + fieldIndex, VariableFieldInfo.class, classModel);
                    classModel.addField(fieldModel);
                    constructorBody.append("fieldInfo_$_" + fieldIndex + "=(FieldInfo)list.get(" + fieldIndex + ");\r\n");
                    writeBody.append("fieldInfo_$_" + fieldIndex + ".write(byteArray,instance);\r\n");
                    readBody.append("fieldInfo_$_" + fieldIndex + ".read(byteArray,instance);\r\n");
                }
                fieldIndex++;
            }
            constructorModel.setBody(constructorBody.toString());
            classModel.addConstructor(constructorModel);
            writeMethod.setBody(writeBody.toString());
            readMethod.setBody(readBody.toString());
            classModel.putMethodModel(writeMethod);
            classModel.putMethodModel(readMethod);
            CompileHelper compiler = new CompileHelper(Thread.currentThread().getContextClassLoader());
            Class<?>           compile  = compiler.compile(classModel);
            Serializer compiledObjectSerializer = (Serializer) compile.getDeclaredConstructor(Class.class, JfireSEImpl.class, List.class).newInstance(clazz, jfireSE, parse);
            return compiledObjectSerializer;
        }
        catch (NoSuchMethodException | IOException | ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
    }
}
