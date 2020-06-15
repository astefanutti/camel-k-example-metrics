/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.redhat.integration;

import java.util.Random;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.RuntimeExchangeException;

import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.Meter;

import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.Metric;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

@Named("service")
@ApplicationScoped
@io.quarkus.arc.Unremovable
public class Service implements Processor {

    @Override
    @Metered(name = "attempt", absolute = true)
    public void process(Exchange exchange) {
        Random rand = new Random();
        if (rand.nextDouble() < 0.5) {
            throw new RuntimeExchangeException("Random failure", exchange);
        }
    }

    @Produces
    @ApplicationScoped
    @Metric(name = "success-ratio", absolute = true)
    // Register a custom gauge that's the ratio of the 'success' meter on the 'generated' meter
    Gauge<Double> successRatio(@Metric(name = "success", absolute = true) Meter success, @Metric(name = "generated", absolute = true) Meter generated) {
        return () -> success.getOneMinuteRate() / generated.getOneMinuteRate();
    }
}
