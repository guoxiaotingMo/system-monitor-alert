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

package org.apache.hertzbeat.log.loki;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LokiLogbackAppender extends AppenderBase<ILoggingEvent> {

    private String lokiUrl = "http://localhost:3100";
    private String projectName = "default";
    private String serviceName = "unknown";
    private String environment = "dev";
    private String username;
    private String password;
    private int batchSize = 100;
    private long flushIntervalMs = 5000;
    private int queueCapacity = 10000;

    private final BlockingQueue<ILoggingEvent> eventQueue;
    private final ScheduledExecutorService scheduler;
    private final ObjectMapper objectMapper;
    private final List<ILoggingEvent> batchBuffer;

    public LokiLogbackAppender() {
        this.eventQueue = new ArrayBlockingQueue<>(queueCapacity);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "loki-appender"); t.setDaemon(true); return t;
        });
        this.objectMapper = new ObjectMapper();
        this.batchBuffer = new ArrayList<>();
    }

    @Override
    public void start() { super.start(); scheduler.scheduleAtFixedRate(this::flush, flushIntervalMs, flushIntervalMs, TimeUnit.MILLISECONDS); }

    @Override
    public void stop() { flush(); scheduler.shutdown(); try { scheduler.awaitTermination(5, TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } super.stop(); }

    @Override
    protected void append(ILoggingEvent event) {
        if (!isStarted()) return;
        eventQueue.offer(event);
        if (eventQueue.size() >= batchSize) scheduler.execute(this::flush);
    }

    private void flush() {
        eventQueue.drainTo(batchBuffer, batchSize);
        if (batchBuffer.isEmpty()) return;
        try { sendToLoki(batchBuffer); } catch (Exception e) { addError("Failed to send logs to Loki", e); } finally { batchBuffer.clear(); }
    }

    private void sendToLoki(List<ILoggingEvent> events) throws IOException {
        Map<String, String> labels = new HashMap<>();
        labels.put("job", "hertzbeat-app"); labels.put("project", projectName);
        labels.put("service", serviceName); labels.put("environment", environment);
        List<List<Object>> values = new ArrayList<>();
        for (ILoggingEvent event : events) {
            List<Object> value = List.of(String.valueOf(event.getTimeStamp() * 1_000_000), formatLogLine(event));
            values.add(value);
        }
        Map<String, Object> stream = new HashMap<>(); stream.put("stream", labels); stream.put("values", values);
        Map<String, Object> payload = new HashMap<>(); payload.put("streams", List.of(stream));
        String json = objectMapper.writeValueAsString(payload);
        HttpURLConnection conn = (HttpURLConnection) URI.create(lokiUrl + "/loki/api/v1/push").toURL().openConnection();
        conn.setRequestMethod("POST"); conn.setRequestProperty("Content-Type", "application/json");
        if (username != null && password != null) {
            conn.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8)));
        }
        conn.setDoOutput(true); conn.setConnectTimeout(5000); conn.setReadTimeout(5000);
        try (var os = conn.getOutputStream()) { os.write(json.getBytes(StandardCharsets.UTF_8)); }
        int responseCode = conn.getResponseCode();
        if (responseCode != 204 && responseCode != 200) addError("Loki returned HTTP " + responseCode);
        conn.disconnect();
    }

    private String formatLogLine(ILoggingEvent event) {
        return String.format("[%s] %s %s - %s", event.getLevel(), Instant.ofEpochMilli(event.getTimeStamp()), event.getLoggerName(), event.getFormattedMessage());
    }

    public void setLokiUrl(String lokiUrl) { this.lokiUrl = lokiUrl; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
    public void setFlushIntervalMs(long flushIntervalMs) { this.flushIntervalMs = flushIntervalMs; }
    public void setQueueCapacity(int queueCapacity) { this.queueCapacity = queueCapacity; }
}
