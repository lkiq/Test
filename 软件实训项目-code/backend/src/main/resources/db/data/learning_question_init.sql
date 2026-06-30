-- ==========================================
-- 学习成果测评 - 种子题目数据（100题）
-- 5 阶段 × 20 题
-- ==========================================

-- ==================== 阶段1：BASIC（基础阶段）====================

INSERT INTO learning_question (stage, type, difficulty, content, options, answer, score, knowledge_point, explanation) VALUES
('BASIC', 'SELECT', 'EASY', '在Java中，以下哪个关键字用于定义类？', '["A. class","B. interface","C. enum","D. struct"]', 'A', 5, 'Java基础语法', 'class是Java中定义类的关键字'),
('BASIC', 'SELECT', 'EASY', 'HTTP协议默认使用的端口号是？', '["A. 21","B. 22","C. 80","D. 443"]', 'C', 5, '网络基础', 'HTTP默认端口80，HTTPS默认端口443'),
('BASIC', 'SELECT', 'EASY', '在Python中，以下哪个不是基本数据类型？', '["A. int","B. float","C. string","D. array"]', 'D', 5, 'Python基础', 'array不是Python内置基本类型，需要使用模块导入'),
('BASIC', 'SELECT', 'EASY', '关系型数据库中，以下哪个约束保证数据唯一性？', '["A. NOT NULL","B. PRIMARY KEY","C. DEFAULT","D. CHECK"]', 'B', 5, '数据库基础', 'PRIMARY KEY主键约束确保每行数据的唯一标识'),
('BASIC', 'SELECT', 'EASY', 'Git中用于查看提交历史的命令是？', '["A. git status","B. git log","C. git diff","D. git show"]', 'B', 5, 'Git基础', 'git log用于查看提交历史记录'),
('BASIC', 'SELECT', 'EASY', '在HTML中，哪个标签用于定义段落？', '["A. <div>","B. <span>","C. <p>","D. <br>"]', 'C', 5, 'HTML基础', '<p>标签定义段落，是最基本的文本容器标签'),
('BASIC', 'SELECT', 'EASY', 'Linux中用于列出目录内容的命令是？', '["A. cd","B. ls","C. pwd","D. mkdir"]', 'B', 5, 'Linux基础', 'ls（list）命令用于列出当前目录下的文件和子目录'),
('BASIC', 'SELECT', 'MEDIUM', '以下哪种排序算法的时间复杂度在平均情况下是O(nlogn)？', '["A. 冒泡排序","B. 插入排序","C. 快速排序","D. 选择排序"]', 'C', 5, '数据结构与算法', '快速排序平均时间复杂度为O(nlogn)，最坏情况为O(n²)'),
('BASIC', 'SELECT', 'MEDIUM', 'TCP协议位于OSI模型的哪一层？', '["A. 应用层","B. 传输层","C. 网络层","D. 数据链路层"]', 'B', 5, '网络基础', 'TCP是传输层协议，提供可靠的端到端数据传输'),
('BASIC', 'SELECT', 'MEDIUM', '在MySQL中，以下哪个索引类型使用B+树结构？', '["A. Hash索引","B. Full-text索引","C. 聚簇索引","D. 空间索引"]', 'C', 5, '数据库基础', 'MySQL InnoDB的聚簇索引使用B+树结构组织数据'),
('BASIC', 'SELECT', 'MEDIUM', '以下哪种设计模式属于创建型模式？', '["A. 观察者模式","B. 单例模式","C. 策略模式","D. 装饰器模式"]', 'B', 5, '设计模式', '单例模式确保一个类只有一个实例，属于创建型模式'),
('BASIC', 'SELECT', 'MEDIUM', '在CSS中，以下哪个选择器的优先级最高？', '["A. 类选择器","B. ID选择器","C. 标签选择器","D. 属性选择器"]', 'B', 5, 'CSS基础', 'ID选择器(#id)优先级最高（权重100），高于类选择器（10）和标签选择器（1）'),
('BASIC', 'MULTI', 'MEDIUM', '以下哪些是面向对象编程的三大特性？（多选）', '["A. 封装","B. 集成","C. 继承","D. 多态"]', 'A,C,D', 5, '面向对象编程', '面向对象的三大特性是封装、继承、多态'),
('BASIC', 'MULTI', 'EASY', '以下哪些属于关系型数据库？（多选）', '["A. MySQL","B. MongoDB","C. PostgreSQL","D. Redis"]', 'A,C', 5, '数据库基础', 'MySQL和PostgreSQL是关系型数据库，MongoDB是文档数据库，Redis是键值存储'),
('BASIC', 'SELECT', 'HARD', '以下哪个不是Java虚拟机(JVM)的组成部分？', '["A. 类加载器","B. 执行引擎","C. 编译器","D. 垃圾回收器"]', 'C', 5, 'Java进阶', '编译器(如javac)是JDK工具，不属于JVM运行时组件'),
('BASIC', 'SELECT', 'HARD', 'RESTful API中，以下哪个HTTP方法用于删除资源？', '["A. GET","B. POST","C. PUT","D. DELETE"]', 'D', 5, 'API设计', 'DELETE方法用于删除指定的资源'),
('BASIC', 'SELECT', 'EASY', '在CSS中，margin: 0 auto 可以实现什么效果？', '["A. 垂直居中","B. 水平居中","C. 元素隐藏","D. 固定定位"]', 'B', 5, 'CSS布局', 'margin: 0 auto 将左右外边距设为自动，实现块级元素水平居中'),
('BASIC', 'MULTI', 'MEDIUM', '以下哪些是常用的Linux文本编辑器？（多选）', '["A. vim","B. nano","C. gedit","D. ls"]', 'A,B,C', 5, 'Linux基础', 'vim、nano、gedit都是Linux文本编辑器，ls是文件列表命令'),
('BASIC', 'SELECT', 'MEDIUM', '在数据结构中，栈(Stack)遵循什么原则？', '["A. FIFO","B. LIFO","C. FILO","D. 随机访问"]', 'B', 5, '数据结构', '栈遵循后进先出(LIFO)原则，最后入栈的元素最先出栈'),
('BASIC', 'SELECT', 'EASY', 'JSON的全称是什么？', '["A. Java Source Object Notation","B. JavaScript Object Notation","C. Java Serialized Object Node","D. JavaScript Open Network"]', 'B', 5, '基础概念', 'JSON全称是JavaScript Object Notation，是一种轻量级数据交换格式');

-- ==================== 阶段2：FRAMEWORK（框架阶段）====================

INSERT INTO learning_question (stage, type, difficulty, content, options, answer, score, knowledge_point, explanation) VALUES
('FRAMEWORK', 'SELECT', 'EASY', 'Spring Boot的自动配置是通过哪个注解实现的？', '["A. @Configuration","B. @EnableAutoConfiguration","C. @ComponentScan","D. @SpringBootApplication"]', 'B', 5, 'Spring Boot', '@EnableAutoConfiguration实现自动配置，@SpringBootApplication包含了该注解'),
('FRAMEWORK', 'SELECT', 'EASY', '在Vue.js中，哪个指令用于条件渲染？', '["A. v-for","B. v-if","C. v-bind","D. v-on"]', 'B', 5, 'Vue.js基础', 'v-if指令根据条件决定是否渲染元素，v-show只是切换display属性'),
('FRAMEWORK', 'SELECT', 'MEDIUM', 'Spring中，@Autowired默认按什么方式注入？', '["A. 名称","B. 类型","C. 构造器","D. 接口"]', 'B', 5, 'Spring框架', '@Autowired默认按类型(byType)注入，如有多个同类型Bean需配合@Qualifier'),
('FRAMEWORK', 'SELECT', 'MEDIUM', 'MyBatis中，#{}和${}的区别是什么？', '["A. 没有区别","B. #{}防止SQL注入，${}不防止","C. ${}防止SQL注入，#{}不防止","D. #{}用于查询，${}用于更新"]', 'B', 5, 'MyBatis', '#{}是预编译占位符防止SQL注入，${}是字符串拼接有注入风险'),
('FRAMEWORK', 'SELECT', 'EASY', 'Vue Router中，动态路由参数通过什么符号定义？', '["A. :","B. @","C. #","D. $"]', 'A', 5, 'Vue Router', '动态路由使用冒号定义，如 /user/:id 匹配 /user/123'),
('FRAMEWORK', 'SELECT', 'MEDIUM', 'Spring Boot项目中，application.yml和application.properties的关系是？', '["A. 互斥的","B. yml优先级高于properties","C. properties优先级高于yml","D. 两者同时加载"]', 'C', 5, 'Spring Boot配置', 'application.properties优先级高于application.yml，同目录下会覆盖'),
('FRAMEWORK', 'SELECT', 'MEDIUM', '在Vue 3中，以下哪个API用于创建响应式数据？', '["A. data()","B. ref()","C. createState()","D. useState()"]', 'B', 5, 'Vue3组合式API', 'ref()是Vue3组合式API中创建响应式数据的基本方法'),
('FRAMEWORK', 'SELECT', 'MEDIUM', 'Spring AOP中，以下哪个注解用于定义前置通知？', '["A. @After","B. @Before","C. @Around","D. @AfterReturning"]', 'B', 5, 'Spring AOP', '@Before注解定义前置通知，在目标方法执行前执行'),
('FRAMEWORK', 'SELECT', 'HARD', '在Spring中，Bean的默认作用域是什么？', '["A. request","B. session","C. singleton","D. prototype"]', 'C', 5, 'Spring核心', 'Spring Bean默认作用域是singleton（单例），整个容器中只有一个实例'),
('FRAMEWORK', 'MULTI', 'MEDIUM', '以下哪些是Element Plus中的常用组件？（多选）', '["A. el-table","B. el-form","C. el-button","D. el-console"]', 'A,B,C', 5, 'Element Plus', 'el-table、el-form、el-button是Element Plus组件，el-console不存在'),
('FRAMEWORK', 'SELECT', 'EASY', '在Spring Boot中，@RestController注解组合了哪两个注解？', '["A. @Controller和@ResponseBody","B. @Service和@Controller","C. @Component和@Controller","D. @Repository和@ResponseBody"]', 'A', 5, 'Spring Boot', '@RestController = @Controller + @ResponseBody，表示RESTful控制器'),
('FRAMEWORK', 'SELECT', 'MEDIUM', 'Vuex中，用于修改state的唯一途径是？', '["A. actions","B. mutations","C. getters","D. modules"]', 'B', 5, 'Vue状态管理', 'mutations是Vuex中唯一能修改state的同步事务，actions通过commit触发mutation'),
('FRAMEWORK', 'SELECT', 'HARD', 'Spring事务管理中，@Transactional的默认传播行为是什么？', '["A. REQUIRES_NEW","B. SUPPORTS","C. REQUIRED","D. MANDATORY"]', 'C', 5, 'Spring事务', 'PROPAGATION_REQUIRED是默认传播行为：有事务则加入，无则创建新事务'),
('FRAMEWORK', 'SELECT', 'MEDIUM', '在Vue中，computed和methods的主要区别是什么？', '["A. 没有区别","B. computed有缓存，methods每次重新计算","C. methods有缓存，computed不缓存","D. computed只能用于模板"]', 'B', 5, 'Vue特性', 'computed基于依赖缓存，依赖不变不重新计算；methods每次调用都执行'),
('FRAMEWORK', 'SELECT', 'EASY', 'MyBatis Plus中，BaseMapper提供了哪个基础的增删改查？', '["A. selectById","B. findById","C. getById","D. readById"]', 'A', 5, 'MyBatis Plus', 'BaseMapper提供selectById、insert、updateById、deleteById等CRUD方法'),
('FRAMEWORK', 'MULTI', 'MEDIUM', '以下哪些是常用的前端打包工具？（多选）', '["A. Webpack","B. Vite","C. Maven","D. Rollup"]', 'A,B,D', 5, '前端工程化', 'Webpack、Vite、Rollup是前端打包工具，Maven是Java构建工具'),
('FRAMEWORK', 'SELECT', 'MEDIUM', '在Spring Boot中，@Value注解的作用是什么？', '["A. 注入Bean","B. 读取配置文件值","C. 声明事务","D. 标记REST接口"]', 'B', 5, 'Spring Boot配置', '@Value用于将配置文件（yml/properties）中的值注入到字段'),
('FRAMEWORK', 'SELECT', 'HARD', '在Vue3中，Teleport组件的作用是什么？', '["A. 异步加载组件","B. 将组件模板渲染到指定DOM节点","C. 实现组件通信","D. 缓存组件状态"]', 'B', 5, 'Vue3新特性', 'Teleport允许将组件的模板部分渲染到DOM中的其他位置（如body下）'),
('FRAMEWORK', 'SELECT', 'EASY', 'npm中用于安装项目依赖的命令是？', '["A. npm run","B. npm install","C. npm start","D. npm build"]', 'B', 5, '前端工程化', 'npm install（或npm i）安装package.json中声明的所有依赖'),
('FRAMEWORK', 'SELECT', 'MEDIUM', 'Spring Security中，哪个接口用于加载用户信息？', '["A. AuthenticationManager","B. UserDetailsService","C. PasswordEncoder","D. SecurityContext"]', 'B', 5, 'Spring Security', 'UserDetailsService接口的loadUserByUsername方法负责从数据源加载用户信息');

-- ==================== 阶段3：PROJECT（项目阶段）====================

INSERT INTO learning_question (stage, type, difficulty, content, options, answer, score, knowledge_point, explanation) VALUES
('PROJECT', 'SELECT', 'EASY', '在项目开发中，敏捷开发方法最常用的框架是？', '["A. Waterfall","B. Scrum","C. V-Model","D. RAD"]', 'B', 5, '项目管理', 'Scrum是最常用的敏捷开发框架，采用迭代、增量的方式交付软件'),
('PROJECT', 'SELECT', 'MEDIUM', 'Git中，用于创建新分支的命令是？', '["A. git branch new","B. git checkout -b new","C. git create new","D. A和B都对"]', 'D', 5, 'Git分支', 'git branch new创建分支，git checkout -b new创建并切换到新分支'),
('PROJECT', 'SELECT', 'MEDIUM', '微服务架构中，以下哪种不建议用于服务间通信？', '["A. HTTP/REST","B. gRPC","C. 共享数据库","D. 消息队列"]', 'C', 5, '微服务架构', '微服务应避免共享数据库，每个服务应有独立的数据存储，通过API通信'),
('PROJECT', 'SELECT', 'MEDIUM', '在Docker中，以下哪个命令用于构建镜像？', '["A. docker run","B. docker build","C. docker create","D. docker image"]', 'B', 5, 'Docker', 'docker build -t name:tag . 根据Dockerfile构建镜像'),
('PROJECT', 'SELECT', 'EASY', '代码审查(Code Review)的主要目的是？', '["A. 找bug","B. 提高代码质量和分享知识","C. 惩罚代码写得差的人","D. 延长项目周期"]', 'B', 5, '软件工程实践', 'Code Review主要目的是提升代码质量、发现潜在问题、促进团队知识共享'),
('PROJECT', 'SELECT', 'MEDIUM', 'Git中合并分支时出现冲突，以下哪个说法正确？', '["A. Git会自动解决所有冲突","B. 需要手动解决冲突后提交","C. 冲突只能放弃本次合并","D. 冲突时只能回滚"]', 'B', 5, 'Git协作', '冲突需要开发者手动编辑文件解决，然后git add标记解决，再提交'),
('PROJECT', 'SELECT', 'HARD', 'CI/CD中，"持续部署"（CD）的含义是？', '["A. 自动编译代码","B. 自动运行测试","C. 自动将代码部署到生产环境","D. 自动创建分支"]', 'C', 5, 'CI/CD', '持续部署(Continuous Deployment)指代码通过测试后自动部署到生产环境'),
('PROJECT', 'SELECT', 'MEDIUM', '在RESTful API设计中，以下哪个URL风格最合适？', '["A. /api/getUsers","B. /api/users","C. /api/user_list","D. /api/getUserList"]', 'B', 5, 'API设计规范', 'RESTful风格使用名词复数表示资源集合，如/api/users'),
('PROJECT', 'MULTI', 'MEDIUM', '以下哪些属于项目质量控制方法？（多选）', '["A. 代码审查","B. 单元测试","C. 加班赶工","D. 静态代码分析"]', 'A,B,D', 5, '质量管理', '代码审查、单元测试、静态代码分析是质量管理方法，加班不是有效的质量手段'),
('PROJECT', 'SELECT', 'EASY', '敏捷开发中的"Sprint"通常持续多长时间？', '["A. 1天","B. 1-4周","C. 3个月","D. 半年"]', 'B', 5, 'Scrum实践', 'Sprint通常持续1-4周，2周是最常见的Sprint长度'),
('PROJECT', 'SELECT', 'MEDIUM', '以下关于Git Rebase的描述，哪个是正确的？', '["A. 丢弃合并记录","B. 将提交历史整理为线性","C. 等同于git merge","D. 永远不会产生冲突"]', 'B', 5, 'Git高级', 'Rebase将分支的提交变基到目标分支顶端，产生线性历史，更清晰但需谨慎'),
('PROJECT', 'SELECT', 'HARD', '在微服务中，服务发现（Service Discovery）的主要作用是？', '["A. 记录日志","B. 自动定位服务实例的网络位置","C. 监控CPU使用","D. 管理数据库连接"]', 'B', 5, '微服务架构', '服务发现让微服务能动态找到其他服务的网络地址，实现服务间调用'),
('PROJECT', 'SELECT', 'MEDIUM', '单元测试中，Mock的主要目的是？', '["A. 加快测试速度","B. 隔离被测试代码的依赖","C. 替换整个系统","D. 生成测试报告"]', 'B', 5, '测试实践', 'Mock用于模拟外部依赖（如数据库、API），使单元测试独立且可重复'),
('PROJECT', 'SELECT', 'EASY', '团队使用Git时，通常不建议频繁向哪个分支直接提交？', '["A. feature分支","B. main/master分支","C. develop分支","D. hotfix分支"]', 'B', 5, 'Git Flow', 'main/master是生产分支，通常只通过PR/MR合并，不应直接提交'),
('PROJECT', 'SELECT', 'MEDIUM', '以下哪种策略有助于提高代码可维护性？', '["A. 尽可能少写注释","B. 遵循SOLID原则","C. 所有代码放在一个文件","D. 使用全局变量"]', 'B', 5, '代码质量', 'SOLID原则（单一职责、开闭等）是面向对象设计的核心原则，提高可维护性'),
('PROJECT', 'MULTI', 'MEDIUM', '以下哪些是常用的项目文档类型？（多选）', '["A. API接口文档","B. 需求规格说明书","C. 部署运维手册","D. 聊天记录"]', 'A,B,C', 5, '项目文档', 'API文档、需求说明、部署手册是常见的项目文档，聊天记录不属于正式文档'),
('PROJECT', 'SELECT', 'MEDIUM', '在Docker Compose中，以下哪个字段用于定义服务？', '["A. containers","B. services","C. apps","D. modules"]', 'B', 5, 'Docker Compose', 'docker-compose.yml中services字段定义各个容器服务'),
('PROJECT', 'SELECT', 'HARD', '以下关于TDD（测试驱动开发）的说法，哪个是正确的？', '["A. 先写代码再写测试","B. 先写测试再写代码","C. 只写测试不写代码","D. 不需要测试"]', 'B', 5, 'TDD', 'TDD提倡先写失败的测试→写最少代码让测试通过→重构，红-绿-重构循环'),
('PROJECT', 'SELECT', 'EASY', 'Git中.gitignore文件的作用是？', '["A. 配置Git用户信息","B. 忽略不需要版本控制的文件","C. 定义分支策略","D. 存储远程仓库地址"]', 'B', 5, 'Git基础', '.gitignore列出Git需要忽略的文件模式（如node_modules、.env等）'),
('PROJECT', 'SELECT', 'MEDIUM', 'API版本管理的最佳实践是？', '["A. 在URL中加入版本号","B. 每次更新都要大改","C. 不进行版本管理","D. 只用一个版本"]', 'A', 5, 'API设计', 'URL中加入版本号（如/api/v1/users）是常见的API版本管理方式，保持向后兼容');

-- ==================== 阶段4：INTERVIEW（面试阶段）====================

INSERT INTO learning_question (stage, type, difficulty, content, options, answer, score, knowledge_point, explanation) VALUES
('INTERVIEW', 'SELECT', 'MEDIUM', '在面试中，以下哪个不是常见的算法题类型？', '["A. 动态规划","B. 二叉树遍历","C. 制作PPT","D. 字符串处理"]', 'C', 5, '面试准备', '制作PPT不属于算法面试范畴，常见的算法题包括DP、树、字符串、图等'),
('INTERVIEW', 'SELECT', 'MEDIUM', '系统设计面试中，CAP定理指的是什么？', '["A. 一致性+可用性+分区容错","B. 正确性+效率+性能","C. 代码+架构+平台","D. 客户端+服务端+数据库"]', 'A', 5, '系统设计', 'CAP定理：分布式系统无法同时满足一致性、可用性和分区容错性'),
('INTERVIEW', 'SELECT', 'HARD', '下面哪种情况会导致内存泄漏？', '["A. 及时释放对象引用","B. 使用try-with-resources","C. 静态集合持有对象引用不释放","D. 使用垃圾回收"]', 'C', 5, 'Java进阶', '静态集合持有对象引用且不清理，会导致内存无法被GC回收'),
('INTERVIEW', 'SELECT', 'MEDIUM', '行为面试中，STAR法则的S代表什么？', '["A. Simple","B. Situation","C. Style","D. Score"]', 'B', 5, '面试技巧', 'STAR：Situation(情境)、Task(任务)、Action(行动)、Result(结果)'),
('INTERVIEW', 'SELECT', 'MEDIUM', '在算法面试中，空间复杂度和时间复杂度哪个是主要优化目标？', '["A. 只优化时间复杂度","B. 只优化空间复杂度","C. 根据题目要求权衡","D. 不需要考虑"]', 'C', 5, '算法分析', '时空复杂度需要根据实际场景权衡，通常优先时间复杂度，有时用空间换时间'),
('INTERVIEW', 'SELECT', 'HARD', 'Redis中，以下哪种数据结构适合实现排行榜？', '["A. String","B. Hash","C. Sorted Set","D. List"]', 'C', 5, 'Redis', 'Sorted Set(ZSet)基于score排序，天然适合排行榜场景'),
('INTERVIEW', 'SELECT', 'MEDIUM', '以下关于HTTPS的说法，哪个是正确的？', '["A. HTTPS不加密传输数据","B. HTTPS使用SSL/TLS加密通信","C. HTTPS比HTTP慢是因为不需要验证","D. HTTPS不需要证书"]', 'B', 5, '网络安全', 'HTTPS通过SSL/TLS协议对通信进行加密，需要CA颁发的数字证书'),
('INTERVIEW', 'MULTI', 'MEDIUM', '以下哪些是常见的系统设计原则？（多选）', '["A. 高内聚低耦合","B. 所有代码放在一起","C. 面向接口编程","D. 单一职责原则"]', 'A,C,D', 5, '系统设计', '高内聚低耦合、面向接口编程、单一职责是核心设计原则'),
('INTERVIEW', 'SELECT', 'MEDIUM', '在数据库设计中，数据库范式的目的是什么？', '["A. 增加数据冗余","B. 减少数据冗余和异常","C. 提高查询速度","D. 增加存储空间"]', 'B', 5, '数据库设计', '数据库范式化旨在减少数据冗余、消除插入/更新/删除异常'),
('INTERVIEW', 'SELECT', 'HARD', '以下关于索引的说法，哪个是错误的？', '["A. 索引能加快查询速度","B. 索引会占用存储空间","C. 索引越多越好","D. 索引会降低写入性能"]', 'C', 5, '数据库优化', '索引不是越多越好，过多的索引会严重降低写入性能并占用大量存储'),
('INTERVIEW', 'SELECT', 'EASY', '技术面试中，遇到不会的问题应该怎么做？', '["A. 随便编一个答案","B. 诚实回答并展示思考过程","C. 直接跳过","D. 抱怨题目太难"]', 'B', 5, '面试软技能', '诚实+展示思路体现解决问题的能力，比直接放弃更好'),
('INTERVIEW', 'SELECT', 'MEDIUM', 'JWT（JSON Web Token）由哪三部分组成？', '["A. Header、Payload、Signature","B. Head、Body、Footer","C. Start、Middle、End","D. Key、Value、Token"]', 'A', 5, '认证授权', 'JWT由Header(头部)、Payload(载荷)、Signature(签名)三部分组成'),
('INTERVIEW', 'SELECT', 'HARD', '以下哪种情况适合使用消息队列？', '["A. 简单的CRUD接口","B. 异步处理/削峰填谷","C. 实时数据库查询","D. 文件存储"]', 'B', 5, '架构设计', '消息队列适合异步解耦、流量削峰填谷、日志收集等场景'),
('INTERVIEW', 'SELECT', 'MEDIUM', '在算法面试中，滑动窗口技术主要用于解决什么问题？', '["A. 排序问题","B. 子数组/子串问题","C. 图遍历问题","D. 树结构问题"]', 'B', 5, '算法技巧', '滑动窗口常用于解决子数组、子串等连续区间的查找、统计问题'),
('INTERVIEW', 'MULTI', 'MEDIUM', '以下哪些是面试时的加分项？（多选）', '["A. 有开源项目贡献","B. 能清晰表达技术观点","C. 简历夸大经历","D. 了解公司业务"]', 'A,B,D', 5, '面试软技能', '开源贡献、清晰表达、了解公司业务是加分项，夸大经历是减分项'),
('INTERVIEW', 'SELECT', 'MEDIUM', '负载均衡（Load Balancing）的主要目的是？', '["A. 提高单台服务器性能","B. 将请求均匀分配到多台服务器","C. 降低服务器配置","D. 增加服务器数量"]', 'B', 5, '架构设计', '负载均衡将请求分摊到多台服务器，避免单点过载，提高可用性'),
('INTERVIEW', 'SELECT', 'HARD', '关于分布式锁，以下哪个说法正确？', '["A. 分布式锁只能用数据库实现","B. Redis的SETNX可以实现分布式锁","C. 分布式锁不需要设置超时","D. 单机应用才需要分布式锁"]', 'B', 5, '分布式系统', 'Redis的SETNX+过期时间可实现分布式锁，防止死锁需要设置超时'),
('INTERVIEW', 'SELECT', 'MEDIUM', 'SQL注入攻击可以通过什么方式防御？', '["A. 拼接SQL字符串","B. 使用预编译语句","C. 关闭防火墙","D. 不使用数据库"]', 'B', 5, '安全', '使用PreparedStatement预编译语句是最有效的SQL注入防御手段'),
('INTERVIEW', 'SELECT', 'EASY', '自我介绍时通常应该控制在多长时间？', '["A. 1分钟","B. 2-3分钟","C. 30分钟","D. 不限制"]', 'B', 5, '面试技巧', '自我介绍控制在2-3分钟为宜，突出核心技能和项目经验'),
('INTERVIEW', 'SELECT', 'MEDIUM', '以下哪种数据库分库分表策略是在应用层实现的？', '["A. MySQL主从复制","B. ShardingSphere","C. 数据库自带分区","D. 磁盘RAID"]', 'B', 5, '数据库架构', 'ShardingSphere是应用层分库分表中间件，对业务代码透明');

-- ==================== 阶段5：FINAL（综合阶段）====================

INSERT INTO learning_question (stage, type, difficulty, content, options, answer, score, knowledge_point, explanation) VALUES
('FINAL', 'SELECT', 'HARD', '一个高并发系统中，以下哪种方案最适合应对流量突发？', '["A. 增加单机配置","B. 弹性伸缩+消息队列削峰","C. 限制所有IP访问","D. 关闭非核心功能"]', 'B', 5, '架构设计', '弹性伸缩+消息队列是应对突发流量的经典组合，保证系统稳定性'),
('FINAL', 'SELECT', 'MEDIUM', '在团队协作中，以下哪种不是有效的沟通方式？', '["A. 每日站会","B. 只在IM上发消息","C. 团队周会","D. 一对一反馈"]', 'B', 5, '团队协作', '仅靠IM文字沟通容易产生误解，关键问题应面对面或视频沟通'),
('FINAL', 'SELECT', 'MEDIUM', '以下关于代码重构的说法，哪个是正确的？', '["A. 重构必须和功能开发一起做","B. 重构不改变外部行为只改善内部结构","C. 重构就是重写","D. 重构只在项目结束时做"]', 'B', 5, '软件工程', '重构(Refactoring)在不改变外部行为的前提下改善代码内部结构和可读性'),
('FINAL', 'SELECT', 'HARD', 'DDD（领域驱动设计）中，聚合根(Aggregate Root)的作用是？', '["A. 存储所有实体","B. 作为外部访问聚合的统一入口","C. 替代数据库","D. 记录所有操作日志"]', 'B', 5, '领域驱动设计', '聚合根是聚合的唯一入口，外部对象只能通过聚合根访问聚合内实体'),
('FINAL', 'SELECT', 'MEDIUM', '以下哪个不是衡量项目成功的关键指标？', '["A. 用户满意度","B. 代码行数","C. 按时交付","D. 系统稳定性"]', 'B', 5, '项目管理', '代码行数不能衡量项目成功，反而可能因为冗余代码降低质量'),
('FINAL', 'SELECT', 'MEDIUM', '技术债务(Technical Debt)最合理的处理方式是？', '["A. 永远不管","B. 有计划地逐步偿还","C. 全部推倒重来","D. 让新人处理"]', 'B', 5, '技术管理', '技术债务应纳入日常迭代计划，有计划地逐步偿还，避免累计无法控制'),
('FINAL', 'MULTI', 'MEDIUM', '以下哪些属于职业素养的表现？（多选）', '["A. 按时完成任务","B. 主动沟通问题","C. 代码写完不测试","D. 持续学习新技术"]', 'A,B,D', 5, '职业素养', '按时完成、主动沟通、持续学习是职业素养，不写测试是不专业的行为'),
('FINAL', 'SELECT', 'HARD', '关于缓存穿透、击穿、雪崩，以下哪个说法正确？', '["A. 三者是同一个概念","B. 布隆过滤器可防止穿透","C. 击穿不需要处理","D. 雪崩不会导致系统故障"]', 'B', 5, '系统设计', '布隆过滤器可通过快速判断key是否存在来防止缓存穿透'),
('FINAL', 'SELECT', 'MEDIUM', '在项目规划中，MVP指的是什么？', '["A. 最有价值球员","B. 最小可行产品","C. 最大版本计划","D. 多功能产品"]', 'B', 5, '产品思维', 'MVP(Minimum Viable Product)最小可行产品，用最少功能验证核心假设'),
('FINAL', 'SELECT', 'MEDIUM', '全栈开发工程师通常需要掌握哪些技能？', '["A. 只有前端","B. 只有后端","C. 前后端+基础运维+数据库","D. 只需要会写代码"]', 'C', 5, '职业发展', '全栈开发需要掌握前端、后端、数据库和基础运维等多项技能'),
('FINAL', 'SELECT', 'MEDIUM', '在项目中遇到技术难题，最佳处理方式是？', '["A. 自己闷头研究","B. 先搜索调研，再及时求助","C. 直接放弃","D. 把任务推给别人"]', 'B', 5, '问题解决', '先做调研尝试解决，设定时限后再求助，体现独立思考和团队协作'),
('FINAL', 'SELECT', 'HARD', '以下哪种架构模式最适合快速迭代的创业项目？', '["A. 单体架构先行","B. 从一开始就全微服务","C. 每功能一个服务","D. 完全不用架构"]', 'A', 5, '架构决策', '初创项目应先用单体快速验证，随着规模扩大再逐步拆分微服务'),
('FINAL', 'MULTI', 'MEDIUM', '以下哪些是提升代码质量的有效手段？（多选）', '["A. 编写单元测试","B. 代码审查(Code Review)","C. 使用静态分析工具","D. 复制粘贴代码"]', 'A,B,C', 5, '代码质量', '单测、Code Review、静态分析都是提升代码质量的手段，复制粘贴降低质量'),
('FINAL', 'SELECT', 'MEDIUM', '持续集成(CI)的核心价值是？', '["A. 自动部署","B. 尽早发现集成问题","C. 自动生成文档","D. 替代人工测试"]', 'B', 5, 'DevOps', 'CI的核心价值是频繁集成代码，尽早发现并修复集成错误'),
('FINAL', 'SELECT', 'EASY', '写一个好的技术文档，以下哪项最重要？', '["A. 字数多","B. 清晰的逻辑和结构","C. 华丽的排版","D. 用英文"]', 'B', 5, '文档写作', '清晰的结构和逻辑是技术文档最重要的品质，让读者快速理解'),
('FINAL', 'SELECT', 'HARD', '关于分布式事务，以下哪种解决方案是正确的？', '["A. 所有操作必须同步","B. 使用Seata等框架实现最终一致性","C. 分布式事务不需要处理","D. 只能回滚"]', 'B', 5, '分布式系统', 'Seata等分布式事务框架可实现最终一致性，适合多数业务场景'),
('FINAL', 'SELECT', 'MEDIUM', '产品设计中，用户体验(UX)的核心关注点是？', '["A. 颜色好看","B. 用户需求和操作便捷","C. 动画炫酷","D. 功能越多越好"]', 'B', 5, '产品设计', 'UX的核心是以用户为中心，满足用户需求的同时提供便捷的操作体验'),
('FINAL', 'MULTI', 'EASY', '以下哪些是良好的编程习惯？（多选）', '["A. 给变量取有意义的名字","B. 适当添加注释","C. 一个函数功能单一","D. 使用goto语句"]', 'A,B,C', 5, '编程规范', '有意义的命名、适当注释、单一职责函数都是好习惯，goto应避免使用'),
('FINAL', 'SELECT', 'MEDIUM', '在职业发展中，以下哪种态度最受企业青睐？', '["A. 只做分内事","B. 主动学习和承担责任","C. 等到被要求才学习","D. 回避挑战性任务"]', 'B', 5, '职业发展', '主动学习、勇于承担的态度是职场上最受欢迎的品质'),
('FINAL', 'SELECT', 'HARD', '健康检查(Health Check)在微服务中的作用是？', '["A. 仅是装饰","B. 让外部系统感知服务状态","C. 替代监控系统","D. 只用来重启服务"]', 'B', 5, '微服务运维', '健康检查让负载均衡/服务发现知道实例是否可用，实现故障自动摘除');
