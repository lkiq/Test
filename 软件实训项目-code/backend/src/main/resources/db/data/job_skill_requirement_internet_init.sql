-- ============================================================
-- AI智能求职辅导平台 - 岗位技能要求初始化脚本
-- 说明：通过岗位名称和技能名称关联ID
-- 使用 INSERT IGNORE 避免重复插入
-- ============================================================

USE career_platform;

-- ============================================================
-- 一、后端开发赛道
-- ============================================================

-- 1. Java后端开发工程师（10项核心技能）
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.15 FROM job_position j, skill s WHERE j.title = 'Java后端开发工程师' AND s.name = 'Java';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.15 FROM job_position j, skill s WHERE j.title = 'Java后端开发工程师' AND s.name = 'Spring Boot';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.12 FROM job_position j, skill s WHERE j.title = 'Java后端开发工程师' AND s.name = 'MySQL';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.1 FROM job_position j, skill s WHERE j.title = 'Java后端开发工程师' AND s.name = 'Redis';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.12 FROM job_position j, skill s WHERE j.title = 'Java后端开发工程师' AND s.name = 'Spring Cloud';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = 'Java后端开发工程师' AND s.name = 'MyBatis-Plus';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = 'Java后端开发工程师' AND s.name = 'Git';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = 'Java后端开发工程师' AND s.name = 'Linux';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '了解', 0.07 FROM job_position j, skill s WHERE j.title = 'Java后端开发工程师' AND s.name = '数据结构与算法';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '了解', 0.05 FROM job_position j, skill s WHERE j.title = 'Java后端开发工程师' AND s.name = '设计模式';

-- 2. 高级Java后端工程师（10项）
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '精通', 0.15 FROM job_position j, skill s WHERE j.title = '高级Java后端工程师' AND s.name = 'Java';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '精通', 0.12 FROM job_position j, skill s WHERE j.title = '高级Java后端工程师' AND s.name = 'Spring Boot';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.1 FROM job_position j, skill s WHERE j.title = '高级Java后端工程师' AND s.name = 'Spring Cloud';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.1 FROM job_position j, skill s WHERE j.title = '高级Java后端工程师' AND s.name = 'MySQL';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.08 FROM job_position j, skill s WHERE j.title = '高级Java后端工程师' AND s.name = 'Kafka';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.08 FROM job_position j, skill s WHERE j.title = '高级Java后端工程师' AND s.name = '微服务架构';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.08 FROM job_position j, skill s WHERE j.title = '高级Java后端工程师' AND s.name = '系统设计';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.07 FROM job_position j, skill s WHERE j.title = '高级Java后端工程师' AND s.name = 'Docker';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.07 FROM job_position j, skill s WHERE j.title = '高级Java后端工程师' AND s.name = 'Redis';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.05 FROM job_position j, skill s WHERE j.title = '高级Java后端工程师' AND s.name = 'Nacos';

-- 3. Go后端开发工程师（9项）
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.2 FROM job_position j, skill s WHERE j.title = 'Go后端开发工程师' AND s.name = 'Go';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.15 FROM job_position j, skill s WHERE j.title = 'Go后端开发工程师' AND s.name = 'Gin';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.12 FROM job_position j, skill s WHERE j.title = 'Go后端开发工程师' AND s.name = 'MySQL';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.1 FROM job_position j, skill s WHERE j.title = 'Go后端开发工程师' AND s.name = 'Redis';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.1 FROM job_position j, skill s WHERE j.title = 'Go后端开发工程师' AND s.name = 'Docker';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = 'Go后端开发工程师' AND s.name = 'Kubernetes';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = 'Go后端开发工程师' AND s.name = 'gRPC';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '了解', 0.07 FROM job_position j, skill s WHERE j.title = 'Go后端开发工程师' AND s.name = '数据结构与算法';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '了解', 0.1 FROM job_position j, skill s WHERE j.title = 'Go后端开发工程师' AND s.name = 'Linux';

-- 4. Python后端开发工程师（9项）
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.2 FROM job_position j, skill s WHERE j.title = 'Python后端开发工程师' AND s.name = 'Python';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.15 FROM job_position j, skill s WHERE j.title = 'Python后端开发工程师' AND s.name = 'Django';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.12 FROM job_position j, skill s WHERE j.title = 'Python后端开发工程师' AND s.name = 'FastAPI';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.1 FROM job_position j, skill s WHERE j.title = 'Python后端开发工程师' AND s.name = 'MySQL';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = 'Python后端开发工程师' AND s.name = 'Redis';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = 'Python后端开发工程师' AND s.name = 'MongoDB';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.07 FROM job_position j, skill s WHERE j.title = 'Python后端开发工程师' AND s.name = 'Git';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '了解', 0.1 FROM job_position j, skill s WHERE j.title = 'Python后端开发工程师' AND s.name = 'LLM';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '了解', 0.1 FROM job_position j, skill s WHERE j.title = 'Python后端开发工程师' AND s.name = 'RESTful API';

-- ============================================================
-- 二、前端开发赛道
-- ============================================================

-- 5. Vue前端开发工程师（10项）
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.15 FROM job_position j, skill s WHERE j.title = 'Vue前端开发工程师' AND s.name = 'JavaScript';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.15 FROM job_position j, skill s WHERE j.title = 'Vue前端开发工程师' AND s.name = 'Vue 3';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.12 FROM job_position j, skill s WHERE j.title = 'Vue前端开发工程师' AND s.name = 'TypeScript';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.1 FROM job_position j, skill s WHERE j.title = 'Vue前端开发工程师' AND s.name = 'Nuxt.js';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.1 FROM job_position j, skill s WHERE j.title = 'Vue前端开发工程师' AND s.name = 'Git';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = 'Vue前端开发工程师' AND s.name = '前端工程化';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = 'Vue前端开发工程师' AND s.name = 'RESTful API';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '了解', 0.07 FROM job_position j, skill s WHERE j.title = 'Vue前端开发工程师' AND s.name = 'Node.js';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '了解', 0.07 FROM job_position j, skill s WHERE j.title = 'Vue前端开发工程师' AND s.name = 'CSS3';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '了解', 0.08 FROM job_position j, skill s WHERE j.title = 'Vue前端开发工程师' AND s.name = 'UniApp';

-- 6. React前端开发工程师（10项）
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.15 FROM job_position j, skill s WHERE j.title = 'React前端开发工程师' AND s.name = 'JavaScript';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.15 FROM job_position j, skill s WHERE j.title = 'React前端开发工程师' AND s.name = 'React';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.12 FROM job_position j, skill s WHERE j.title = 'React前端开发工程师' AND s.name = 'TypeScript';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.1 FROM job_position j, skill s WHERE j.title = 'React前端开发工程师' AND s.name = 'Next.js';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.1 FROM job_position j, skill s WHERE j.title = 'React前端开发工程师' AND s.name = 'Git';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = 'React前端开发工程师' AND s.name = '前端工程化';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = 'React前端开发工程师' AND s.name = 'RESTful API';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '了解', 0.07 FROM job_position j, skill s WHERE j.title = 'React前端开发工程师' AND s.name = 'Node.js';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '了解', 0.07 FROM job_position j, skill s WHERE j.title = 'React前端开发工程师' AND s.name = 'CSS3';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '了解', 0.08 FROM job_position j, skill s WHERE j.title = 'React前端开发工程师' AND s.name = 'React Native';

-- 7. 高级前端工程师（9项）
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '精通', 0.15 FROM job_position j, skill s WHERE j.title = '高级前端工程师' AND s.name = 'JavaScript';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '精通', 0.12 FROM job_position j, skill s WHERE j.title = '高级前端工程师' AND s.name = 'Vue 3';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.12 FROM job_position j, skill s WHERE j.title = '高级前端工程师' AND s.name = 'TypeScript';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.1 FROM job_position j, skill s WHERE j.title = '高级前端工程师' AND s.name = 'React';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.1 FROM job_position j, skill s WHERE j.title = '高级前端工程师' AND s.name = '前端工程化';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.1 FROM job_position j, skill s WHERE j.title = '高级前端工程师' AND s.name = 'Node.js';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = '高级前端工程师' AND s.name = '微前端架构';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = '高级前端工程师' AND s.name = '性能优化';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '了解', 0.05 FROM job_position j, skill s WHERE j.title = '高级前端工程师' AND s.name = 'WebAssembly';

-- 8. 全栈开发工程师（9项）
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.12 FROM job_position j, skill s WHERE j.title = '全栈开发工程师' AND s.name = 'JavaScript';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.12 FROM job_position j, skill s WHERE j.title = '全栈开发工程师' AND s.name = 'React';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.1 FROM job_position j, skill s WHERE j.title = '全栈开发工程师' AND s.name = 'Node.js';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.12 FROM job_position j, skill s WHERE j.title = '全栈开发工程师' AND s.name = 'MySQL';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.1 FROM job_position j, skill s WHERE j.title = '全栈开发工程师' AND s.name = 'MongoDB';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = '全栈开发工程师' AND s.name = 'Next.js';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = '全栈开发工程师' AND s.name = 'Git';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = '全栈开发工程师' AND s.name = 'Docker';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '了解', 0.05 FROM job_position j, skill s WHERE j.title = '全栈开发工程师' AND s.name = 'Python';

-- ============================================================
-- 三、测试开发赛道
-- ============================================================

-- 9. 测试开发工程师（10项）
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.12 FROM job_position j, skill s WHERE j.title = '测试开发工程师' AND s.name = 'Python';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.12 FROM job_position j, skill s WHERE j.title = '测试开发工程师' AND s.name = 'Java';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.12 FROM job_position j, skill s WHERE j.title = '测试开发工程师' AND s.name = 'Selenium';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.1 FROM job_position j, skill s WHERE j.title = '测试开发工程师' AND s.name = '接口测试';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.1 FROM job_position j, skill s WHERE j.title = '测试开发工程师' AND s.name = 'Postman';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.1 FROM job_position j, skill s WHERE j.title = '测试开发工程师' AND s.name = 'MySQL';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = '测试开发工程师' AND s.name = 'Jmeter';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = '测试开发工程师' AND s.name = 'Git';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '了解', 0.1 FROM job_position j, skill s WHERE j.title = '测试开发工程师' AND s.name = '自动化测试';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '了解', 0.08 FROM job_position j, skill s WHERE j.title = '测试开发工程师' AND s.name = 'Linux';

-- 10. 自动化测试工程师（9项）
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.15 FROM job_position j, skill s WHERE j.title = '自动化测试工程师' AND s.name = 'Selenium';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.15 FROM job_position j, skill s WHERE j.title = '自动化测试工程师' AND s.name = 'Appium';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.12 FROM job_position j, skill s WHERE j.title = '自动化测试工程师' AND s.name = 'Python';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.1 FROM job_position j, skill s WHERE j.title = '自动化测试工程师' AND s.name = 'Pytest';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.1 FROM job_position j, skill s WHERE j.title = '自动化测试工程师' AND s.name = 'Playwright';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = '自动化测试工程师' AND s.name = '接口测试';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = '自动化测试工程师' AND s.name = 'Git';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '了解', 0.07 FROM job_position j, skill s WHERE j.title = '自动化测试工程师' AND s.name = 'CI/CD';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '了解', 0.05 FROM job_position j, skill s WHERE j.title = '自动化测试工程师' AND s.name = 'Cypress';

-- 11. 性能测试工程师（8项）
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.18 FROM job_position j, skill s WHERE j.title = '性能测试工程师' AND s.name = 'Jmeter';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.12 FROM job_position j, skill s WHERE j.title = '性能测试工程师' AND s.name = '性能测试';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.12 FROM job_position j, skill s WHERE j.title = '性能测试工程师' AND s.name = 'MySQL';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.1 FROM job_position j, skill s WHERE j.title = '性能测试工程师' AND s.name = 'Linux';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.1 FROM job_position j, skill s WHERE j.title = '性能测试工程师' AND s.name = 'Python';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '了解', 0.1 FROM job_position j, skill s WHERE j.title = '性能测试工程师' AND s.name = 'Prometheus';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '了解', 0.08 FROM job_position j, skill s WHERE j.title = '性能测试工程师' AND s.name = 'Grafana';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '了解', 0.1 FROM job_position j, skill s WHERE j.title = '性能测试工程师' AND s.name = 'Redis';

-- ============================================================
-- 四、运维/DevOps赛道
-- ============================================================

-- 12. 运维开发工程师（DevOps）（10项）
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.12 FROM job_position j, skill s WHERE j.title = '运维开发工程师（DevOps）' AND s.name = 'Python';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.12 FROM job_position j, skill s WHERE j.title = '运维开发工程师（DevOps）' AND s.name = 'Go';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.12 FROM job_position j, skill s WHERE j.title = '运维开发工程师（DevOps）' AND s.name = 'Docker';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.12 FROM job_position j, skill s WHERE j.title = '运维开发工程师（DevOps）' AND s.name = 'Kubernetes';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.1 FROM job_position j, skill s WHERE j.title = '运维开发工程师（DevOps）' AND s.name = 'Jenkins';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.1 FROM job_position j, skill s WHERE j.title = '运维开发工程师（DevOps）' AND s.name = 'Linux';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = '运维开发工程师（DevOps）' AND s.name = 'Prometheus';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = '运维开发工程师（DevOps）' AND s.name = 'GitLab CI';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = '运维开发工程师（DevOps）' AND s.name = 'Shell';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '了解', 0.08 FROM job_position j, skill s WHERE j.title = '运维开发工程师（DevOps）' AND s.name = 'Terraform';

-- 13. SRE工程师（9项）
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.12 FROM job_position j, skill s WHERE j.title = 'SRE工程师' AND s.name = 'Linux';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.12 FROM job_position j, skill s WHERE j.title = 'SRE工程师' AND s.name = 'Docker';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.12 FROM job_position j, skill s WHERE j.title = 'SRE工程师' AND s.name = 'Kubernetes';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.1 FROM job_position j, skill s WHERE j.title = 'SRE工程师' AND s.name = 'Prometheus';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.1 FROM job_position j, skill s WHERE j.title = 'SRE工程师' AND s.name = 'Grafana';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = 'SRE工程师' AND s.name = 'Python';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = 'SRE工程师' AND s.name = '计算机网络';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = 'SRE工程师' AND s.name = 'ELK Stack';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '了解', 0.1 FROM job_position j, skill s WHERE j.title = 'SRE工程师' AND s.name = 'Istio';

-- 14. K8s运维工程师（8项）
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.18 FROM job_position j, skill s WHERE j.title = 'K8s运维工程师' AND s.name = 'Kubernetes';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.15 FROM job_position j, skill s WHERE j.title = 'K8s运维工程师' AND s.name = 'Docker';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.12 FROM job_position j, skill s WHERE j.title = 'K8s运维工程师' AND s.name = 'Helm';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.1 FROM job_position j, skill s WHERE j.title = 'K8s运维工程师' AND s.name = 'Istio';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.1 FROM job_position j, skill s WHERE j.title = 'K8s运维工程师' AND s.name = 'Go';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.1 FROM job_position j, skill s WHERE j.title = 'K8s运维工程师' AND s.name = 'Linux';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '了解', 0.08 FROM job_position j, skill s WHERE j.title = 'K8s运维工程师' AND s.name = 'ArgoCD';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '了解', 0.07 FROM job_position j, skill s WHERE j.title = 'K8s运维工程师' AND s.name = 'Prometheus';

-- ============================================================
-- 五、算法/AI赛道
-- ============================================================

-- 15. 大模型算法工程师（10项）
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.12 FROM job_position j, skill s WHERE j.title = '大模型算法工程师' AND s.name = 'Python';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.12 FROM job_position j, skill s WHERE j.title = '大模型算法工程师' AND s.name = 'PyTorch';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.12 FROM job_position j, skill s WHERE j.title = '大模型算法工程师' AND s.name = 'LLM';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.1 FROM job_position j, skill s WHERE j.title = '大模型算法工程师' AND s.name = 'RAG';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.1 FROM job_position j, skill s WHERE j.title = '大模型算法工程师' AND s.name = 'LangChain';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = '大模型算法工程师' AND s.name = '向量数据库';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = '大模型算法工程师' AND s.name = 'Numpy';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = '大模型算法工程师' AND s.name = 'Pandas';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '了解', 0.1 FROM job_position j, skill s WHERE j.title = '大模型算法工程师' AND s.name = 'Agent';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '了解', 0.1 FROM job_position j, skill s WHERE j.title = '大模型算法工程师' AND s.name = 'Prompt Engineering';

-- 16. 推荐算法工程师（9项）
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.12 FROM job_position j, skill s WHERE j.title = '推荐算法工程师' AND s.name = 'Python';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.12 FROM job_position j, skill s WHERE j.title = '推荐算法工程师' AND s.name = 'TensorFlow';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.12 FROM job_position j, skill s WHERE j.title = '推荐算法工程师' AND s.name = 'PyTorch';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.1 FROM job_position j, skill s WHERE j.title = '推荐算法工程师' AND s.name = 'Spark';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.1 FROM job_position j, skill s WHERE j.title = '推荐算法工程师' AND s.name = 'Scikit-learn';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.1 FROM job_position j, skill s WHERE j.title = '推荐算法工程师' AND s.name = '数据挖掘';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = '推荐算法工程师' AND s.name = 'MySQL';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = '推荐算法工程师' AND s.name = 'Redis';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '了解', 0.08 FROM job_position j, skill s WHERE j.title = '推荐算法工程师' AND s.name = 'Hadoop';

-- 17. NLP算法工程师（9项）
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.15 FROM job_position j, skill s WHERE j.title = 'NLP算法工程师' AND s.name = 'Python';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.12 FROM job_position j, skill s WHERE j.title = 'NLP算法工程师' AND s.name = 'PyTorch';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.12 FROM job_position j, skill s WHERE j.title = 'NLP算法工程师' AND s.name = 'TensorFlow';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.1 FROM job_position j, skill s WHERE j.title = 'NLP算法工程师' AND s.name = 'LLM';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.1 FROM job_position j, skill s WHERE j.title = 'NLP算法工程师' AND s.name = 'Scikit-learn';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = 'NLP算法工程师' AND s.name = 'Numpy';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = 'NLP算法工程师' AND s.name = 'Pandas';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '了解', 0.1 FROM job_position j, skill s WHERE j.title = 'NLP算法工程师' AND s.name = 'LangChain';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '了解', 0.05 FROM job_position j, skill s WHERE j.title = 'NLP算法工程师' AND s.name = 'Java';

-- 18. 数据分析工程师（9项）
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.15 FROM job_position j, skill s WHERE j.title = '数据分析工程师' AND s.name = 'Python';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.12 FROM job_position j, skill s WHERE j.title = '数据分析工程师' AND s.name = 'SQL';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '熟练', 0.12 FROM job_position j, skill s WHERE j.title = '数据分析工程师' AND s.name = 'Pandas';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.1 FROM job_position j, skill s WHERE j.title = '数据分析工程师' AND s.name = 'Numpy';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.1 FROM job_position j, skill s WHERE j.title = '数据分析工程师' AND s.name = 'Matplotlib';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = '数据分析工程师' AND s.name = 'Scikit-learn';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '掌握', 0.08 FROM job_position j, skill s WHERE j.title = '数据分析工程师' AND s.name = 'MySQL';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '了解', 0.1 FROM job_position j, skill s WHERE j.title = '数据分析工程师' AND s.name = 'Hive';
INSERT IGNORE INTO job_skill_requirement (job_id, skill_id, required_level, weight)
SELECT j.id, s.id, '了解', 0.05 FROM job_position j, skill s WHERE j.title = '数据分析工程师' AND s.name = 'Excel';

-- ============================================================
-- 统计验证
-- ============================================================
SELECT '岗位技能要求数据初始化完成，当前关联总数：' AS message, COUNT(*) AS total FROM job_skill_requirement;

SELECT j.title AS 岗位名称, COUNT(jsr.skill_id) AS 技能数
FROM job_position j
LEFT JOIN job_skill_requirement jsr ON j.id = jsr.job_id
WHERE j.is_deleted = 0
GROUP BY j.id, j.title
ORDER BY 技能数 DESC;
