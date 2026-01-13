#!/bin/bash

BASE_DIR=$(cd "$(dirname "$0")" && pwd)

JARS=(
  "user-service-1.0.0.jar"
  "order-service-1.0.0.jar"
  "payment-service-1.0.0.jar"
  "gateway-1.0.0.jar"
)

echo "正在停止所有服务..."

for JAR in "${JARS[@]}"; do
    SERVICE_NAME=$(echo "$JAR" | sed 's/-[0-9].*$//')
    PID=$(ps -ef | grep "$JAR" | grep -v grep | awk '{print $2}')

    if [ -n "$PID" ]; then
        echo "停止 [$SERVICE_NAME] PID: $PID"
        kill -9 $PID
        echo "已停止"
    else
        echo "[$SERVICE_NAME] 未在运行"
    fi
done

echo "所有服务已停止"