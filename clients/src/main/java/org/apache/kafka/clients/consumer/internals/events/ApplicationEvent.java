/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.kafka.clients.consumer.internals.events;

import java.util.Objects;

/**
 * This is the abstract definition of the events created by the KafkaConsumer API
 */
public abstract class ApplicationEvent {

    public enum Type {
        COMMIT, POLL, FETCH_COMMITTED_OFFSET, METADATA_UPDATE, ASSIGNMENT_CHANGE,
        LIST_OFFSETS, RESET_POSITIONS, VALIDATE_POSITIONS, TOPIC_METADATA, SUBSCRIPTION_CHANGE,
        UNSUBSCRIBE
    }

    private final Type type;

    protected ApplicationEvent(Type type) {
        this.type = Objects.requireNonNull(type);
    }

    public Type type() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApplicationEvent that = (ApplicationEvent) o;

        return type == that.type;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    protected String toStringBase() {
        return "type=" + type;
    }

    @Override
    public String toString() {
        return "ApplicationEvent{" +
                toStringBase() +
                '}';
    }
}