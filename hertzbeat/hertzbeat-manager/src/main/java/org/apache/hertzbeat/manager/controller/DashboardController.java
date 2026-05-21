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

package org.apache.hertzbeat.manager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.hertzbeat.common.entity.dto.Message;
import org.apache.hertzbeat.common.entity.manager.Monitor;
import org.apache.hertzbeat.common.entity.manager.Project;
import org.apache.hertzbeat.common.entity.manager.ServiceAvailability;
import org.apache.hertzbeat.manager.dao.MonitorDao;
import org.apache.hertzbeat.manager.dao.ProjectDao;
import org.apache.hertzbeat.manager.dao.ServiceAvailabilityDao;
import org.apache.hertzbeat.manager.pojo.dto.AppCount;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "Operations Dashboard API")
@RestController
@RequestMapping(path = "/api/dashboard", produces = {APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
public class DashboardController {

    private final ProjectDao projectDao;
    private final MonitorDao monitorDao;
    private final ServiceAvailabilityDao serviceAvailabilityDao;

    @GetMapping("/overview")
    @Operation(summary = "Get operations overview")
    public ResponseEntity<Message<Map<String, Object>>> getOverview() {
        Map<String, Object> overview = new HashMap<>();
        List<Project> projects = projectDao.findAll();
        overview.put("totalProjects", projects.size());
        overview.put("enabledProjects", projects.stream().filter(p -> p.getStatus() == 1).count());
        List<Monitor> monitors = monitorDao.findAll();
        overview.put("totalMonitors", monitors.size());
        overview.put("upMonitors", monitors.stream().filter(m -> m.getStatus() == 1).count());
        overview.put("downMonitors", monitors.stream().filter(m -> m.getStatus() == 2).count());
        overview.put("pausedMonitors", monitors.stream().filter(m -> m.getStatus() == 0).count());
        List<AppCount> appCounts = monitorDao.findAppsStatusCount();
        overview.put("appStatusCounts", appCounts);
        List<ServiceAvailability> unavailableServices = serviceAvailabilityDao.findByAvailabilityStatus((byte) 2);
        List<ServiceAvailability> degradedServices = serviceAvailabilityDao.findByAvailabilityStatus((byte) 1);
        overview.put("unavailableServiceCount", unavailableServices.size());
        overview.put("degradedServiceCount", degradedServices.size());
        return ResponseEntity.ok(Message.success(overview));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get project dashboard data")
    public ResponseEntity<Message<Map<String, Object>>> getProjectDashboard(@Parameter(description = "Project ID") @PathVariable Long projectId) {
        Map<String, Object> dashboard = new HashMap<>();
        Project project = projectDao.findById(projectId).orElse(null);
        if (project == null) return ResponseEntity.ok(Message.fail("Project not found"));
        dashboard.put("project", project);
        List<ServiceAvailability> services = serviceAvailabilityDao.findByProjectId(projectId);
        dashboard.put("totalServices", services.size());
        dashboard.put("availableServices", services.stream().filter(s -> s.getAvailabilityStatus() == 0).count());
        dashboard.put("degradedServices", services.stream().filter(s -> s.getAvailabilityStatus() == 1).count());
        dashboard.put("unavailableServices", services.stream().filter(s -> s.getAvailabilityStatus() == 2).count());
        List<Map<String, Object>> serviceList = new ArrayList<>();
        for (ServiceAvailability sa : services) {
            Map<String, Object> info = new HashMap<>();
            info.put("monitorId", sa.getMonitorId());
            info.put("availabilityStatus", sa.getAvailabilityStatus());
            info.put("slaPercentage", sa.getSlaPercentage());
            info.put("consecutiveFailures", sa.getConsecutiveFailures());
            info.put("lastCheckTime", sa.getLastCheckTime());
            monitorDao.findById(sa.getMonitorId()).ifPresent(m -> {
                info.put("name", m.getName()); info.put("app", m.getApp()); info.put("instance", m.getInstance());
            });
            serviceList.add(info);
        }
        dashboard.put("services", serviceList);
        return ResponseEntity.ok(Message.success(dashboard));
    }

    @GetMapping("/sla")
    @Operation(summary = "Get SLA overview for all services")
    public ResponseEntity<Message<List<ServiceAvailability>>> getSlaOverview() {
        return ResponseEntity.ok(Message.success(serviceAvailabilityDao.findAll()));
    }
}
