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

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hertzbeat.common.entity.manager.Project;
import org.apache.hertzbeat.manager.dao.ProjectDao;
import org.apache.hertzbeat.manager.service.ProjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectDao projectDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addProject(Project project) {
        projectDao.save(project);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifyProject(Project project) {
        Optional<Project> existingProject = projectDao.findById(project.getId());
        if (existingProject.isPresent()) {
            projectDao.save(project);
        } else {
            throw new IllegalArgumentException("Project not found: " + project.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProject(long projectId) {
        projectDao.deleteById(projectId);
    }

    @Override
    public Project getProject(long projectId) {
        return projectDao.findById(projectId).orElse(null);
    }

    @Override
    public Page<Project> getProjects(String filter, PageRequest pageRequest) {
        Specification<Project> specification = (root, query, cb) -> {
            if (filter != null && !filter.isEmpty()) {
                return cb.or(
                    cb.like(root.get("name"), "%" + filter + "%"),
                    cb.like(root.get("description"), "%" + filter + "%")
                );
            }
            return null;
        };
        return projectDao.findAll(specification, pageRequest);
    }
}
