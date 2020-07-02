/*
 * Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
 *
 */
package net.northking.atp.db.service;

import net.northking.atp.db.persistent.ReExecPlanPluginSetting;
import net.northking.db.BasicService;

import java.util.List;

public interface ReExecPlanPluginSettingService extends BasicService<ReExecPlanPluginSetting> {
    // ----      The End by Generator     ----//

    void batchUpdateSetting(List<ReExecPlanPluginSetting> pluginSettingList);
}
