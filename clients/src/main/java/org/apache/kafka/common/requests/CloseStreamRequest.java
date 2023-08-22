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

package org.apache.kafka.common.requests;

import org.apache.kafka.common.message.CloseStreamRequestData;
import org.apache.kafka.common.message.CloseStreamResponseData;
import org.apache.kafka.common.protocol.ApiKeys;

public class CloseStreamRequest extends AbstractRequest {

    public static class Builder extends AbstractRequest.Builder<CloseStreamRequest> {

        private final CloseStreamRequestData data;
        public Builder(CloseStreamRequestData data) {
            super(ApiKeys.CLOSE_STREAM);
            this.data = data;
        }

        @Override
        public CloseStreamRequest build(short version) {
            return new CloseStreamRequest(data, version);
        }

        @Override
        public String toString() {
            return data.toString();
        }
    }
    private final CloseStreamRequestData data;

    public CloseStreamRequest(CloseStreamRequestData data, short version) {
        super(ApiKeys.CLOSE_STREAM, version);
        this.data = data;
    }

    @Override
    public CloseStreamResponse getErrorResponse(int throttleTimeMs, Throwable e) {
        ApiError apiError = ApiError.fromThrowable(e);
        CloseStreamResponseData response = new CloseStreamResponseData()
            .setErrorCode(apiError.error().code())
            .setThrottleTimeMs(throttleTimeMs);
        return new CloseStreamResponse(response);
    }

    @Override
    public CloseStreamRequestData data() {
        return data;
    }
}
