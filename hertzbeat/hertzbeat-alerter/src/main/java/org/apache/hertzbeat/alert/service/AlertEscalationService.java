/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hertzbeat.alert.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hertzbeat.alert.dao.AlertEscalationDao;
import org.apache.hertzbeat.common.cache.CacheFactory;
import org.apache.hertzbeat.common.entity.alerter.AlertEscalation;
import org.apache.hertzbeat.common.entity.alerter.GroupAlert;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertEscalationService {

    private final AlertEscalationDao alertEscalationDao;

    public List<AlertEscalation> getActiveEscalationRules() {
        List<AlertEscalation> rules = CacheFactory.getAlertEscalationCache();
        if (rules == null) {
            rules = alertEscalationDao.findByEnableTrue();
            CacheFactory.setAlertEscalationCache(rules);
        }
        return rules;
    }

    public void refreshCache() {
        List<AlertEscalation> rules = alertEscalationDao.findByEnableTrue();
        CacheFactory.setAlertEscalationCache(rules);
    }

    public boolean shouldEscalate(GroupAlert alert, AlertEscalation rule) {
        if (alert.getPriority() != rule.getFromPriority()) return false;
        if (rule.getProjectId() != null && alert.getLabels() != null) {
            String alertProject = alert.getLabels().get("project");
            if (alertProject != null && !String.valueOf(rule.getProjectId()).equals(alertProject)) return false;
        }
        if (rule.getLabels() != null && !rule.getLabels().isEmpty() && alert.getLabels() != null) {
            for (Map.Entry<String, String> entry : rule.getLabels().entrySet()) {
                String alertValue = alert.getLabels().get(entry.getKey());
                if (alertValue == null || !alertValue.equals(entry.getValue())) return false;
            }
        }
        if (alert.getFirstTriggerTime() != null) {
            Duration elapsed = Duration.between(alert.getFirstTriggerTime(), LocalDateTime.now());
            return elapsed.getSeconds() >= rule.getTimeoutSeconds();
        }
        return false;
    }

    @Scheduled(fixedDelay = 60000)
    public void checkEscalations() {
        log.debug("Checking alert escalation rules");
        refreshCache();
    }
}
