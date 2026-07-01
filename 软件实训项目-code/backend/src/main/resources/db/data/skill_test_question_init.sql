-- ============================================================
-- 技能测试题库 - 建表 + 预置数据
-- 用于学习路径中的技能测试（startTest 接口）
-- MySQL 8.0+
-- ============================================================

USE career_platform;

-- ============================================================
-- 建表
-- ============================================================
CREATE TABLE IF NOT EXISTS `skill_test_question` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '题目ID',
    `skill_id` BIGINT NOT NULL COMMENT '技能ID（关联skill表）',
    `stage` VARCHAR(20) NOT NULL COMMENT '阶段：BASIC/FRAMEWORK/PROJECT/INTERVIEW',
    `question` VARCHAR(500) NOT NULL COMMENT '题目内容',
    `options` JSON NOT NULL COMMENT '选项(JSON数组，如["A. xxx","B. xxx","C. xxx","D. xxx"])',
    `correct_answer` VARCHAR(10) NOT NULL COMMENT '正确答案，如"A"',
    `difficulty` VARCHAR(10) NOT NULL DEFAULT '中等' COMMENT '难度：简单/中等/困难',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_skill_stage` (`skill_id`, `stage`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技能测试题库表';

-- ============================================================
-- 预置题目数据（使用技能名称子查询查找 skill_id，避免 ID 硬编码）
-- 每个技能 × 每阶段 5 题
-- ============================================================

-- ==================== Java (ID ~1) ====================
-- Java BASIC
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'Java' AND is_deleted = 0 LIMIT 1), 'BASIC', 'Java 中，以下哪个不是基本数据类型？', '["A. int","B. String","C. boolean","D. double"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = 'Java' AND is_deleted = 0 LIMIT 1), 'BASIC', '以下哪个关键字用于创建对象？', '["A. new","B. class","C. import","D. package"]', 'A', '简单'),
((SELECT id FROM skill WHERE name = 'Java' AND is_deleted = 0 LIMIT 1), 'BASIC', 'Java 中，int 类型的默认值是什么？', '["A. 0","B. null","C. 1","D. -1"]', 'A', '简单'),
((SELECT id FROM skill WHERE name = 'Java' AND is_deleted = 0 LIMIT 1), 'BASIC', '以下代码的输出是什么？System.out.println(1 + 2 + "3")', '["A. 6","B. 33","C. 123","D. 编译错误"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Java' AND is_deleted = 0 LIMIT 1), 'BASIC', 'Java 中 final 关键字修改的变量有什么特点？', '["A. 可以被子类重写","B. 值不可改变","C. 可以多次赋值","D. 自动初始化"]', 'B', '简单');

-- Java FRAMEWORK
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'Java' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'Java 中 ArrayList 和 LinkedList 的主要区别是什么？', '["A. ArrayList 是线程安全的","B. LinkedList 使用双向链表实现","C. ArrayList 不能存 null","D. LinkedList 查询比 ArrayList 快"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Java' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'JDK 动态代理和 CGLIB 代理的区别是什么？', '["A. JDK 代理基于类，CGLIB 基于接口","B. JDK 代理基于接口，CGLIB 基于类","C. 两者没有区别","D. JDK 代理不需要接口"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Java' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'HashMap 在什么情况下会转为红黑树？', '["A. 链表长度 > 6 且数组长度 >= 64","B. 链表长度 > 8 且数组长度 >= 64","C. 元素数量 > 16","D. 永远不会转换"]', 'B', '困难'),
((SELECT id FROM skill WHERE name = 'Java' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', '以下关于 Java 泛型的说法正确的是？', '["A. 泛型在运行时保留类型信息","B. Java 泛型通过类型擦除实现","C. 泛型不支持通配符","D. 泛型类不能继承"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Java' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'ConcurrentHashMap 如何保证线程安全？', '["A. 使用 synchronized 锁整个表","B. 分段锁 + CAS 操作","C. 使用 volatile 关键字","D. 它不支持并发访问"]', 'B', '中等');

-- Java PROJECT
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'Java' AND is_deleted = 0 LIMIT 1), 'PROJECT', '在高并发场景下，应如何选择线程池？', '["A. 使用 Executors.newCachedThreadPool()","B. 使用 ThreadPoolExecutor 自定义参数","C. 使用 Executors.newSingleThreadExecutor()","D. 不使用线程池"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Java' AND is_deleted = 0 LIMIT 1), 'PROJECT', 'Java 中如何处理大文件读取？', '["A. 一次性读入内存","B. 使用 BufferedReader 按行读取","C. 使用 Scanner 读取全部","D. 无法读取大文件"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Java' AND is_deleted = 0 LIMIT 1), 'PROJECT', '使用 synchronized 锁对象的哪个部分可以最大程度减少锁竞争？', '["A. 锁整个方法","B. 锁 this","C. 只锁需要同步的代码块","D. 不做同步"]', 'C', '中等'),
((SELECT id FROM skill WHERE name = 'Java' AND is_deleted = 0 LIMIT 1), 'PROJECT', 'Java 应用中如何防止内存泄漏？', '["A. 不创建对象","B. 及时释放不再使用的对象引用，使用 try-with-resources","C. 增加堆内存","D. 多创建临时对象"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Java' AND is_deleted = 0 LIMIT 1), 'PROJECT', '以下哪种方式能正确优雅地关闭线程池？', '["A. shutdown() 后调用 awaitTermination()","B. 只调用 shutdown()","C. 直接 kill 线程","D. 调用 shutdownNow() 不等待"]', 'A', '困难');

-- Java INTERVIEW
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'Java' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'JVM 内存模型中，哪个区域会发生 OutOfMemoryError？', '["A. 程序计数器","B. 堆(Heap)","C. 本地方法栈","D. 只有栈"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Java' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'String、StringBuilder、StringBuffer 的区别是什么？', '["A. 都是不可变的","B. String 不可变，StringBuffer 线程安全，StringBuilder 非线程安全","C. StringBuilder 线程安全","D. StringBuffer 比 StringBuilder 快"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Java' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', '什么是双亲委派模型？', '["A. 一个类由多个类加载器同时加载","B. 类加载器优先将请求委派给父加载器","C. 不用加载器直接执行","D. 只有 Bootstrap ClassLoader 存在"]', 'B', '困难'),
((SELECT id FROM skill WHERE name = 'Java' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'Java 中 == 和 equals 的区别是？', '["A. 没有区别","B. == 比较引用，equals 比较值","C. == 比较值，equals 比较引用","D. equals 只能在 String 中使用"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = 'Java' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', '什么是 Java 的反射机制？', '["A. 编译时确定类型","B. 运行时动态获取类信息和调用方法","C. 一种设计模式","D. 自动生成代码"]', 'B', '中等');

-- ==================== Spring Boot (ID ~7) ====================
-- Spring Boot BASIC
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'Spring Boot' AND is_deleted = 0 LIMIT 1), 'BASIC', 'Spring Boot 的核心注解是什么？', '["A. @Component","B. @SpringBootApplication","C. @Service","D. @Repository"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = 'Spring Boot' AND is_deleted = 0 LIMIT 1), 'BASIC', 'Spring Boot 内嵌了哪个 Web 服务器？', '["A. Apache","B. Tomcat","C. Nginx","D. IIS"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = 'Spring Boot' AND is_deleted = 0 LIMIT 1), 'BASIC', '在 Spring Boot 中，配置文件 application.yml 和 bootstrap.yml 的区别是？', '["A. 没有区别","B. bootstrap.yml 加载更早，用于配置中心","C. application.yml 不加载","D. bootstrap.yml 不被支持"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Spring Boot' AND is_deleted = 0 LIMIT 1), 'BASIC', '@RestController 注解组合了哪两个注解？', '["A. @Controller + @ResponseBody","B. @Service + @Controller","C. @Component + @ResponseBody","D. @Controller + @RequestMapping"]', 'A', '简单'),
((SELECT id FROM skill WHERE name = 'Spring Boot' AND is_deleted = 0 LIMIT 1), 'BASIC', 'Spring Boot 的 starter 主要作用是什么？', '["A. 应用启动入口","B. 自动配置和依赖管理","C. 数据库连接","D. 日志管理"]', 'B', '简单');

-- Spring Boot FRAMEWORK
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'Spring Boot' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'Spring Boot 中拦截器和过滤器的区别是什么？', '["A. 没有区别","B. 过滤器在 Servlet 容器层，拦截器在 Spring 容器层","C. 拦截器在 Servlet 容器层","D. 过滤器不能拦截所有请求"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Spring Boot' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', '@Transactional 注解默认的传播行为是什么？', '["A. NEVER","B. REQUIRED","C. SUPPORTS","D. MANDATORY"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Spring Boot' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'Spring Boot 如何自定义 starter？', '["A. 只写一个类","B. 创建 autoconfigure 模块，配置 spring.factories","C. 修改源码","D. 不能自定义"]', 'B', '困难'),
((SELECT id FROM skill WHERE name = 'Spring Boot' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', '在 Spring Boot 中处理全局异常的最佳方式是什么？', '["A. 每个方法 try-catch","B. 使用 @ControllerAdvice + @ExceptionHandler","C. 抛出所有异常","D. 使用 System.out 打印"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Spring Boot' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'Spring Boot 中读取配置文件属性的最佳实践是？', '["A. 直接使用 System.getProperty","B. 使用 @ConfigurationProperties 绑定到类","C. 读取文件流","D. 硬编码"]', 'B', '中等');

-- Spring Boot PROJECT
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'Spring Boot' AND is_deleted = 0 LIMIT 1), 'PROJECT', 'Spring Boot 微服务之间如何实现服务调用？', '["A. 直接调用数据库","B. 使用 OpenFeign 或 RestTemplate","C. 只能使用 HTTP","D. 不能跨服务调用"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Spring Boot' AND is_deleted = 0 LIMIT 1), 'PROJECT', '如何处理 Spring Boot 中的循环依赖？', '["A. 无法处理","B. 使用 @Lazy 注解或在构造器中使用 @Autowired 的 setter 方法","C. 重启应用","D. 删除其中一个类"]', 'B', '困难'),
((SELECT id FROM skill WHERE name = 'Spring Boot' AND is_deleted = 0 LIMIT 1), 'PROJECT', 'Spring Boot 项目中如何实现定时任务？', '["A. 使用 while(true) 循环","B. 使用 @Scheduled 注解","C. 手动触发","D. 不能实现"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Spring Boot' AND is_deleted = 0 LIMIT 1), 'PROJECT', 'Spring Boot Actuator 的作用是什么？', '["A. 数据库管理","B. 监控和管理应用（健康检查、指标等）","C. 日志框架","D. 安全框架"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Spring Boot' AND is_deleted = 0 LIMIT 1), 'PROJECT', 'Spring Boot 多数据源如何配置？', '["A. 不支持多数据源","B. 使用 @Primary 和 @Qualifier 注解配置不同 DataSource","C. 只用一个数据源","D. 使用多个 application.yml"]', 'B', '困难');

-- Spring Boot INTERVIEW
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'Spring Boot' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'Spring Boot 自动配置原理是什么？', '["A. 写很多配置文件","B. @EnableAutoConfiguration 通过 spring.factories 加载自动配置类","C. 手动配置每个 Bean","D. 不需要配置"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Spring Boot' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'Spring 的 Bean 作用域有哪些？', '["A. 只有 singleton","B. singleton、prototype、request、session、application","C. 只有 prototype","D. 无限多种"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Spring Boot' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'Spring 中 @Autowired 和 @Resource 的区别？', '["A. 没有区别","B. @Autowired 按类型注入，@Resource 按名称注入","C. @Resource 按类型注入","D. 两者都不能注入 Bean"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Spring Boot' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', '什么是 AOP？Spring Boot 如何实现？', '["A. 一个编程语言","B. 面向切面编程，通过动态代理实现横切关注点","C. 数据库操作","D. 前端框架"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Spring Boot' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'Spring Boot 如何实现异步任务？', '["A. 不支持异步","B. 使用 @Async 注解并在配置类启用 @EnableAsync","C. 只能使用多线程","D. 必须使用消息队列"]', 'B', '中等');

-- ==================== MySQL (ID ~15) ====================
-- MySQL BASIC
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'MySQL' AND is_deleted = 0 LIMIT 1), 'BASIC', 'MySQL 中，哪个语句用于插入数据？', '["A. ADD","B. INSERT","C. UPDATE","D. CREATE"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = 'MySQL' AND is_deleted = 0 LIMIT 1), 'BASIC', '以下哪个不是 MySQL 的数据类型？', '["A. INT","B. VARCHAR","C. STRING","D. DATETIME"]', 'C', '简单'),
((SELECT id FROM skill WHERE name = 'MySQL' AND is_deleted = 0 LIMIT 1), 'BASIC', 'MySQL 中 PRIMARY KEY 的含义是？', '["A. 允许为空","B. 唯一标识每一行，不允许为空","C. 可以重复","D. 是一种索引"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = 'MySQL' AND is_deleted = 0 LIMIT 1), 'BASIC', 'MySQL 中，COUNT(*) 和 COUNT(1) 的区别是？', '["A. COUNT(1) 更快","B. COUNT(1) 计算第一列的值","C. 在大多数情况下没有性能区别","D. COUNT(*) 会忽略 NULL"]', 'C', '中等'),
((SELECT id FROM skill WHERE name = 'MySQL' AND is_deleted = 0 LIMIT 1), 'BASIC', '以下哪个 SQL 子句用于分组聚合？', '["A. ORDER BY","B. GROUP BY","C. WHERE","D. HAVING"]', 'B', '简单');

-- MySQL FRAMEWORK
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'MySQL' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'MySQL 索引的底层数据结构是什么？', '["A. 数组","B. B+ Tree","C. 链表","D. 哈希表"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'MySQL' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'InnoDB 和 MyISAM 的主要区别是什么？', '["A. 没有区别","B. InnoDB 支持事务和行级锁，MyISAM 不支持","C. MyISAM 支持事务","D. InnoDB 不支持外键"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'MySQL' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', '什么是回表查询？如何避免？', '["A. 查询数据后返回客户端","B. 通过二级索引查询后需要回聚簇索引查完整数据，使用覆盖索引避免","C. 数据自动返回","D. 无法避免"]', 'B', '困难'),
((SELECT id FROM skill WHERE name = 'MySQL' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'MySQL 事务的隔离级别有哪些？', '["A. 只有一种","B. READ UNCOMMITTED、READ COMMITTED、REPEATABLE READ、SERIALIZABLE","C. 无限多","D. 不需要设置"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'MySQL' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', '什么是 SQL 注入？如何防止？', '["A. 数据库故障","B. 恶意 SQL 拼接攻击，使用 PreparedStatement 参数化查询防止","C. 正常操作","D. 网络攻击"]', 'B', '中等');

-- MySQL PROJECT
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'MySQL' AND is_deleted = 0 LIMIT 1), 'PROJECT', '如何优化慢查询？', '["A. 忽略不计","B. 使用 EXPLAIN 分析执行计划，添加合适的索引","C. 重启数据库","D. 只优化代码"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'MySQL' AND is_deleted = 0 LIMIT 1), 'PROJECT', 'MySQL 主从复制的原理是什么？', '["A. 手动复制文件","B. 基于 binlog，从库拉取主库的二进制日志并执行","C. 自动同步","D. 不需要复制"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'MySQL' AND is_deleted = 0 LIMIT 1), 'PROJECT', 'MySQL 分库分表中间件有哪些？', '["A. 没有这种东西","B. ShardingSphere、MyCat","C. 只有 MySQL Router","D. 不需要分库分表"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'MySQL' AND is_deleted = 0 LIMIT 1), 'PROJECT', '如何处理大表中的数据迁移？', '["A. 一次性全量迁移","B. 分批迁移 + binlog 增量同步","C. 删除重建","D. 不需要迁移"]', 'B', '困难'),
((SELECT id FROM skill WHERE name = 'MySQL' AND is_deleted = 0 LIMIT 1), 'PROJECT', 'MySQL 中如何实现乐观锁？', '["A. 使用 SELECT FOR UPDATE","B. 使用 version 字段实现 CAS 更新","C. 锁定整张表","D. 不需要锁"]', 'B', '中等');

-- MySQL INTERVIEW
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'MySQL' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'MySQL 中 char 和 varchar 的区别？', '["A. 没有区别","B. char 定长，varchar 变长","C. varchar 定长","D. char 变长"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = 'MySQL' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', '什么是最左前缀原则？', '["A. 查询最左侧数据","B. 联合索引从最左侧开始匹配才能生效","C. 只有第一个字段重要","D. 没有这个原则"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'MySQL' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', '聚簇索引和非聚簇索引的区别？', '["A. 没有区别","B. 聚簇索引的数据和索引存在一起，非聚簇索引叶子节点存主键指针","C. 只有聚簇索引","D. 非聚簇索引更快"]', 'B', '困难'),
((SELECT id FROM skill WHERE name = 'MySQL' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'MySQL 死锁如何排查和解决？', '["A. 忽略","B. 使用 SHOW ENGINE INNODB STATUS 查看，优化事务顺序","C. 重启数据库","D. 删除数据"]', 'B', '困难'),
((SELECT id FROM skill WHERE name = 'MySQL' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', '什么是 MVCC？MySQL 如何实现？', '["A. 一种索引","B. 多版本并发控制，通过 undo log 和 ReadView 实现","C. 数据库名称","D. 查询方式"]', 'B', '困难');

-- ==================== Redis (ID ~16) ====================
-- Redis BASIC
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'Redis' AND is_deleted = 0 LIMIT 1), 'BASIC', 'Redis 的默认端口是多少？', '["A. 3306","B. 6379","C. 8080","D. 27017"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = 'Redis' AND is_deleted = 0 LIMIT 1), 'BASIC', 'Redis 的数据存储在哪个位置？', '["A. 磁盘文件","B. 内存","C. 云端","D. 数据库"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = 'Redis' AND is_deleted = 0 LIMIT 1), 'BASIC', 'Redis 中 String 类型的 SET 命令格式是？', '["A. SET value key","B. SET key value","C. PUT key value","D. INSERT key value"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = 'Redis' AND is_deleted = 0 LIMIT 1), 'BASIC', 'Redis 中，以下哪个命令用于设置过期时间？', '["A. TIMEOUT","B. EXPIRE","C. TTL","D. SETEX"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = 'Redis' AND is_deleted = 0 LIMIT 1), 'BASIC', 'Redis 有哪些基本数据类型？', '["A. 只有 String","B. String、Hash、List、Set、ZSet","C. 只有 List","D. String 和 int"]', 'B', '简单');

-- Redis FRAMEWORK
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'Redis' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'Redis 的持久化方式有哪些？', '["A. 没有持久化","B. RDB 快照和 AOF 日志","C. 只在磁盘存储","D. 依赖 MySQL"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Redis' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'Redis 缓存穿透是什么？如何解决？', '["A. 缓存过期","B. 查询不存在的数据穿透缓存直达数据库，使用布隆过滤器解决","C. 缓存满了","D. 不需要解决"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Redis' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'Redis 缓存雪崩是什么？', '["A. 缓存正常运行","B. 大量缓存同时过期导致数据库压力骤增","C. Redis 宕机","D. 网络故障"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Redis' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'Redis 实现分布式锁用什么命令？', '["A. LOCK","B. SETNX + EXPIRE","C. GET","D. SET"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Redis' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'Redis 集群模式有哪些？', '["A. 只有单机","B. 主从、哨兵、Cluster","C. 只有哨兵","D. 只有 Cluster"]', 'B', '中等');

-- Redis PROJECT
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'Redis' AND is_deleted = 0 LIMIT 1), 'PROJECT', '在高并发场景下如何使用 Redis 做排行榜？', '["A. 使用 List","B. 使用 ZSet（Sorted Set）的 ZADD 和 ZRANGE","C. 使用 String","D. 无法实现"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Redis' AND is_deleted = 0 LIMIT 1), 'PROJECT', 'Redis 大 Key 问题如何处理？', '["A. 忽略","B. 拆分大 Key，使用 hash 分片","C. 删除 Redis","D. 增加内存"]', 'B', '困难'),
((SELECT id FROM skill WHERE name = 'Redis' AND is_deleted = 0 LIMIT 1), 'PROJECT', 'Redis 做消息队列有哪些方式？', '["A. 无法实现","B. List（LPUSH/RPOP）或 Stream 类型","C. 只能用 Kafka","D. 只能用 RabbitMQ"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Redis' AND is_deleted = 0 LIMIT 1), 'PROJECT', 'Redis Pipeline 的作用是什么？', '["A. 加密传输","B. 批量执行命令减少 RTT 网络往返","C. 持久化","D. 集群管理"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Redis' AND is_deleted = 0 LIMIT 1), 'PROJECT', '如何保证 Redis 和数据库的双写一致性？', '["A. 无法保证","B. 先更新数据库再删除缓存，或使用 Canal 监听 binlog","C. 只更新 Redis","D. 只更新数据库"]', 'B', '困难');

-- Redis INTERVIEW
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'Redis' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'Redis 为什么快？', '["A. 因为是 NoSQL","B. 基于内存、单线程避免锁竞争、IO 多路复用","C. 因为用 C 语言","D. 不快"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Redis' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'Redis 的过期策略是什么？', '["A. 只有惰性删除","B. 惰性删除 + 定期删除","C. 只有定期删除","D. 不删除"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Redis' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'Redis 内存淘汰策略有哪些？', '["A. 只有一种","B. noeviction、allkeys-lru、volatile-lru、allkeys-random 等","C. 无法配置","D. 只有 LRU"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Redis' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'Redis 哨兵模式是什么？', '["A. 一种数据结构","B. 高可用方案，自动故障转移和主从切换","C. 加密方式","D. 不存在"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Redis' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'Redis 6.0 之后的多线程做了什么？', '["A. 完全多线程","B. 网络 IO 多线程处理，命令执行仍是单线程","C. 没有变化","D. 只支持单线程"]', 'B', '中等');

-- ==================== Vue.js (ID ~10) ====================
-- Vue BASIC
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'Vue 3' AND is_deleted = 0 LIMIT 1), 'BASIC', 'Vue 3 中创建响应式数据使用什么 API？', '["A. data()","B. ref() 或 reactive()","C. watch()","D. computed()"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = 'Vue 3' AND is_deleted = 0 LIMIT 1), 'BASIC', 'Vue 模板中 {{ }} 语法的作用是什么？', '["A. 注释","B. 文本插值，绑定数据","C. 计算","D. 样式绑定"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = 'Vue 3' AND is_deleted = 0 LIMIT 1), 'BASIC', 'Vue 3 中 v-model 指令的作用是什么？', '["A. 单向数据流","B. 双向数据绑定","C. 条件渲染","D. 列表渲染"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = 'Vue 3' AND is_deleted = 0 LIMIT 1), 'BASIC', 'Vue 3 的组合式 API setup() 在哪个生命周期执行？', '["A. mounted","B. beforeCreate 和 created 之间","C. updated","D. destroyed"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Vue 3' AND is_deleted = 0 LIMIT 1), 'BASIC', 'Vue 3 中用什么代替 Vue 2 的 this.$emit？', '["A. props","B. defineEmits()","C. defineProps()","D. provide()"]', 'B', '中等');

-- Vue FRAMEWORK
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'Vue 3' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'Vue 3 中 watch 和 watchEffect 的区别是？', '["A. 没有区别","B. watch 需要指定监听源，watchEffect 自动收集依赖","C. watchEffect 不需要副作用","D. watch 不支持深度监听"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Vue 3' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'Vue Router 如何实现路由懒加载？', '["A. 使用 require","B. component: () => import(路径)","C. 直接 import","D. 不需要"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Vue 3' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'Pinia 相比 Vuex 的优势是什么？', '["A. 没有优势","B. 更简洁的 API、完整的 TypeScript 支持、去除了 mutations","C. Pinia 功能更少","D. Vuex 更好"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Vue 3' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'Vue 3 组件通信方式有哪些？', '["A. 只有 props","B. props/emits、provide/inject、Pinia、事件总线","C. 只有 emit","D. 不能通信"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Vue 3' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'Vue 3 中 ref 和 reactive 的区别？', '["A. 没有区别","B. ref 包装基本类型需要 .value，reactive 直接访问属性","C. reactive 包装基本类型","D. ref 不能用于对象"]', 'B', '中等');

-- Vue PROJECT
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'Vue 3' AND is_deleted = 0 LIMIT 1), 'PROJECT', 'Vue 3 项目中如何优化首屏加载？', '["A. 不优化","B. 路由懒加载、组件异步加载、gzip 压缩、CDN","C. 增加代码量","D. 只用 CSS"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Vue 3' AND is_deleted = 0 LIMIT 1), 'PROJECT', 'Vue 3 中如何封装可复用组件？', '["A. 不封装","B. 使用 props 定义接口，emit 定义事件，slot 定义插槽","C. 只写一次","D. 复制粘贴"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Vue 3' AND is_deleted = 0 LIMIT 1), 'PROJECT', 'Vue 3 项目中如何处理跨域问题？', '["A. 无法处理","B. vite.config.ts 中配置 proxy 代理","C. 后端处理","D. 忽略"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Vue 3' AND is_deleted = 0 LIMIT 1), 'PROJECT', 'Vue 3 + TypeScript 项目中如何定义组件的 props 类型？', '["A. 不定义","B. 使用 defineProps<{...}>() 泛型","C. 使用 any","D. 用 JavaScript"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Vue 3' AND is_deleted = 0 LIMIT 1), 'PROJECT', 'Vue 3 权限控制如何实现？', '["A. 不做权限","B. 路由守卫 + 按钮级指令 v-permission","C. 只有登录","D. 前端不用做"]', 'B', '困难');

-- Vue INTERVIEW
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'Vue 3' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'Vue 3 的响应式原理是什么？', '["A. Object.defineProperty","B. Proxy 代理对象","C. 没有响应式","D. 手动更新"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Vue 3' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'Vue 的虚拟 DOM 是什么？', '["A. 实际的 DOM","B. 用 JS 对象模拟 DOM 结构，通过 diff 算法高效更新","C. 数据库","D. 服务器"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Vue 3' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'Vue 3 的 nextTick 原理是什么？', '["A. 立即执行","B. 在下次 DOM 更新循环后执行回调，利用微任务/Promise","C. 延迟 1 秒","D. 同 setTimeout"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Vue 3' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'Vue 3 的 Composition API 有什么优势？', '["A. 没有优势","B. 更好的逻辑复用、更好的类型推断、更灵活的代码组织","C. 不兼容 Vue 2","D. 只能用于小项目"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Vue 3' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'Vue 3 中如何实现 keep-alive？', '["A. 无法实现","B. 用 <KeepAlive> 包裹动态组件，缓存组件状态","C. 手动保存状态","D. 使用 localStorage"]', 'B', '中等');

-- ==================== Git (ID ~20) ====================
-- Git BASIC
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'Git' AND is_deleted = 0 LIMIT 1), 'BASIC', 'Git 中，初始化仓库用什么命令？', '["A. git start","B. git init","C. git create","D. git new"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = 'Git' AND is_deleted = 0 LIMIT 1), 'BASIC', 'Git 中查看暂存区状态的命令是？', '["A. git log","B. git status","C. git diff","D. git show"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = 'Git' AND is_deleted = 0 LIMIT 1), 'BASIC', 'Git 中将文件添加到暂存区的命令是？', '["A. git commit","B. git add","C. git push","D. git pull"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = 'Git' AND is_deleted = 0 LIMIT 1), 'BASIC', 'Git 中创建新分支并切换的命令是？', '["A. git branch name","B. git checkout -b name","C. git switch name","D. git new-branch name"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = 'Git' AND is_deleted = 0 LIMIT 1), 'BASIC', 'Git 将本地提交推送到远程仓库的命令是？', '["A. git send","B. git push","C. git upload","D. git commit -m"]', 'B', '简单');

-- Git FRAMEWORK
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'Git' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'Git merge 和 rebase 的区别是什么？', '["A. 没有区别","B. merge 保留分支历史，rebase 使提交历史线性","C. rebase 更复杂","D. merge 不能合并"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Git' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', '如何回退 Git 的某次提交？', '["A. 只能删除仓库","B. git revert（安全保留历史）或 git reset（强制回退）","C. 无法回退","D. 只能 git push --force"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Git' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'Git stash 的作用是什么？', '["A. 删除代码","B. 暂存未提交的修改，方便切换分支","C. 提交代码","D. 合并分支"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Git' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'Git 中 .gitignore 文件的作用是什么？', '["A. 记录文件","B. 指定不需要版本控制的文件","C. 删除文件","D. 代码格式化"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Git' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', '解决 Git merge 冲突的步骤是？', '["A. 放弃合并","B. git status 查看冲突文件 → 手动编辑 → git add → git commit","C. 自动解决","D. 删除冲突文件"]', 'B', '中等');

-- Git PROJECT
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'Git' AND is_deleted = 0 LIMIT 1), 'PROJECT', '团队协作中 Git Flow 工作流包含哪些分支？', '["A. 只有 main","B. main、develop、feature、release、hotfix","C. 只有 feature","D. 不需要分支"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Git' AND is_deleted = 0 LIMIT 1), 'PROJECT', 'Git cherry-pick 命令的作用是？', '["A. 选择分支","B. 将某个提交应用到当前分支","C. 删除提交","D. 创建分支"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Git' AND is_deleted = 0 LIMIT 1), 'PROJECT', '如何修改已经 push 的 commit 信息？', '["A. 无法修改","B. git commit --amend 修改后 git push --force（谨慎使用）","C. 直接编辑","D. 删除仓库"]', 'B', '困难'),
((SELECT id FROM skill WHERE name = 'Git' AND is_deleted = 0 LIMIT 1), 'PROJECT', 'Code Review 时如何推荐使用 Git？', '["A. 不用 Git","B. 通过 Pull Request/Merge Request 提交审查","C. 直接 push 到 main","D. 邮件发送代码"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Git' AND is_deleted = 0 LIMIT 1), 'PROJECT', 'Git submodule 和 subtree 的区别？', '["A. 没有区别","B. submodule 引用外部仓库，subtree 将外部仓库代码合并到主仓库","C. 都是分支","D. 不能管理外部依赖"]', 'B', '困难');

-- Git INTERVIEW
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'Git' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'Git 内部对象模型包含哪些类型？', '["A. 只有 commit","B. blob、tree、commit、tag","C. 只有文件","D. 只有目录"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Git' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'git reset --soft、--mixed、--hard 的区别？', '["A. 没有区别","B. --soft 保留暂存区和工作区，--mixed 保留工作区，--hard 全部清除","C. --hard 保留工作区","D. 不能区分"]', 'B', '困难'),
((SELECT id FROM skill WHERE name = 'Git' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'Git hooks 有什么作用？', '["A. 装饰代码","B. 在 Git 操作的特定时机触发自定义脚本（如 pre-commit 代码检查）","C. 没有作用","D. 只是日志"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Git' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', '如何将一个分支的多个 commit 合并为一个？', '["A. 手动删除","B. git rebase -i（交互式变基）squash 操作","C. 无法合并","D. git merge"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Git' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'Git 中 HEAD、工作区、暂存区的概念？', '["A. 没有概念","B. HEAD 指向当前分支最新提交，工作区是当前文件，暂存区是 add 后的中间状态","C. 只有工作区","D. 只有 HEAD"]', 'B', '中等');

-- ==================== Docker (ID ~21) ====================
-- Docker BASIC
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'Docker' AND is_deleted = 0 LIMIT 1), 'BASIC', 'Docker 的核心概念不包括？', '["A. 镜像(Image)","B. 容器(Container)","C. 虚拟机(Virtual Machine)","D. 仓库(Registry)"]', 'C', '简单'),
((SELECT id FROM skill WHERE name = 'Docker' AND is_deleted = 0 LIMIT 1), 'BASIC', 'Docker 中运行容器的命令是？', '["A. docker start","B. docker run","C. docker execute","D. docker begin"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = 'Docker' AND is_deleted = 0 LIMIT 1), 'BASIC', 'Docker 查看运行中容器列表的命令是？', '["A. docker list","B. docker ps","C. docker show","D. docker containers"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = 'Docker' AND is_deleted = 0 LIMIT 1), 'BASIC', 'Dockerfile 中 FROM 指令的作用是？', '["A. 指定构建目录","B. 指定基础镜像","C. 指定运行命令","D. 复制文件"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = 'Docker' AND is_deleted = 0 LIMIT 1), 'BASIC', 'Docker 构建镜像的命令是？', '["A. docker create","B. docker build -t name:tag .","C. docker make","D. docker compile"]', 'B', '简单');

-- Docker FRAMEWORK
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'Docker' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'Docker 容器和虚拟机的区别是什么？', '["A. 没有区别","B. 容器共享宿主机内核，更轻量；虚拟机需要完整 Guest OS","C. 虚拟机更轻量","D. 容器不能运行应用"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Docker' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'Docker 数据卷(Volume)的作用是什么？', '["A. 加速构建","B. 持久化数据，容器间数据共享","C. 网络配置","D. 没有作用"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Docker' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'Docker Compose 的作用是什么？', '["A. 单个容器管理","B. 定义和运行多容器 Docker 应用","C. 代码编译","D. 数据库管理"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Docker' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'Docker 镜像分层的好处是什么？', '["A. 没有好处","B. 共享基础层，节省存储和传输成本","C. 只有坏处","D. 不支持"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Docker' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', '如何进入正在运行的 Docker 容器？', '["A. 无法进入","B. docker exec -it container_id /bin/bash","C. docker enter","D. docker open"]', 'B', '中等');

-- Docker PROJECT
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'Docker' AND is_deleted = 0 LIMIT 1), 'PROJECT', '如何优化 Docker 镜像体积？', '["A. 无法优化","B. 多阶段构建、使用 alpine 基础镜像、减少层数、清理缓存","C. 增加文件","D. 使用大镜像"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Docker' AND is_deleted = 0 LIMIT 1), 'PROJECT', 'Docker Compose 中 depends_on 的作用？', '["A. 网络配置","B. 定义服务启动顺序依赖","C. 数据卷配置","D. 没有作用"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Docker' AND is_deleted = 0 LIMIT 1), 'PROJECT', 'Spring Boot 应用 Docker 化的一般步骤？', '["A. 不需要","B. 写 Dockerfile → build 镜像 → push 到仓库 → 部署运行","C. 直接运行 jar","D. 只用 Docker Compose"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Docker' AND is_deleted = 0 LIMIT 1), 'PROJECT', 'Docker 网络模式有哪些？', '["A. 只有一种","B. bridge、host、none、container、overlay","C. 两种","D. 不支持网络"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Docker' AND is_deleted = 0 LIMIT 1), 'PROJECT', '如何实现 Docker 容器间通信？', '["A. 无法通信","B. 使用自定义 bridge 网络或 Docker Compose 默认网络","C. 使用外网","D. 只能单机"]', 'B', '中等');

-- Docker INTERVIEW
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'Docker' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'Docker 的 CMD 和 ENTRYPOINT 区别？', '["A. 没有区别","B. CMD 可被覆盖，ENTRYPOINT 作为固定入口","C. 只能用一个","D. 都不需要"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Docker' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', '什么是 Docker 的 namespace 和 cgroups？', '["A. 网络协议","B. namespace 实现隔离，cgroups 实现资源限制","C. 存储方式","D. 不需要了解"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Docker' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'Docker Swarm 和 Kubernetes 的区别？', '["A. 一样","B. Swarm 更简单轻量内置，K8s 功能更强大但更复杂","C. Kubernetes 更简单","D. 不能一起用"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Docker' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'Docker 健康检查如何配置？', '["A. 不需要","B. Dockerfile 中 HEALTHCHECK 指令或在 Compose 中配置 healthcheck","C. 手动检查","D. 只能外部检测"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Docker' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', '容器退出后数据还在吗？如何保持？', '["A. 数据永远在","B. 默认丢失，需要使用 Volume 或 bind mount 持久化","C. 自动保存","D. 容器不退出"]', 'B', '中等');

-- ==================== Linux (ID ~23) ====================
-- Linux BASIC
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'Linux' AND is_deleted = 0 LIMIT 1), 'BASIC', 'Linux 中查看当前路径的命令是？', '["A. dir","B. pwd","C. ls","D. cd"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = 'Linux' AND is_deleted = 0 LIMIT 1), 'BASIC', 'Linux 中列出目录内容的命令是？', '["A. dir","B. ls","C. cd","D. cat"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = 'Linux' AND is_deleted = 0 LIMIT 1), 'BASIC', 'Linux 中查看文件内容的命令是？', '["A. read","B. cat","C. show","D. open"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = 'Linux' AND is_deleted = 0 LIMIT 1), 'BASIC', 'Linux 中赋予文件执行权限的命令是？', '["A. chown","B. chmod +x filename","C. chgrp","D. perm"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = 'Linux' AND is_deleted = 0 LIMIT 1), 'BASIC', 'Linux 中查看系统进程的命令是？', '["A. list","B. ps","C. show","D. process"]', 'B', '简单');

-- Linux FRAMEWORK
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'Linux' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'Linux 中如何查看端口占用？', '["A. port list","B. netstat -tlnp 或 ss -tlnp","C. port view","D. show ports"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Linux' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'Linux 中软链接和硬链接的区别？', '["A. 没有区别","B. 软链接类似快捷方式，硬链接共享 inode","C. 硬链接更快","D. 软链接不能删除"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Linux' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'Linux 中如何后台运行进程？', '["A. 不能","B. nohup 命令 或 命令 & 或 screen/tmux","C. 只能前台","D. 使用 cron"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Linux' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'Linux 中 grep 命令的作用是？', '["A. 创建文件","B. 文本搜索过滤","C. 删除文件","D. 编译代码"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Linux' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'Linux 中查看磁盘使用情况的命令是？', '["A. memory","B. df -h","C. storage","D. disk"]', 'B', '中等');

-- Linux PROJECT
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'Linux' AND is_deleted = 0 LIMIT 1), 'PROJECT', '如何编写 Shell 脚本批量处理文件？', '["A. 手动一个个处理","B. 使用 for 循环和 Shell 命令组合","C. 只能用 Python","D. 不能批量"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Linux' AND is_deleted = 0 LIMIT 1), 'PROJECT', 'Linux 服务器被攻击后如何排查？', '["A. 不理","B. 查看日志(/var/log)、检查异常进程、查看网络连接、检查定时任务","C. 重启","D. 格式化"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Linux' AND is_deleted = 0 LIMIT 1), 'PROJECT', 'Linux 定时任务(Crontab)格式是什么？', '["A. 只有时间","B. 分 时 日 月 周 命令","C. 只有命令","D. 不需要格式"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Linux' AND is_deleted = 0 LIMIT 1), 'PROJECT', 'Linux 中如何查看实时日志？', '["A. cat log","B. tail -f logfile","C. less log","D. more log"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Linux' AND is_deleted = 0 LIMIT 1), 'PROJECT', 'Linux 中 Top 命令如何按内存排序？', '["A. 不能排序","B. 按 shift + M","C. 按 Enter","D. 按 q"]', 'B', '中等');

-- Linux INTERVIEW
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'Linux' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'Linux 内核空间和用户空间的区别？', '["A. 没有区别","B. 内核空间运行 OS 核心，用户空间运行应用程序","C. 都运行在内核","D. 没有这个概念"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Linux' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'Linux 中 page cache 是什么？', '["A. 网页缓存","B. 内核用于缓存文件数据的内存，加速 IO","C. 网络缓存","D. 不需要了解"]', 'B', '困难'),
((SELECT id FROM skill WHERE name = 'Linux' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'Linux 文件权限 755 的含义？', '["A. 所有人可读写","B. 所有者 rwx，同组 r-x，其他 r-x","C. 所有人 rwx","D. 只读"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Linux' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', '什么是 Linux 的 inode？', '["A. 文件名","B. 文件元数据，包含文件大小、权限、时间戳和数据块指针","C. 目录","D. 不需要了解"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'Linux' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'Linux 中如何查找大文件？', '["A. 手动找","B. find / -type f -size +100M","C. ls 所有文件","D. 不能查找"]', 'B', '中等');

-- ==================== 数据结构 (ID ~28) ====================
-- 数据结构 BASIC
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = '数据结构' AND is_deleted = 0 LIMIT 1), 'BASIC', '以下哪种数据结构是 FIFO（先进先出）？', '["A. 栈","B. 队列","C. 堆","D. 树"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = '数据结构' AND is_deleted = 0 LIMIT 1), 'BASIC', '以下哪种数据结构是 LIFO（后进先出）？', '["A. 队列","B. 栈","C. 链表","D. 图"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = '数据结构' AND is_deleted = 0 LIMIT 1), 'BASIC', '数组和链表的主要区别是什么？', '["A. 没有区别","B. 数组连续内存随机访问快，链表离散内存插入删除快","C. 链表更快","D. 数组不能遍历"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = '数据结构' AND is_deleted = 0 LIMIT 1), 'BASIC', '链表访问第 i 个元素的时间复杂度是？', '["A. O(1)","B. O(n)","C. O(log n)","D. O(n²)"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = '数据结构' AND is_deleted = 0 LIMIT 1), 'BASIC', '数组访问第 i 个元素的时间复杂度是？', '["A. O(1)","B. O(n)","C. O(log n)","D. O(n²)"]', 'A', '简单');

-- 数据结构 FRAMEWORK
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = '数据结构' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', '二叉搜索树的中序遍历结果是？', '["A. 无序","B. 升序排列","C. 降序排列","D. 层序"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = '数据结构' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', '哈希表解决冲突的方法有哪些？', '["A. 只有一种","B. 链地址法、开放寻址法","C. 不能解决","D. 只能扩容"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = '数据结构' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', '堆（Heap）是什么数据结构？', '["A. 栈","B. 一种完全二叉树，满足堆序性","C. 数组","D. 链表"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = '数据结构' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', '图的深度优先遍历(DFS)使用什么数据结构？', '["A. 队列","B. 栈（递归调用栈）","C. 数组","D. 堆"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = '数据结构' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', '图的广度优先遍历(BFS)使用什么数据结构？', '["A. 栈","B. 队列","C. 堆","D. 链表"]', 'B', '中等');

-- 数据结构 PROJECT
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = '数据结构' AND is_deleted = 0 LIMIT 1), 'PROJECT', '在 Java 中，TreeMap 基于什么数据结构实现？', '["A. 哈希表","B. 红黑树","C. 链表","D. 数组"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = '数据结构' AND is_deleted = 0 LIMIT 1), 'PROJECT', '跳表（SkipList）是什么数据结构？Redis 中哪里使用了它？', '["A. 没用","B. 多层有序链表，ZSet 使用","C. 哈希表","D. 二叉树"]', 'B', '困难'),
((SELECT id FROM skill WHERE name = '数据结构' AND is_deleted = 0 LIMIT 1), 'PROJECT', 'LRU 缓存的实现通常用什么数据结构？', '["A. 数组","B. 哈希表 + 双向链表","C. 栈","D. 堆"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = '数据结构' AND is_deleted = 0 LIMIT 1), 'PROJECT', '布隆过滤器在什么场景使用？', '["A. 排序","B. 缓存穿透防护、去重、爬虫 URL 去重","C. 加密","D. 压缩"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = '数据结构' AND is_deleted = 0 LIMIT 1), 'PROJECT', '字典树（Trie）适用于什么场景？', '["A. 排序","B. 字符串前缀匹配、自动补全","C. 计算","D. 加密"]', 'B', '困难');

-- 数据结构 INTERVIEW
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = '数据结构' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', '什么是时间复杂度？O(1)、O(n)、O(log n) 各代表什么？', '["A. 代码长度","B. 算法执行时间的增长趋势","C. 内存占用","D. 程序大小"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = '数据结构' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', '什么是空间复杂度？', '["A. 时间","B. 算法运行时占用的额外内存空间增长趋势","C. 磁盘空间","D. 代码大小"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = '数据结构' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', '红黑树和 AVL 树的区别？', '["A. 没有区别","B. 红黑树插入删除效率更高，AVL 查询更平衡","C. AVL 更快","D. 不能比较"]', 'B', '困难'),
((SELECT id FROM skill WHERE name = '数据结构' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', '如何判断链表是否有环？', '["A. 无法判断","B. 快慢指针法（Floyd 判圈算法）","C. 遍历一遍","D. 计算长度"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = '数据结构' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', '什么是并查集（Union-Find）？适用于什么场景？', '["A. 排序","B. 快速判断连通性和合并集合，如朋友圈问题","C. 查找","D. 遍历"]', 'B', '困难');

-- ==================== RESTful API (ID ~27) ====================
-- RESTful API BASIC
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'RESTful API' AND is_deleted = 0 LIMIT 1), 'BASIC', 'REST 的缩写含义是什么？', '["A. Restful Server","B. Representational State Transfer（表述性状态转移）","C. Remote Service","D. Request Transfer"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = 'RESTful API' AND is_deleted = 0 LIMIT 1), 'BASIC', 'RESTful API 中，获取资源使用哪个 HTTP 方法？', '["A. POST","B. GET","C. DELETE","D. PATCH"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = 'RESTful API' AND is_deleted = 0 LIMIT 1), 'BASIC', 'RESTful API 中，创建资源使用哪个 HTTP 方法？', '["A. GET","B. POST","C. PUT","D. DELETE"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = 'RESTful API' AND is_deleted = 0 LIMIT 1), 'BASIC', 'RESTful API 中，删除资源使用哪个 HTTP 方法？', '["A. PUT","B. DELETE","C. GET","D. POST"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = 'RESTful API' AND is_deleted = 0 LIMIT 1), 'BASIC', 'HTTP 状态码 200 表示什么？', '["A. 未找到","B. 成功","C. 服务器错误","D. 未授权"]', 'B', '简单');

-- RESTful API FRAMEWORK
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'RESTful API' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'HTTP 状态码 404 表示什么？', '["A. 成功","B. 资源未找到","C. 服务器错误","D. 未授权"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = 'RESTful API' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'HTTP 状态码 500 表示什么？', '["A. 资源未找到","B. 服务器内部错误","C. 未授权","D. 成功"]', 'B', '简单'),
((SELECT id FROM skill WHERE name = 'RESTful API' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'PUT 和 PATCH 的区别是什么？', '["A. 没有区别","B. PUT 全量更新，PATCH 部分更新","C. PATCH 全量更新","D. PUT 部分更新"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'RESTful API' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'API 版本管理通常如何实现？', '["A. 不需要","B. URL 路径版本（/api/v1/）、Header 版本、Query 参数版本","C. 只用最新版","D. 每次改接口"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'RESTful API' AND is_deleted = 0 LIMIT 1), 'FRAMEWORK', 'RESTful API 的幂等性是什么？', '["A. 都一样","B. 多次相同请求的结果一致（GET/PUT/DELETE 天然幂等）","C. 不可重复","D. 只有一个"]', 'B', '中等');

-- RESTful API PROJECT
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'RESTful API' AND is_deleted = 0 LIMIT 1), 'PROJECT', '如何设计合理的 API 分页？', '["A. 不分页","B. 使用 page + size 或 cursor（游标）分页","C. 返回全部数据","D. 手动分页"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'RESTful API' AND is_deleted = 0 LIMIT 1), 'PROJECT', 'API 安全认证的常见方式有哪些？', '["A. 不做认证","B. JWT Token、OAuth2.0、API Key","C. 只用密码","D. 不需要安全"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'RESTful API' AND is_deleted = 0 LIMIT 1), 'PROJECT', 'Swagger/OpenAPI 的作用是什么？', '["A. 数据库管理","B. API 文档自动生成和在线测试","C. 代码编译","D. 没有作用"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'RESTful API' AND is_deleted = 0 LIMIT 1), 'PROJECT', '如何防止 API 被恶意频繁调用？', '["A. 不处理","B. 限流（Rate Limiting）+ IP 黑名单","C. 开放 API","D. 不做限制"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'RESTful API' AND is_deleted = 0 LIMIT 1), 'PROJECT', '@RequestMapping 和 @GetMapping 的关系是？', '["A. 没有关系","B. @GetMapping 是 @RequestMapping(method=GET) 的简化","C. @RequestMapping 不能用 GET","D. 不能同时使用"]', 'B', '中等');

-- RESTful API INTERVIEW
INSERT INTO `skill_test_question` (`skill_id`, `stage`, `question`, `options`, `correct_answer`, `difficulty`) VALUES
((SELECT id FROM skill WHERE name = 'RESTful API' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'REST 和 GraphQL 有什么区别？', '["A. 没有区别","B. REST 多端点固定数据，GraphQL 单端点灵活查询","C. GraphQL 不能查询","D. REST 更灵活"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'RESTful API' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'HATEOAS 是什么概念？', '["A. 一个框架","B. 超媒体驱动，响应中包含相关链接","C. 数据库","D. 不需要了解"]', 'B', '困难'),
((SELECT id FROM skill WHERE name = 'RESTful API' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'CORS 跨域问题如何解决？', '["A. 忽略","B. 后端设置 Access-Control-Allow-Origin 头或使用代理","C. 前端直接请求","D. 不能解决"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'RESTful API' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'API 返回格式的最佳实践是什么？', '["A. 随便返回","B. 统一返回结构 {code, message, data}","C. 只返回数据","D. 返回纯文本"]', 'B', '中等'),
((SELECT id FROM skill WHERE name = 'RESTful API' AND is_deleted = 0 LIMIT 1), 'INTERVIEW', 'POST 和 PUT 的区别是什么？', '["A. 一样","B. POST 创建（非幂等），PUT 更新/替换（幂等）","C. PUT 创建","D. POST 删除"]', 'B', '中等');

-- ==================== 统计验证 ====================
SELECT 'skill_test_question 题库初始化完成' AS message, COUNT(*) AS total FROM skill_test_question WHERE is_deleted = 0;
