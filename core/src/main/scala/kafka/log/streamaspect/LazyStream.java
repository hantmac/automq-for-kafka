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

package kafka.log.streamaspect;

import com.automq.stream.api.AppendResult;
import com.automq.stream.api.CreateStreamOptions;
import com.automq.stream.api.FetchResult;
import com.automq.stream.api.OpenStreamOptions;
import com.automq.stream.api.ReadOptions;
import com.automq.stream.api.RecordBatch;
import com.automq.stream.api.Stream;
import com.automq.stream.api.StreamClient;
import com.automq.stream.utils.FutureUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Lazy stream, create stream when append record.
 */
public class LazyStream implements Stream {
    private static final Logger LOGGER = LoggerFactory.getLogger(LazyStream.class);
    public static final long NOOP_STREAM_ID = -1L;
    private static final Stream NOOP_STREAM = new NoopStream();
    private final String name;
    private final StreamClient client;
    private final int replicaCount;
    private final long epoch;
    private volatile Stream inner = NOOP_STREAM;
    private ElasticStreamEventListener eventListener;

    public LazyStream(String name, long streamId, StreamClient client, int replicaCount, long epoch) throws IOException {
        this.name = name;
        this.client = client;
        this.replicaCount = replicaCount;
        this.epoch = epoch;
        if (streamId != NOOP_STREAM_ID) {
            try {
                // open exist stream
                inner = client.openStream(streamId, OpenStreamOptions.newBuilder().epoch(epoch).build()).get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                if (e.getCause() instanceof IOException) {
                    throw (IOException) (e.getCause());
                } else {
                    throw new RuntimeException(e.getCause());
                }
            }
            LOGGER.info("opened existing stream: stream_id={}, epoch={}, name={}", streamId, epoch, name);
        }
    }

    public void warmUp() throws IOException {
        if (this.inner == NOOP_STREAM) {
            try {
                this.inner = client.createAndOpenStream(CreateStreamOptions.newBuilder().replicaCount(replicaCount)
                        .epoch(epoch).build()).get();
                LOGGER.info("warmup, created and opened a new stream: stream_id={}, epoch={}, name={}", this.inner.streamId(), epoch, name);
                notifyListener(ElasticStreamMetaEvent.STREAM_DO_CREATE);
            } catch (Throwable e) {
                throw new IOException(e);
            }
        }
    }

    @Override
    public long streamId() {
        return inner.streamId();
    }

    @Override
    public long startOffset() {
        return inner.startOffset();
    }

    @Override
    public long confirmOffset() {
        return inner.confirmOffset();
    }

    @Override
    public long nextOffset() {
        return inner.nextOffset();
    }


    @Override
    public synchronized CompletableFuture<AppendResult> append(RecordBatch recordBatch) {
        if (this.inner == NOOP_STREAM) {
            try {
                this.inner = client.createAndOpenStream(CreateStreamOptions.newBuilder().replicaCount(replicaCount)
                        .epoch(epoch).build()).get();
                LOGGER.info("created and opened a new stream: stream_id={}, epoch={}, name={}", this.inner.streamId(), epoch, name);
                notifyListener(ElasticStreamMetaEvent.STREAM_DO_CREATE);
            } catch (Throwable e) {
                return FutureUtil.failedFuture(new IOException(e));
            }
        }
        return inner.append(recordBatch);
    }

    @Override
    public CompletableFuture<Void> trim(long newStartOffset) {
        return inner.trim(newStartOffset);
    }

    @Override
    public CompletableFuture<FetchResult> fetch(long startOffset, long endOffset, int maxBytesHint, ReadOptions readOptions) {
        return inner.fetch(startOffset, endOffset, maxBytesHint, readOptions);
    }

    @Override
    public CompletableFuture<Void> close() {
        return inner.close();
    }

    @Override
    public CompletableFuture<Void> destroy() {
        return inner.destroy();
    }

    @Override
    public String toString() {
        return "LazyStream{" + "name='" + name + '\'' + "streamId='" + inner.streamId() + '\'' + ", replicaCount=" + replicaCount + '}';
    }

    public void setListener(ElasticStreamEventListener listener) {
        this.eventListener = listener;
    }

    public void notifyListener(ElasticStreamMetaEvent event) {
        try {
            Optional.ofNullable(eventListener).ifPresent(listener -> listener.onEvent(inner.streamId(), event));
        } catch (Throwable e) {
            LOGGER.error("got notify listener error", e);
        }
    }

    static class NoopStream implements Stream {
        @Override
        public long streamId() {
            return NOOP_STREAM_ID;
        }

        @Override
        public long startOffset() {
            return 0;
        }

        @Override
        public long confirmOffset() {
            return 0;
        }

        @Override
        public long nextOffset() {
            return 0;
        }

        @Override
        public CompletableFuture<AppendResult> append(RecordBatch recordBatch) {
            return FutureUtil.failedFuture(new UnsupportedOperationException("noop stream"));
        }

        @Override
        public CompletableFuture<Void> trim(long newStartOffset) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<FetchResult> fetch(long startOffset, long endOffset, int maxBytesHint, ReadOptions readOptions) {
            return CompletableFuture.completedFuture(Collections::emptyList);
        }

        @Override
        public CompletableFuture<Void> close() {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Void> destroy() {
            return CompletableFuture.completedFuture(null);
        }
    }
}
