#!/bin/bash

# ==== 配置区域 ====
JAR_NAME="/root/jar/code-generate-0.0.1-SNAPSHOT.jar"  
LOG_FILE="app.log"               # 日志文件（当前目录）
PID_FILE="app.pid"               # 进程ID文件（用于停止服务）
# ==== 配置区域结束 ====

# 启动函数
start() {
  # 检查JAR文件是否存在
  if [ ! -f "$JAR_NAME" ]; then
    echo "错误：未找到JAR文件 $JAR_NAME"
    exit 1
  fi

  # 检查服务是否已运行
  if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p "$PID" > /dev/null; then
      echo "服务已在运行中，PID: $PID"
      exit 0
    else
      # 清理残留PID文件
      rm -f "$PID_FILE"
    fi
  fi

  # 启动服务（后台运行+守护进程+日志输出）
  echo "启动服务... JVM参数: -Xms64m -Xmx64m"
  nohup java -Xms64m -Xmx64m -jar "$JAR_NAME" > "$LOG_FILE" 2>&1 &
  PID=$!
  echo "$PID" > "$PID_FILE"
  echo "服务启动成功，PID: $PID，日志文件: $(pwd)/$LOG_FILE"
}

# 停止函数
stop() {
  if [ ! -f "$PID_FILE" ]; then
    echo "服务未运行"
    exit 1
  fi

  PID=$(cat "$PID_FILE")
  echo "停止服务，PID: $PID..."
  kill "$PID" && rm -f "$PID_FILE"
  echo "服务已停止"
}

# 命令分发
case "$1" in
  start)
    start
    ;;
  stop)
    stop
    ;;
  *)
    echo "用法: $0 {start|stop}"
    exit 1
    ;;
esac
