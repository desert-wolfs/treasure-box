#!/bin/bash
# start_all_auto.sh 自动启动当前目录下所有 jar（排除 *-sources.jar 等）

BASE_DIR=$(cd "$(dirname "$0")" && pwd)
LOG_DIR="$BASE_DIR/logs"
mkdir -p "$LOG_DIR"

JVM_OPTS="-Xms1g -Xmx2g -XX:+HeapDumpOnOutOfMemoryError"

for JAR in *.jar; do
    # 跳过 sources、javadoc 等
    if [[ "$JAR" =~ sources|javadoc|tests ]]; then
        continue
    fi

    [[ -f "$JAR" ]] || continue

    SERVICE_NAME="${JAR%-*}"  # 更智能取名

    if ps -ef | grep -v grep | grep -q "$JAR"; then
        echo "[$SERVICE_NAME] 已在运行"
        continue
    fi

    echo "启动 $SERVICE_NAME ($JAR)"
    nohup java $JVM_OPTS -jar "$JAR" > "$LOG_DIR/${SERVICE_NAME}.log" 2>&1 &
    sleep 2
done

echo "自动启动完成，日志目录：$LOG_DIR"