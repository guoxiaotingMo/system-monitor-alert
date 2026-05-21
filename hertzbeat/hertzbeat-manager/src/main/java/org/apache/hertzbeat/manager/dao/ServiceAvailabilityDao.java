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
package org.apache.hertzbeat.manager.dao;

import java.util.List;
import java.util.Optional;
import org.apache.hertzbeat.common.entity.manager.ServiceAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServiceAvailabilityDao extends JpaRepository<ServiceAvailability, Long>, JpaSpecificationExecutor<ServiceAvailability> {
    Optional<ServiceAvailability> findByMonitorId(Long monitorId);
    List<ServiceAvailability> findByProjectId(Long projectId);
    List<ServiceAvailability> findByAvailabilityStatus(byte status);

    @Modifying(clearAutomatically = true)
    @Query("update ServiceAvailability set slaPercentage = :percentage, totalChecks = :total, successfulChecks = :success where monitorId = :monitorId")
    void updateSlaByMonitorId(@Param("monitorId") Long monitorId, @Param("percentage") Double percentage, @Param("total") Long total, @Param("success") Long success);
}
