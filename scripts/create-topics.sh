#!/bin/bash

echo "Waiting Kafka..."
sleep 15

kafka-topics \
  --bootstrap-server kafka:9092 \
  --create \
  --if-not-exists \
  --topic customer-events \
  --partitions 1 \
  --replication-factor 1

kafka-topics \
  --bootstrap-server kafka:9092 \
  --create \
  --if-not-exists \
  --topic bank-events \
  --partitions 1 \
  --replication-factor 1

echo "Topics created!"