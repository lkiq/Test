package com.xuelian.career.config;

import com.xuelian.career.service.MasteryMigrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 启动时数据迁移运行器 - 用配置开关控制
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MasteryMigrationRunner implements ApplicationRunner {

    private final MasteryMigrationService migrationService;

    @Value("${app.mastery.migration-enabled:false}")
    private boolean migrationEnabled;

    @Override
    public void run(ApplicationArguments args) {
        if (migrationEnabled) {
            log.info("========================================");
            log.info("开始迁移历史学习数据到 mastery 表...");
            migrationService.migrateCompletedTasks();
            log.info("历史数据迁移完成，请将 app.mastery.migration-enabled 改回 false");
            log.info("========================================");
        }
    }
}
