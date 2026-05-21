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
import jakarta.validation.Valid;
import org.apache.hertzbeat.alert.dao.AlertEscalationDao;
import org.apache.hertzbeat.common.entity.alerter.AlertEscalation;
import org.apache.hertzbeat.common.entity.dto.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "Alert Escalation API")
@RestController
@RequestMapping(path = "/api/alert/escalation", produces = {APPLICATION_JSON_VALUE})
public class AlertEscalationController {

    @Autowired
    private AlertEscalationDao alertEscalationDao;

    @PostMapping
    @Operation(summary = "Create escalation rule")
    public ResponseEntity<Message<Void>> addEscalation(@Valid @RequestBody AlertEscalation escalation) {
        alertEscalationDao.save(escalation);
        return ResponseEntity.ok(Message.success("Add success"));
    }

    @PutMapping
    @Operation(summary = "Modify escalation rule")
    public ResponseEntity<Message<Void>> modifyEscalation(@Valid @RequestBody AlertEscalation escalation) {
        alertEscalationDao.save(escalation);
        return ResponseEntity.ok(Message.success("Modify success"));
    }

    @DeleteMapping(path = "/{id}")
    @Operation(summary = "Delete escalation rule")
    public ResponseEntity<Message<Void>> deleteEscalation(@PathVariable Long id) {
        alertEscalationDao.deleteById(id);
        return ResponseEntity.ok(Message.success("Delete success"));
    }

    @GetMapping(path = "/{id}")
    @Operation(summary = "Get escalation rule by ID")
    public ResponseEntity<Message<AlertEscalation>> getEscalation(@PathVariable Long id) {
        return ResponseEntity.ok(Message.success(alertEscalationDao.findById(id).orElse(null)));
    }

    @GetMapping
    @Operation(summary = "Get escalation rules with pagination")
    public ResponseEntity<Message<Page<AlertEscalation>>> getEscalations(
            @RequestParam(defaultValue = "1") int pageIndex,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(Message.success(alertEscalationDao.findAll(PageRequest.of(pageIndex - 1, pageSize))));
    }
}
