#!/bin/bash

# 项目目录（可修改）
BASE_DIR=$(cd "$(dirname "$0")" && pwd)
LOG_DIR="$BASE_DIR/logs"
mkdir -p "$LOG_DIR"

# 要启动的jar包列表（按需修改）
JARS=(
  "user-service-1.0.0.jar"
  "order-service-1.0.0.jar"
  "payment-service-1.0.0.jar"
  "gateway-1.0.0.jar"
  # 在这里继续添加你的jar...
)

# Java参数（统一配置，可单独覆盖）
JVM_OPTS="-Xms1024m -Xmx2048m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$LOG_DIR"

echo "=================================================="
echo "开始启动所有服务..."
echo "项目目录: $BASE_DIR"
echo "日志目录: $LOG_DIR"
echo "=================================================="

for JAR in "${JARS[@]}"; do
    JAR_PATH="$BASE_DIR/$JAR"

    if [ ! -f "$JAR_PATH" ]; then
        echo "✘ [$JAR] 不存在！跳过..."
        continue
    fi

    # 提取服务名（去掉版本号后缀，更美观）
    SERVICE_NAME=$(echo "$JAR" | sed 's/-[0-9].*$//')

    # 检查是否已在运行
    PID=$(ps -ef | grep "$JAR" | grep -v grep | awk '{print $2}')
    if [ -n "$PID" ]; then
        echo "✔ [$SERVICE_NAME] 已在运行，PID: $PID"
        continue
    fi

    echo "▶ 正在启动 [$SERVICE_NAME] ..."

    # 关键：nohub + & + /dev/null 2>&1 + tail -0f 实现后台启动并实时打印日志
    nohup java $JVM_OPTS -jar "$JAR_PATH" > "$LOG_DIR/${SERVICE_NAME}.log" 2>&1 &

    # 等待几秒看看是否启动成功
    sleep 3
    NEW_PID=$(ps -ef | grep "$JAR" | grep -v grep | awk '{print $2}')
    if [ -n "$NEW_PID" ]; then
        echo "✔ [$SERVICE_NAME] 启动成功！PID: $NEW_PID"
        echo "   日志: tail -f $LOG_DIR/${SERVICE_NAME}.log"
    else
        echo "✘ [$SERVICE_NAME] 启动失败！请查看日志: $LOG_DIR/${SERVICE_NAME}.log"
    fi
done

echo "=================================================="
echo "所有服务启动完成！"
echo "查看所有日志：cd $LOG_DIR && tail -f *"
echo "停止所有服务：./stop_all.sh"
echo "=================================================="