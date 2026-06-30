package com.xuelian.career.service;

import com.xuelian.career.service.impl.CareerExplorationServiceImpl;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 意图分类器参数化回归测试。
 * 目标：确保未来的改动不会让分类器再次变笨。
 *
 * 说明：
 * - 项目当前意图值只有 RECOMMENDATION 与 GENERAL 两个（定义于 CareerExplorationServiceImpl 内部常量）。
 * - 按现有 Prompt 规则，与职业完全无关的纯闲聊（日期/天气/寒暄）归入 GENERAL，不存在 OTHER。
 * - classifyIntent 为 private 方法，这里通过反射调用，避免为测试而改动生产代码可见性。
 * - 本测试为集成测试，会真实调用 DeepSeek API；API 不可用时自动跳过，不报失败。
 */
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IntentClassifierTest {

    /** 与生产代码保持一致的意图常量（镜像，避免反射读 private static 字段） */
    private static final String INTENT_RECOMMENDATION = "RECOMMENDATION";
    private static final String INTENT_GENERAL = "GENERAL";

    @Autowired
    private CareerExplorationServiceImpl explorationService;

    private Method classifyIntentMethod;
    private DeepSeekService deepSeekService;

    @BeforeEach
    void setUp() throws Exception {
        classifyIntentMethod = CareerExplorationServiceImpl.class
                .getDeclaredMethod("classifyIntent", String.class);
        classifyIntentMethod.setAccessible(true);

        // 反射读取注入的 deepSeekService 字段，用于可用性前置判断
        Field dsField = CareerExplorationServiceImpl.class.getDeclaredField("deepSeekService");
        dsField.setAccessible(true);
        deepSeekService = (DeepSeekService) dsField.get(explorationService);
    }

    /**
     * 20 个极具迷惑性的测试用例，覆盖三大类：
     * A. 伪装成闲聊的职业问题（应为 GENERAL 或 RECOMMENDATION）
     * B. 含“岗位”字眼但实为技术探讨的问题（应为 GENERAL）
     * C. 纯粹闲聊（应为 GENERAL）
     *
     * 每条用例：问题文本 / 期望意图 / 用例描述
     */
    static Stream<Arguments> trickyCases() {
        return Stream.of(
                // ===== A. 伪装成闲聊的职业问题 =====
                Arguments.of("嗨，能聊聊后端开发那些事儿吗", INTENT_GENERAL,
                        "A1 伪装闲聊-问后端开发知识"),
                Arguments.of("随便问问，Java 工程师平时都干啥", INTENT_GENERAL,
                        "A2 伪装闲聊-问岗位日常"),
                Arguments.of("好奇问下，做前端有前途吗", INTENT_GENERAL,
                        "A3 伪装闲聊-问行业看法"),
                Arguments.of("闲聊一下，你觉得我搞数据分析怎么样", INTENT_RECOMMENDATION,
                        "A4 伪装闲聊-个人适配判断"),
                Arguments.of("打发时间，聊聊我适合产品还是技术", INTENT_RECOMMENDATION,
                        "A5 伪装闲聊-个人方向选择"),
                Arguments.of("无聊问问，帮我看看我做运维行不行", INTENT_RECOMMENDATION,
                        "A6 伪装闲聊-个人推荐请求"),
                Arguments.of("顺嘴问一句，我是学计算机的能做什么", INTENT_RECOMMENDATION,
                        "A7 伪装闲聊-基于背景的个人推荐"),

                // ===== B. 含“岗位”字眼但实为技术探讨（应为 GENERAL） =====
                Arguments.of("Java 后端岗位需要掌握哪些技术栈", INTENT_GENERAL,
                        "B1 含岗位-问技术栈"),
                Arguments.of("前端岗位的 React 和 Vue 哪个更值得学", INTENT_GENERAL,
                        "B2 含岗位-问技术选型"),
                Arguments.of("算法岗位平时用 Python 多还是 C++ 多", INTENT_GENERAL,
                        "B3 含岗位-问技术现状"),
                Arguments.of("测试岗位的自动化测试框架怎么选", INTENT_GENERAL,
                        "B4 含岗位-问框架选型"),
                Arguments.of("DevOps 岗位的 CI/CD 一般用什么工具", INTENT_GENERAL,
                        "B5 含岗位-问工具链"),
                Arguments.of("后端岗位的微服务架构怎么拆分合理", INTENT_GENERAL,
                        "B6 含岗位-问架构设计"),

                // ===== C. 纯粹闲聊（应为 GENERAL） =====
                Arguments.of("你吃了吗", INTENT_GENERAL,
                        "C1 纯闲聊-寒暄"),
                Arguments.of("今天几号", INTENT_GENERAL,
                        "C2 纯闲聊-日期"),
                Arguments.of("明天天气怎么样", INTENT_GENERAL,
                        "C3 纯闲聊-天气"),
                Arguments.of("讲个笑话听听", INTENT_GENERAL,
                        "C4 纯闲聊-娱乐"),
                Arguments.of("你会唱歌吗", INTENT_GENERAL,
                        "C5 纯闲聊-问能力"),

                // ===== 额外高迷惑性边界用例 =====
                Arguments.of("计算机专业就业前景如何", INTENT_GENERAL,
                        "D1 问行业前景-非个人推荐"),
                Arguments.of("我应该选前端还是后端", INTENT_RECOMMENDATION,
                        "D2 个人方向选择-明确我该选")
        );
    }

    @ParameterizedTest(name = "[{index}] {2}: \"{0}\" -> 期望 {1}")
    @MethodSource("trickyCases")
    @DisplayName("意图分类器迷惑用例验证")
    void classifyIntent_shouldReturnExpected(String question, String expected, String desc) throws Exception {
        // DeepSeek 不可用时跳过，避免环境问题误报为分类器回归
        Assumptions.assumeTrue(deepSeekService != null && deepSeekService.isAvailable(),
                "DeepSeek API 不可用，跳过意图分类测试");

        String actual = (String) classifyIntentMethod.invoke(explorationService, question);

        assertEquals(expected, actual,
                String.format("用例[%s]分类不符：问题=\"%s\"，期望=%s，实际=%s",
                        desc, question, expected, actual));
    }
}
