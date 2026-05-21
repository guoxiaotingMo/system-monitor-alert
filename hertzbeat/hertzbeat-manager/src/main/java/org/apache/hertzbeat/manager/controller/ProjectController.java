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
import jakarta.validation.Valid;
import org.apache.hertzbeat.common.entity.dto.Message;
import org.apache.hertzbeat.common.entity.manager.Project;
import org.apache.hertzbeat.manager.service.ProjectService;
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

@Tag(name = "Project Manage API")
@RestController
@RequestMapping(path = "/api/project", produces = {APPLICATION_JSON_VALUE})
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @PostMapping
    @Operation(summary = "Create a new project")
    public ResponseEntity<Message<Void>> addProject(@Valid @RequestBody Project project) {
        projectService.addProject(project);
        return ResponseEntity.ok(Message.success("Add success"));
    }

    @PutMapping
    @Operation(summary = "Modify an existing project")
    public ResponseEntity<Message<Void>> modifyProject(@Valid @RequestBody Project project) {
        projectService.modifyProject(project);
        return ResponseEntity.ok(Message.success("Modify success"));
    }

    @GetMapping(path = "/{id}")
    @Operation(summary = "Get project by ID")
    public ResponseEntity<Message<Project>> getProject(@Parameter(description = "Project ID") @PathVariable long id) {
        Project project = projectService.getProject(id);
        if (project == null) {
            return ResponseEntity.ok(Message.fail("Project not exist."));
        }
        return ResponseEntity.ok(Message.success(project));
    }

    @DeleteMapping(path = "/{id}")
    @Operation(summary = "Delete project by ID")
    public ResponseEntity<Message<Void>> deleteProject(@Parameter(description = "Project ID") @PathVariable long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(Message.success("Delete success"));
    }

    @GetMapping
    @Operation(summary = "Get projects with pagination")
    public ResponseEntity<Message<Page<Project>>> getProjects(
            @RequestParam(required = false) String filter,
            @RequestParam(defaultValue = "1") int pageIndex,
            @RequestParam(defaultValue = "20") int pageSize) {
        Page<Project> projects = projectService.getProjects(filter, PageRequest.of(pageIndex - 1, pageSize));
        return ResponseEntity.ok(Message.success(projects));
    }
}
