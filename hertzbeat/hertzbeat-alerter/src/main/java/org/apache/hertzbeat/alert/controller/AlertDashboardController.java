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

package org.apache.hertzbeat.alert.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.hertzbeat.alert.dao.AlertDao;
import org.apache.hertzbeat.alert.dto.AlertPriorityNum;
import org.apache.hertzbeat.common.entity.alerter.Alert;
import org.apache.hertzbeat.common.entity.dto.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "Alert Dashboard API")
@RestController
@RequestMapping(path = "/api/alert/dashboard", produces = {APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
public class AlertDashboardController {

    private final AlertDao alertDao;

    @GetMapping("/stats")
    @Operation(summary = "Get alert statistics by priority level")
    public ResponseEntity<Message<Map<String, Object>>> getAlertStats() {
        Map<String, Object> stats = new HashMap<>();
        Page<Alert> recentAlerts = alertDao.findAll(PageRequest.of(0, 100));
        stats.put("totalAlerts", recentAlerts.getTotalElements());
        stats.put("emergencyCount", recentAlerts.getContent().stream().filter(a -> a.getPriority() == 1).count());
        stats.put("criticalCount", recentAlerts.getContent().stream().filter(a -> a.getPriority() == 2).count());
        stats.put("warningCount", recentAlerts.getContent().stream().filter(a -> a.getPriority() == 3).count());
        stats.put("infoCount", recentAlerts.getContent().stream().filter(a -> a.getPriority() == 4).count());
        return ResponseEntity.ok(Message.success(stats));
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent alerts with pagination")
    public ResponseEntity<Message<Page<Alert>>> getRecentAlerts(
            @RequestParam(defaultValue = "1") int pageIndex,
            @RequestParam(defaultValue = "20") int pageSize) {
        Page<Alert> alerts = alertDao.findAll(PageRequest.of(pageIndex - 1, pageSize));
        return ResponseEntity.ok(Message.success(alerts));
    }
}
