package com.github.wconfig.helper.component;

import com.github.wconfig.helper.localstorage.LocalStorage;
import com.github.wconfig.helper.service.ApplicationService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import java.util.concurrent.TimeUnit;

/**
 * ScheduleConfigPuller
 *
 * @author lupeng10
 * @create 2023-07-02 13:13
 */
public class ScheduleConfigPuller {
    private static final Logger log = Logger.getInstance(ScheduleConfigPuller.class);

    static {
        CommonThreadPool.schedulePool.scheduleWithFixedDelay(ScheduleConfigPuller::pull,5, 5, TimeUnit.MINUTES);
    }

    private static void pull() {
        log.info("[定时任务自动拉取wconfig==start]");
        ApplicationManager.getApplication().getService(ApplicationService.class).pullAllConfig(LocalStorage.getProject());
    }
}
