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
package org.apache.hertzbeat.manager.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hertzbeat.common.entity.manager.ServiceAvailability;
import org.apache.hertzbeat.manager.dao.ServiceAvailabilityDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceAvailabilityServiceImpl {

    private final ServiceAvailabilityDao serviceAvailabilityDao;

    @Transactional(rollbackFor = Exception.class)
    public void recordCheckResult(Long monitorId, Long projectId, boolean success) {
        Optional<ServiceAvailability> opt = serviceAvailabilityDao.findByMonitorId(monitorId);
        ServiceAvailability sa;
        if (opt.isPresent()) {
            sa = opt.get();
        } else {
            sa = ServiceAvailability.builder()
                .monitorId(monitorId).projectId(projectId)
                .consecutiveFailures(0).failureThreshold(3)
                .availabilityStatus((byte) 0)
                .totalChecks(0L).successfulChecks(0L)
                .slaPercentage(100.0)
                .slaPeriodStart(LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0))
                .build();
        }
        sa.setTotalChecks(sa.getTotalChecks() + 1);
        sa.setLastCheckTime(LocalDateTime.now());
        if (success) {
            sa.setSuccessfulChecks(sa.getSuccessfulChecks() + 1);
            sa.setConsecutiveFailures(0);
            if (sa.getAvailabilityStatus() != 0) {
                sa.setAvailabilityStatus((byte) 0);
                sa.setLastStatusChangeTime(LocalDateTime.now());
            }
        } else {
            sa.setConsecutiveFailures(sa.getConsecutiveFailures() + 1);
            if (sa.getConsecutiveFailures() >= sa.getFailureThreshold()) {
                byte newStatus = sa.getConsecutiveFailures() >= sa.getFailureThreshold() * 2 ? (byte) 2 : (byte) 1;
                if (sa.getAvailabilityStatus() != newStatus) {
                    sa.setAvailabilityStatus(newStatus);
                    sa.setLastStatusChangeTime(LocalDateTime.now());
                }
            }
        }
        if (sa.getTotalChecks() > 0) {
            sa.setSlaPercentage(Math.round((double) sa.getSuccessfulChecks() / sa.getTotalChecks() * 10000.0) / 100.0);
        }
        serviceAvailabilityDao.save(sa);
    }

    public ServiceAvailability getByMonitorId(Long monitorId) {
        return serviceAvailabilityDao.findByMonitorId(monitorId).orElse(null);
    }

    public List<ServiceAvailability> getByProjectId(Long projectId) {
        return serviceAvailabilityDao.findByProjectId(projectId);
    }

    public List<ServiceAvailability> getUnavailableServices() {
        return serviceAvailabilityDao.findByAvailabilityStatus((byte) 2);
    }
}
