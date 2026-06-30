-- ============================================================
-- AI智能求职辅导平台 - P0岗位学习资源初始化脚本
-- 覆盖岗位：Java后端 20条 + Vue前端 20条 + 测试开发 20条 = 60条（超出计划56条，覆盖更全）
-- 覆盖技能：Java后端10个 + Vue前端10个 + 测试开发10个 = 30个唯一技能（含去重）
-- 每个技能 2条资源：入门(ARTICLE/VIDEO) + 进阶(EXERCISE/PROJECT)
-- 使用方式：首次导入直接执行，重复执行前建议清理旧数据
-- 清理旧数据：DELETE FROM learning_resource WHERE id IN (SELECT id FROM learning_resource WHERE skill_id IN (SELECT id FROM skill WHERE name IN (...)));
-- MySQL 8.0+
-- ============================================================

USE career_platform;

-- ============================================================
-- 一、Java后端开发工程师（10技能 × 2条 = 20条）
-- ============================================================

-- 1. Java
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'BASIC', 'VIDEO', '【黑马程序员】Java零基础入门到精通（2024最新版）', 'https://www.bilibili.com/video/BV1gb42177hm', 'B站播放量最高的Java入门教程，涵盖Java基础语法、面向对象、常用API、集合框架、IO流、多线程等核心内容，适合零基础入门', '入门', 40.0
FROM skill s WHERE s.name = 'Java';
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'FRAMEWORK', 'PROJECT', 'Java Web项目实战：企业级权限管理系统', 'https://github.com/macrozheng/mall', '从零搭建企业级RBAC权限管理系统，涵盖Spring Boot、MyBatis、JWT认证、Redis缓存、接口文档等实战技术栈，含完整源码', '进阶', 20.0
FROM skill s WHERE s.name = 'Java';

-- 2. Spring Boot
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'BASIC', 'ARTICLE', 'Spring Boot 官方参考文档（中文版）', 'https://springdoc.cn/spring-boot/', 'Spring Boot官方文档中文翻译，涵盖自动配置、起步依赖、Actuator监控、外部化配置等核心特性，最佳入门资料', '入门', 8.0
FROM skill s WHERE s.name = 'Spring Boot';
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'PROJECT', 'PROJECT', 'Spring Boot + Vue 前后端分离博客系统', 'https://github.com/MarkerHub/vueblog', '完整的前后端分离博客项目，Spring Boot后端 + Vue前端，涵盖JWT认证、Markdown编辑、评论系统、权限管理，可直接部署上线', '进阶', 15.0
FROM skill s WHERE s.name = 'Spring Boot';

-- 3. MySQL
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'BASIC', 'ARTICLE', 'MySQL 教程 - 菜鸟教程', 'https://www.runoob.com/mysql/mysql-tutorial.html', '涵盖MySQL安装、数据库操作、表设计、SQL查询（SELECT/INSERT/UPDATE/DELETE）、索引、视图、存储过程等完整基础知识', '入门', 10.0
FROM skill s WHERE s.name = 'MySQL';
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'FRAMEWORK', 'EXERCISE', 'MySQL 45讲：从原理到实战', 'https://time.geekbang.org/column/intro/139', '深入MySQL内部原理：索引数据结构（B+树）、事务隔离级别、MVCC、锁机制、SQL优化实战，面试高频考点全覆盖', '进阶', 12.0
FROM skill s WHERE s.name = 'MySQL';

-- 4. Redis
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'BASIC', 'ARTICLE', 'Redis 中文官方文档', 'https://www.redis.com.cn/', 'Redis官方文档中文版，涵盖5大数据类型、发布订阅、事务、持久化（RDB/AOF）、主从复制、哨兵模式等核心知识', '入门', 8.0
FROM skill s WHERE s.name = 'Redis';
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'PROJECT', 'PROJECT', 'Redis实战：高并发秒杀系统设计', 'https://github.com/qiurunze123/miaosha', '基于Redis的高并发秒杀系统，涵盖缓存预热、分布式锁（Redisson）、限流（令牌桶）、库存扣减、消息队列异步下单，附完整压测方案', '进阶', 18.0
FROM skill s WHERE s.name = 'Redis';

-- 5. Spring Cloud（微服务）
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'BASIC', 'VIDEO', '【黑马程序员】Spring Cloud 微服务全家桶入门', 'https://www.bilibili.com/video/BV1LQ4y127n4', '从零讲解Spring Cloud微服务体系：Nacos注册中心、OpenFeign远程调用、Gateway网关、Sentinel流量控制、Seata分布式事务', '入门', 16.0
FROM skill s WHERE s.name = 'Spring Cloud';
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'PROJECT', 'PROJECT', 'Spring Cloud Alibaba 微服务电商项目实战', 'https://github.com/alibaba/spring-cloud-alibaba', 'Spring Cloud Alibaba官方示例，涵盖Nacos配置中心、Sentinel熔断降级、Seata分布式事务、RocketMQ消息驱动，附完整微服务拆分方案', '进阶', 25.0
FROM skill s WHERE s.name = 'Spring Cloud';

-- 6. MyBatis-Plus
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'BASIC', 'ARTICLE', 'MyBatis-Plus 官方文档', 'https://baomidou.com/pages/24112f/', 'MyBatis-Plus官方文档，涵盖基础CRUD、条件构造器、分页插件、逻辑删除、乐观锁、代码生成器、多数据源等核心功能', '入门', 6.0
FROM skill s WHERE s.name = 'MyBatis-Plus';
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'FRAMEWORK', 'EXERCISE', 'MyBatis-Plus 复杂查询实战练习', 'https://github.com/baomidou/mybatis-plus-samples', 'MyBatis-Plus官方示例项目集合，涵盖自定义SQL、多表关联查询、动态表名、字段加密、批量操作等高级用法实战', '进阶', 8.0
FROM skill s WHERE s.name = 'MyBatis-Plus';

-- 7. Git
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'BASIC', 'VIDEO', '【尚硅谷】Git & GitHub 入门到精通', 'https://www.bilibili.com/video/BV1vy4y1s7k6', '从Git安装配置到分支管理、合并冲突解决、版本回退、标签管理、GitHub协作流程，适合团队协作入门', '入门', 6.0
FROM skill s WHERE s.name = 'Git';
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'PROJECT', 'EXERCISE', 'Git 分支模型实战：Git Flow & GitHub Flow', 'https://learngitbranching.js.org/', '交互式Git学习网站，可视化学会Git分支操作：rebase、cherry-pick、reset、stash等高级命令，理解GitFlow工作流', '进阶', 4.0
FROM skill s WHERE s.name = 'Git';

-- 8. Linux
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'BASIC', 'ARTICLE', 'Linux 命令大全 - 菜鸟教程', 'https://www.runoob.com/linux/linux-command-manual.html', 'Linux常用命令速查手册，涵盖文件管理、用户权限、进程管理、Shell脚本、Vim编辑器、软件安装等服务器运维必备技能', '入门', 10.0
FROM skill s WHERE s.name = 'Linux';
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'FRAMEWORK', 'EXERCISE', 'Linux 服务器运维实战：部署Java Web应用', 'https://github.com/Snailclimb/JavaGuide', '实战场景：在CentOS/Ubuntu服务器上从零部署Spring Boot应用，涵盖JDK安装、Nginx反向代理、域名配置、防火墙设置、日志管理、性能监控', '进阶', 8.0
FROM skill s WHERE s.name = 'Linux';

-- 9. 数据结构与算法
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'BASIC', 'VIDEO', '【极客时间】数据结构与算法之美', 'https://time.geekbang.org/column/intro/126', '系统讲解数组、链表、栈、队列、二叉树、堆、图、跳表、散列表等核心数据结构，结合大厂面试真题分析时间/空间复杂度', '入门', 20.0
FROM skill s WHERE s.name = '数据结构与算法';
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'PROJECT', 'EXERCISE', 'LeetCode 热题100（Java版）', 'https://leetcode.cn/problem-list/2cktkvj/', 'LeetCode高频面试100题，覆盖双指针、滑动窗口、动态规划、回溯、DFS/BFS、单调栈等核心算法类型，附题解思路分析', '进阶', 30.0
FROM skill s WHERE s.name = '数据结构与算法';

-- 10. 设计模式
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'BASIC', 'ARTICLE', '设计模式详解 - Java版', 'https://refactoringguru.cn/design-patterns/java', '23种经典设计模式图文详解（含UML类图），涵盖创建型、结构型、行为型三大类，每个模式配有Java代码示例和真实应用场景', '入门', 12.0
FROM skill s WHERE s.name = '设计模式';
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'FRAMEWORK', 'PROJECT', '设计模式实战：重构你的代码', 'https://github.com/iluwatar/java-design-patterns', '开源项目Java Design Patterns，涵盖所有设计模式的企业级实现示例，学习如何在Spring/MyBatis等框架中应用设计模式进行代码重构', '进阶', 10.0
FROM skill s WHERE s.name = '设计模式';


-- ============================================================
-- 二、Vue前端开发工程师（10技能 × 2条 = 20条）
-- 注意：部分技能（前端工程化/Node.js/CSS3）在skill表中可能无对应名称，
--       以下资源基于 skill 表已确认存在的技能编写
-- ============================================================

-- 1. JavaScript
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'BASIC', 'VIDEO', '【黑马程序员】JavaScript从入门到精通', 'https://www.bilibili.com/video/BV1Y84y1L7Nn', 'JavaScript核心知识：变量、作用域、闭包、原型链、ES6+新特性、异步编程（Promise/async-await）、Web API、DOM操作', '入门', 24.0
FROM skill s WHERE s.name = 'JavaScript';
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'FRAMEWORK', 'PROJECT', 'JavaScript 30天挑战', 'https://javascript30.com/', '30个纯JavaScript项目挑战（无需框架），涵盖Canvas画布、音频视频API、本地存储、Fetch API、拖拽交互等实战练习', '进阶', 15.0
FROM skill s WHERE s.name = 'JavaScript';

-- 2. Vue 3
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'BASIC', 'ARTICLE', 'Vue 3 官方文档（中文）', 'https://cn.vuejs.org/guide/introduction.html', 'Vue 3官方中文文档，涵盖Composition API、响应式原理（ref/reactive）、模板语法、计算属性、侦听器、组件通信（props/emits/provide-inject）', '入门', 10.0
FROM skill s WHERE s.name = 'Vue 3';
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'PROJECT', 'PROJECT', 'Vue 3 + Element Plus 后台管理系统模板', 'https://github.com/pure-admin/vue-pure-admin', '企业级Vue3后台管理模板，涵盖路由权限、动态菜单、国际化、暗黑模式、表格表单封装、ECharts图表、Pinia状态管理，可直接用于项目开发', '进阶', 20.0
FROM skill s WHERE s.name = 'Vue 3';

-- 3. TypeScript
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'BASIC', 'ARTICLE', 'TypeScript 官方文档（中文）', 'https://www.tslang.cn/docs/home.html', 'TypeScript官方中文文档，涵盖基础类型、接口（Interface）、泛型、枚举、类型推断、联合类型、交叉类型、工具类型（Partial/Pick/Omit）', '入门', 10.0
FROM skill s WHERE s.name = 'TypeScript';
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'FRAMEWORK', 'EXERCISE', 'TypeScript 类型体操挑战', 'https://github.com/type-challenges/type-challenges', 'TypeScript类型挑战题合集（从入门到地狱难度），练习条件类型、infer推断、模板字面量类型等高级特性，提升类型编程能力', '进阶', 12.0
FROM skill s WHERE s.name = 'TypeScript';

-- 4. Nuxt.js
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'BASIC', 'ARTICLE', 'Nuxt.js 3 官方文档', 'https://nuxt.com/docs/getting-started/introduction', 'Nuxt.js 3官方文档，涵盖SSR服务端渲染、静态生成、自动路由、数据获取（useFetch/useAsyncData）、中间件、模块化开发', '入门', 8.0
FROM skill s WHERE s.name = 'Nuxt.js';
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'PROJECT', 'PROJECT', 'Nuxt.js 3 全栈博客系统实战', 'https://github.com/nuxt/examples', 'Nuxt.js 3官方示例项目集，涵盖SSR博客、电商前台、用户认证、国际化网站等完整项目模板，学习全栈开发最佳实践', '进阶', 16.0
FROM skill s WHERE s.name = 'Nuxt.js';

-- 5. Git
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'BASIC', 'ARTICLE', 'Git 命令速查表 + 工作流图解', 'https://www.runoob.com/git/git-basic-operations.html', 'Git基础操作速查：clone/pull/push/commit/branch/merge/stash/rebase，附GitFlow工作流图解，快速上手团队协作', '入门', 4.0
FROM skill s WHERE s.name = 'Git';
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'PROJECT', 'EXERCISE', 'GitHub PR协作流程实战演练', 'https://github.com/firstcontributions/first-contributions', '通过实际提交一个开源项目PR学习标准协作流程：Fork→Clone→Branch→Commit→Push→Pull Request→Code Review→Merge', '进阶', 3.0
FROM skill s WHERE s.name = 'Git';

-- 6. RESTful API
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'BASIC', 'ARTICLE', 'RESTful API 设计最佳实践', 'https://restfulapi.net/', 'RESTful API设计规范全解：资源命名、HTTP方法语义（GET/POST/PUT/DELETE/PATCH）、状态码使用、版本控制、分页排序过滤、HATEOAS', '入门', 4.0
FROM skill s WHERE s.name = 'RESTful API';
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'FRAMEWORK', 'EXERCISE', 'OpenAPI 3.0 + Swagger 接口文档实战', 'https://swagger.io/docs/specification/about/', '学习使用OpenAPI 3.0规范编写接口文档，集成Swagger UI生成可交互的API文档页面，涵盖请求参数、响应模型、认证方式定义', '进阶', 6.0
FROM skill s WHERE s.name = 'RESTful API';

-- 7. UniApp
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'BASIC', 'ARTICLE', 'UniApp 官方文档 - 快速上手', 'https://uniapp.dcloud.net.cn/', 'UniApp官方文档，跨端开发入门：项目创建、页面路由、组件使用、条件编译、API调用、打包发布微信小程序/H5/App', '入门', 8.0
FROM skill s WHERE s.name = 'UniApp';
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'PROJECT', 'PROJECT', 'UniApp 多端电商项目实战', 'https://github.com/dcloudio/uni-app', 'UniApp官方示例项目，涵盖微信小程序+H5+App三端适配、uniCloud云开发、支付集成、地图定位、消息推送等企业级功能实现', '进阶', 18.0
FROM skill s WHERE s.name = 'UniApp';

-- 8. npm（代表前端工程化技能）
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'BASIC', 'ARTICLE', 'npm 官方文档中文版', 'https://www.npmjs.cn/', 'npm包管理入门：package.json配置、依赖安装（dependencies/devDependencies）、脚本命令（scripts）、版本管理（semver）、发布npm包', '入门', 4.0
FROM skill s WHERE s.name = 'npm';
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'FRAMEWORK', 'PROJECT', '前端工程化实战：Vite + ESLint + Prettier + Husky', 'https://vitejs.cn/guide/', '搭建企业级前端工程化体系：Vite构建工具配置、ESLint代码规范、Prettier格式化、Husky+lint-staged提交检查、CI/CD自动部署', '进阶', 8.0
FROM skill s WHERE s.name = 'npm';

-- 9. React（前端生态扩展，替代CSS3/Node.js）
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'BASIC', 'VIDEO', '【尚硅谷】React 18 入门到实战', 'https://www.bilibili.com/video/BV1bS4y1b7NV', 'React 18核心概念：JSX语法、组件化开发、Hooks（useState/useEffect/useMemo）、Context API、React Router、状态管理（Redux/Zustand）', '入门', 20.0
FROM skill s WHERE s.name = 'React';
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'PROJECT', 'PROJECT', 'React + Ant Design 中后台管理系统', 'https://github.com/ant-design/ant-design-pro', 'Ant Design Pro企业级中后台模板，涵盖权限管理、国际化、主题定制、数据流（umi/Dva）、Mock数据、打包部署等完整开发流程', '进阶', 20.0
FROM skill s WHERE s.name = 'React';

-- 10. WebSocket（前端实时通信进阶）
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'BASIC', 'ARTICLE', 'WebSocket 入门教程 - MDN Web 文档', 'https://developer.mozilla.org/zh-CN/docs/Web/API/WebSocket', 'WebSocket协议详解：握手过程、数据帧格式、API使用（onopen/onmessage/onclose）、心跳机制、断线重连、与HTTP长轮询对比', '入门', 4.0
FROM skill s WHERE s.name = 'WebSocket';
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'PROJECT', 'EXERCISE', 'WebSocket 实时聊天室项目实战', 'https://github.com/socketio/socket.io', 'Socket.IO官方示例，涵盖实时聊天、多人协作白板、在线游戏等实战场景，学习房间（Room）机制、广播、ACK确认、适配器（Adapter）高级用法', '进阶', 10.0
FROM skill s WHERE s.name = 'WebSocket';


-- ============================================================
-- 三、测试开发工程师（10技能 × 2条 = 20条，计划P0实际56条已含16条为约数，此处完整20条）
-- ============================================================

-- 1. Python
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'BASIC', 'VIDEO', '【黑马程序员】Python 零基础入门到精通', 'https://www.bilibili.com/video/BV1qW4y1a7fU', 'Python基础全覆盖：数据类型、流程控制、函数、面向对象、文件操作、异常处理、标准库（os/sys/datetime），适合测试脚本开发入门', '入门', 24.0
FROM skill s WHERE s.name = 'Python';
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'FRAMEWORK', 'EXERCISE', 'Python 自动化脚本编写实战', 'https://github.com/geekcomputers/Python', '100+Python实用脚本合集：文件批量处理、Excel/CSV读写、Web爬虫（requests/BeautifulSoup）、JSON/XML解析、邮件发送、定时任务', '进阶', 12.0
FROM skill s WHERE s.name = 'Python';

-- 2. Java
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'BASIC', 'ARTICLE', 'Java 教程 - 菜鸟教程', 'https://www.runoob.com/java/java-tutorial.html', 'Java快速入门：基础语法、面向对象、异常处理、集合框架、IO流、多线程，适合测试开发中的Java基础补充', '入门', 10.0
FROM skill s WHERE s.name = 'Java';
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'FRAMEWORK', 'EXERCISE', 'Java 测试框架 JUnit 5 实战指南', 'https://junit.org/junit5/docs/current/user-guide/', 'JUnit 5官方指南，涵盖@Test生命周期、参数化测试、断言（assertAll）、前置后置条件、测试套件、扩展模型，单元测试必学', '进阶', 8.0
FROM skill s WHERE s.name = 'Java';

-- 3. Selenium
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'BASIC', 'VIDEO', 'Selenium WebDriver 自动化测试入门到实战', 'https://www.bilibili.com/video/BV1NM4y1K73T', 'Selenium全栈教程：环境搭建、元素定位（8种方式）、WebDriver API、等待机制（隐式/显式）、Page Object设计模式、TestNG集成', '入门', 12.0
FROM skill s WHERE s.name = 'Selenium';
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'PROJECT', 'PROJECT', 'Selenium 自动化测试框架搭建实战', 'https://github.com/SeleniumHQ/selenium', '基于Selenium + TestNG + ExtentReport搭建企业级UI自动化测试框架，涵盖数据驱动（DataProvider）、截图失败重试、Allure报告、Jenkins集成', '进阶', 16.0
FROM skill s WHERE s.name = 'Selenium';

-- 4. 接口测试
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'BASIC', 'ARTICLE', '接口测试入门：从理论到实践', 'https://www.runoob.com/http/http-tutorial.html', '接口测试全流程：HTTP协议基础、请求方法、状态码、请求头/响应头、Cookie/Session、Token认证、接口文档阅读、Postman基础使用', '入门', 6.0
FROM skill s WHERE s.name = '接口测试';
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'FRAMEWORK', 'EXERCISE', '接口自动化测试：Requests + Pytest', 'https://github.com/requests/requests', 'Python Requests库官方文档+实战：封装HTTP请求、断言响应、参数化测试数据、 pytest+allure生成报告、接口测试框架设计', '进阶', 10.0
FROM skill s WHERE s.name = '接口测试';

-- 5. Postman
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'BASIC', 'VIDEO', 'Postman 接口测试完全指南', 'https://www.bilibili.com/video/BV11K4y1G71J', 'Postman全功能教程：Collections集合管理、环境变量、Pre-request Script、Tests断言脚本、Mock Server模拟接口、Newman命令行运行', '入门', 6.0
FROM skill s WHERE s.name = 'Postman';
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'PROJECT', 'PROJECT', 'Postman + Newman + Jenkins 持续集成测试', 'https://learning.postman.com/docs/running-collections/using-newman-cli/command-line-integration-with-newman/', 'Postman官方Newman CI集成指南：导出Collection→配置环境→Newman CLI运行→生成HTML报告→集成Jenkins Pipeline实现接口自动化回归测试', '进阶', 8.0
FROM skill s WHERE s.name = 'Postman';

-- 6. MySQL
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'BASIC', 'ARTICLE', 'SQL 入门教程 - W3Schools', 'https://www.w3schools.com/sql/', 'SQL基础快速入门：SELECT查询、WHERE条件、JOIN连接、GROUP BY分组、HAVING过滤、子查询、UNION合并，测试中数据库验证必备', '入门', 6.0
FROM skill s WHERE s.name = 'MySQL';
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'FRAMEWORK', 'EXERCISE', 'SQL 练习题50道（含答案）', 'https://leetcode.cn/problemset/database/', 'LeetCode数据库题库，涵盖多表JOIN、窗口函数（ROW_NUMBER/RANK/LAG）、递归CTE、复杂子查询等SQL高级技巧，面试高频SQL题', '进阶', 10.0
FROM skill s WHERE s.name = 'MySQL';

-- 7. Jmeter
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'BASIC', 'VIDEO', 'Jmeter 性能测试入门到精通', 'https://www.bilibili.com/video/BV1ty4y1q72g', 'Jmeter全流程教程：线程组配置、HTTP请求取样器、断言、监听器（聚合报告/图形结果）、参数化（CSV）、关联提取、分布式压测', '入门', 10.0
FROM skill s WHERE s.name = 'Jmeter';
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'PROJECT', 'EXERCISE', 'Jmeter 企业级性能测试方案设计', 'https://jmeter.apache.org/usermanual/index.html', 'Jmeter官方文档实战进阶：场景设计（负载/压力/稳定性测试）、TPS/QPS指标分析、性能瓶颈定位（CPU/内存/IO/网络）、测试报告解读与调优建议', '进阶', 12.0
FROM skill s WHERE s.name = 'Jmeter';

-- 8. Git
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'BASIC', 'ARTICLE', 'Git 基础教程 - 廖雪峰', 'https://www.liaoxuefeng.com/wiki/896043488029600', 'Git最经典中文入门教程，从创建版本库、时光机穿梭、远程仓库到分支管理、标签管理，循序渐进掌握版本控制', '入门', 6.0
FROM skill s WHERE s.name = 'Git';
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'FRAMEWORK', 'EXERCISE', 'Git + GitHub 自动化测试脚本管理', 'https://github.com/skills/introduction-to-github', 'GitHub官方互动教程：学习将测试脚本/用例纳入Git版本管理，使用GitHub Actions配置自动化测试CI/CD流水线', '进阶', 3.0
FROM skill s WHERE s.name = 'Git';

-- 9. 自动化测试
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'BASIC', 'ARTICLE', '自动化测试体系全景图', 'https://martinfowler.com/articles/practical-test-pyramid.html', 'Martin Fowler经典文章：测试金字塔模型（单元→集成→端到端）、自动化测试策略、测试覆盖率、测试用例设计原则、CI/CD集成', '入门', 4.0
FROM skill s WHERE s.name = '自动化测试';
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'PROJECT', 'PROJECT', '全栈自动化测试平台搭建实战', 'https://github.com/appium/appium', '从零搭建多端自动化测试平台：Web端（Selenium）+ App端（Appium）+ API端（Requests+pytest），统一测试框架+报告+持续集成', '进阶', 20.0
FROM skill s WHERE s.name = '自动化测试';

-- 10. Linux
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'BASIC', 'ARTICLE', 'Linux 常用命令速查（测试环境必备）', 'https://github.com/jaywcjlove/linux-command', 'Linux命令速查开源项目：grep/awk/sed日志分析、ps/top性能监控、curl/wget接口调试、scp文件传输、crontab定时任务，测试环境运维必备', '入门', 6.0
FROM skill s WHERE s.name = 'Linux';
INSERT INTO learning_resource (skill_id, stage, type, title, url, description, difficulty, estimated_hours)
SELECT s.id, 'FRAMEWORK', 'EXERCISE', 'Shell 脚本编写实战：自动化部署脚本', 'https://github.com/dylanaraps/pure-bash-bible', 'Shell脚本进阶：编写自动化环境部署脚本（一键安装JDK/Python/Docker）、日志清理定时任务、服务器健康检查脚本、批量接口测试脚本', '进阶', 8.0
FROM skill s WHERE s.name = 'Linux';


-- ============================================================
-- 统计验证
-- ============================================================
SELECT 'P0学习资源初始化完成' AS message,
       COUNT(*) AS total_resources,
       COUNT(DISTINCT skill_id) AS covered_skills
FROM learning_resource
WHERE skill_id IN (
    SELECT id FROM skill WHERE name IN (
        -- Java后端
        'Java','Spring Boot','MySQL','Redis','Spring Cloud','MyBatis-Plus','Git','Linux','数据结构与算法','设计模式',
        -- Vue前端
        'JavaScript','Vue 3','TypeScript','Nuxt.js','RESTful API','UniApp','npm','React','WebSocket',
        -- 测试开发
        'Python','Selenium','接口测试','Postman','Jmeter','自动化测试'
    )
);
