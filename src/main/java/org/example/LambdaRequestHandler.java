/*
 * (C) Copyright 2023 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Srimanta Singh
 */
package org.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.StopInstancesRequest;
import software.amazon.awssdk.services.ec2.waiters.Ec2Waiter;

public class LambdaRequestHandler
    implements RequestHandler<String, String> {
    public String handleRequest(String input, Context context) {
        context.getLogger().log("Input: " + input);
        Region region = Region.US_EAST_1;
        Ec2Client ec2 = Ec2Client.builder()
                                 .region(region)
//                                 .credentialsProvider(ProfileCredentialsProvider.create())
                                 .build();
        for ( String instanceId : input.split(",")) {
            stopInstance(ec2, instanceId, context);
        }
        return "Hello World - " + input;
    }

    public static void stopInstance(Ec2Client ec2, String instanceId, Context context) {
        Ec2Waiter ec2Waiter = Ec2Waiter.builder()
                                       .overrideConfiguration(b -> b.maxAttempts(100))
                                       .client(ec2)
                                       .build();
        StopInstancesRequest request = StopInstancesRequest.builder()
                                                           .instanceIds(instanceId)
                                                           .build();

        context.getLogger().log("Use an Ec2Waiter to wait for the instance to stop. This will take a few minutes.");
        ec2.stopInstances(request);
        DescribeInstancesRequest instanceRequest = DescribeInstancesRequest.builder()
                                                                           .instanceIds(instanceId)
                                                                           .build();

        WaiterResponse<DescribeInstancesResponse> waiterResponse = ec2Waiter.waitUntilInstanceStopped(instanceRequest);
        waiterResponse.matched().response().ifPresent(System.out::println);
        context.getLogger().log("Successfully stopped instance "+instanceId);
    }
}
