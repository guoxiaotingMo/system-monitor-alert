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

package org.apache.hertzbeat.log.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.apache.hertzbeat.common.entity.dto.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "Log Query API")
@RestController
@RequestMapping(path = "/api/log", produces = {APPLICATION_JSON_VALUE})
public class LogQueryController {

    @Value("${hertzbeat.loki.url:http://localhost:3100}")
    private String lokiUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/query")
    @Operation(summary = "Query logs from Grafana Loki using LogQL")
    public ResponseEntity<Message<JsonNode>> queryLogs(
            @Parameter(description = "LogQL query expression") @RequestParam String query,
            @Parameter(description = "Start time epoch nanoseconds") @RequestParam(required = false) Long start,
            @Parameter(description = "End time epoch nanoseconds") @RequestParam(required = false) Long end,
            @Parameter(description = "Max log lines to return") @RequestParam(defaultValue = "100") int limit,
            @Parameter(description = "Query direction: forward or backward") @RequestParam(defaultValue = "backward") String direction,
            @Parameter(description = "Time range in seconds") @RequestParam(defaultValue = "3600") long range) throws IOException {
        long endTime = end != null ? end : Instant.now().toEpochMilli() * 1_000_000;
        long startTime = start != null ? start : (Instant.now().minusSeconds(range).toEpochMilli() * 1_000_000);
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = String.format("%s/loki/api/v1/query_range?query=%s&start=%d&end=%d&limit=%d&direction=%s",
            lokiUrl, encodedQuery, startTime, endTime, limit, direction);
        HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
        conn.setRequestMethod("GET"); conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(5000); conn.setReadTimeout(30000);
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) return ResponseEntity.ok(Message.fail("Loki query failed with HTTP " + responseCode));
        JsonNode result = objectMapper.readTree(conn.getInputStream()); conn.disconnect();
        return ResponseEntity.ok(Message.success(result));
    }

    @GetMapping("/labels")
    @Operation(summary = "Get available log labels from Loki")
    public ResponseEntity<Message<JsonNode>> getLabels(
            @Parameter(description = "Start time") @RequestParam(required = false) Long start,
            @Parameter(description = "End time") @RequestParam(required = false) Long end) throws IOException {
        long endTime = end != null ? end : Instant.now().toEpochMilli() * 1_000_000;
        long startTime = start != null ? start : (Instant.now().minusSeconds(3600).toEpochMilli() * 1_000_000);
        String url = String.format("%s/loki/api/v1/labels?start=%d&end=%d", lokiUrl, startTime, endTime);
        HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
        conn.setRequestMethod("GET"); conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(5000); conn.setReadTimeout(10000);
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) return ResponseEntity.ok(Message.fail("Loki labels query failed with HTTP " + responseCode));
        JsonNode result = objectMapper.readTree(conn.getInputStream()); conn.disconnect();
        return ResponseEntity.ok(Message.success(result));
    }
}
