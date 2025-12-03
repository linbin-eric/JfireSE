# JfireSE

JfireSE（Jfire Serialization Engine）是一个高性能的 Java 序列化框架，专为高吞吐量场景设计。

## 特性

- **高性能**：使用 Unsafe 直接内存操作 + 动态代码生成，性能优于传统反射
- **变长编码**：采用 VarInt/VarLong 编码压缩数据体积
- **引用追踪**：支持循环引用和对象重用检测
- **类型丰富**：支持基本类型、装箱类型、数组、集合、Map、JDK 特殊类型及自定义对象
- **零配置**：自动扫描对象字段，无需手动配置序列化规则
- **轻量级**：核心代码精简，仅依赖基础工具库

## 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>cc.jfire</groupId>
    <artifactId>JfireSE</artifactId>
    <version>1.0</version>
</dependency>
```

### 基本用法

```java
// 创建 JfireSE 实例
JfireSE jfireSE = new JfireSEImpl();

// 序列化
Person person = new Person("张三", 25);
byte[] bytes = jfireSE.serialize(person);

// 反序列化
Person restored = (Person) jfireSE.deSerialize(bytes);
```

### 预注册类（推荐）

预注册类可以减少序列化时的类名开销，提升性能：

```java
JfireSEConfig config = new JfireSEConfig();
config.registerClass(Person.class);
config.registerClass(Order.class);

JfireSE jfireSE = new JfireSEImpl(config);
```

## 支持的数据类型

### 基本类型
- `byte`, `short`, `int`, `long`, `float`, `double`, `boolean`, `char`

### 装箱类型
- `Byte`, `Short`, `Integer`, `Long`, `Float`, `Double`, `Boolean`, `Character`
- `String`

### 数组类型
- 基本类型数组：`int[]`, `long[]`, `byte[]`, `boolean[]`, `char[]`, `short[]`, `double[]`, `float[]`
- 装箱类型数组：`Integer[]`, `Long[]`, `String[]` 等
- 对象数组：任意类型的对象数组

### 集合类型
- **List**: `ArrayList`, `LinkedList`
- **Set**: `HashSet`, `LinkedHashSet`, `TreeSet`, `ConcurrentSkipListSet`
- **Map**: `HashMap`, `TreeMap`, `LinkedHashMap`, `ConcurrentHashMap`

### JDK 特殊类型
- `java.util.Date`
- `java.sql.Date`
- `java.util.Calendar`
- `java.lang.Class`
- `java.lang.reflect.Method`

### 自定义对象
- 自动序列化所有非静态、非瞬态字段
- 支持继承层次结构
- 支持接口字段（多态）

## 高级特性

### 循环引用

JfireSE 内置引用追踪机制，自动处理循环引用：

```java
public class User {
    private String name;
    private Home home;
}

public class Home {
    private String address;
    private User owner;  // 循环引用
}

User user = new User("张三");
Home home = new Home("北京");
user.setHome(home);
home.setOwner(user);  // 形成循环

byte[] bytes = jfireSE.serialize(user);
User restored = (User) jfireSE.deSerialize(bytes);
// restored.getHome().getOwner() == restored  ✓
```

### 代码生成优化

JfireSE 会在运行时动态生成优化的序列化器代码，避免反射开销：

```java
// 首次序列化时自动编译生成优化版本
// 后续序列化使用 Unsafe 直接内存操作，性能大幅提升
```

## 序列化格式

JfireSE 使用紧凑的二进制格式：

| 标志位 | 含义 |
|--------|------|
| 0 | NULL 值 |
| 1 | 非 NULL（装箱类型） |
| 2 | 首次序列化：类名 + classId + 内容（带追踪） |
| 3 | 首次序列化：类名 + classId + 内容（无追踪） |
| 4 | 循环引用：classId + 实例ID |
| 5-6 | 后续序列化：classId + 内容 |
| 7-9 | 已知类型：内容/实例ID |

## 性能对比

JfireSE 在性能基准测试中与 Fury、Kryo 等框架进行对比，表现优异。

运行基准测试：

```bash
mvn test -Dtest=BenchMark
```

## 项目结构

```
src/main/java/com/jfirer/se2/
├── JfireSE.java              # 主接口
├── JfireSEImpl.java          # 主实现
├── JfireSEConfig.java        # 配置类
├── ByteArray.java            # 高性能字节操作
├── classinfo/                # 类信息管理
│   ├── ClassInfo.java        # 类信息抽象基类
│   ├── DynamicClassInfo.java # 动态类信息
│   ├── RegisterClasInfo.java # 注册类信息
│   └── RefTracking.java      # 引用追踪接口
└── serializer/               # 序列化器
    ├── Serializer.java       # 序列化器接口
    ├── SerializerFactory.java# 序列化器工厂
    └── impl/                 # 具体实现
        ├── PrimitiveArraySerializer.java  # 基本类型数组
        ├── BoxedTypeSerializer.java       # 装箱类型
        ├── BoxedArraySerializer.java      # 装箱类型数组
        ├── ArraySerializer.java           # 对象数组
        ├── ObjectSerializer/              # 对象序列化器
        │   ├── ObjectSerializer.java      # 主序列化器
        │   ├── FieldInfo.java             # 字段信息基类
        │   ├── PrimitiveFieldInfo.java    # 基本类型字段
        │   ├── BoxedFieldInfo.java        # 装箱类型字段
        │   ├── FinalFieldInfo.java        # Final 字段
        │   └── VariableFieldInfo.java     # 可变类型字段
        └── jdk/                           # JDK 类型序列化器
            ├── ListSerializer.java
            ├── MapSerializer.java
            ├── SetSerializer.java
            ├── DateSerializer.java
            └── ...
```

## 构建要求

- JDK 17+
- Maven 3.6+

## 构建命令

```bash
# 编译
mvn clean compile

# 测试
mvn test

# 打包
mvn package

# 安装到本地仓库
mvn install
```

## 依赖说明

| 依赖 | 说明 |
|------|------|
| `cc.jfire:baseutil` | 反射工具、代码编译、值访问器 |
| `io.github.karlatemp:unsafeaccessor` | Unsafe 访问包装 |

## 注意事项

1. **构造函数**：反序列化时使用 `Unsafe.allocateInstance()` 创建对象，不会调用构造函数
2. **瞬态字段**：标记为 `transient` 的字段不会被序列化
3. **静态字段**：静态字段不会被序列化
4. **final 字段**：final 字段正常序列化，反序列化时通过 Unsafe 设置值

## 许可证

Apache License 2.0

## 贡献

欢迎提交 Issue 和 Pull Request！

## 联系方式

- GitHub: [https://github.com/linbin-eric/JfireSE](https://github.com/linbin-eric/JfireSE)
