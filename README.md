<h1 align="center">
AutoMQ for Kafka
</h1>
<h3 align="center">
    The truly serverless Kafka solution that maximizes the benefits of cloud
</h3>

[![Docs](https://img.shields.io/badge/Docs-blue)](https://docs.automq.com/zh/docs/automq-s3kafka/YUzOwI7AgiNIgDk1GJAcu6Uanog)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

[//]: # ([![E2E_TEST]&#40;https://github.com/AutoMQ/automq-for-kafka/actions/workflows/nightly-e2e.yml/badge.svg&#41;]&#40;https://github.com/AutoMQ/automq-for-kafka/actions/workflows/nightly-e2e.yml&#41;)

## What is AutoMQ for Kafka
AutoMQ for Apache Kafka is redesigned based on cloud infrastructure, and users 
benefit from 
**elastic computing resources** and nearly **unlimited cheap storage** in 
the cloud.

It is developed based on Apache Kafka and replace Kafka's local storage with S3 Stream. This design allows Kafka Brokers:
- Become stateless and could scale up/down in seconds. 
- By making targeted modifications at the LogSegment level and extensively reusing upper-level code, it guarantees **100% functional compatibility**.

Compared to Apache Kafka, AutoMQ for Apache Kafka offers the following advantages:

1. Enhanced Scalability: It leverages cloud-native infrastructure and stateless Kafka Brokers, enabling seamless scaling to meet varying workloads. This elasticity allows for efficient resource allocation and ensures optimal performance even during peak periods.

2. 10x Cheaper: By utilizing object storage, it could save storage cost up to 90%; By leveraging serverless architecture and spot instance, it can achieve significant cost savings of up to 90% in compute expenses.

3. Simplified Management: No need to manage disks; It automatically performs second-level partition load balancing across Brokers.

## S3Stream
AutoMQ for Kafka is built on S3Stream directly, a streaming library based on object storage. Please refer to [S3Stream](https://docs.automq.com/zh/docs/automq-s3kafka/Q8fNwoCDGiBOV6k8CDSccKKRn9d) for more architecture details.

AutomMQ for Kafka and AutoMQ for RocketMQ share the same codebase of S3Stream, so please refer to [AutoMQ RocketMQ](https://github.com/AutoMQ/automq-for-rocketmq/tree/main/s3stream) for the source code.

## Quick Start

### Local Run
#### Launch cluster
Launch an AutoMQ for Kafka cluster locally using Docker Compose.

This cluster comprises 1 Controller node, 2 Broker nodes, and an additional LocalStack container to simulate S3 services locally.
``` bash
# launch AutoMQ for Kafka cluster
docker compose -f docker/docker-compose.yaml up -d
```
#### Run a console producer and consumer
1. Create a topic to store your events:
``` bash
# create quickstart-events topic
bin/kafka-topics.sh --create --topic quickstart-events --bootstrap-server localhost:9094
```

2. Run the console producer client to write a few events into your topic. By default, each line you enter will result in a separate event being written to the topic.
``` bash
bin/kafka-console-producer.sh --topic quickstart-events --bootstrap-server localhost:9094
```
You may input some messages like:
``` text
This is my first event
This is my second event
```

3. Run the console consumer client to read the events you just created:
``` bash
# CRTL-C to exit the consumer
# run console consumer
bin/kafka-console-consumer.sh --topic quickstart-events --from-beginning --bootstrap-server localhost:9094
```

4. Clean up the cluster
``` bash
docker compose -f docker/docker-compose.yaml down -v
```

[Explore more](https://docs.automq.com/zh/docs/automq-s3kafka/VKpxwOPvciZmjGkHk5hcTz43nde): Second-level partition migration and automatic traffic rebalancing.


### AutoMQ Cloud
Sign up for a [free trial](https://docs.automq.com/zh/docs/automq-s3kafka/EKcdwqXFWixsm0kH5zVcqYzhnle) of AutoMQ Cloud and experience auto scaling with AutoMQ for Kafka.

## License
[Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html)

