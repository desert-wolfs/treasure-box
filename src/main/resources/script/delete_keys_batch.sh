#!/bin/bash

## 遍历节点批量循环删除集群中的redisKey
# 不使用 set -e，避免 redis-cli 某次返回非 0 时脚本直接静默退出
set -uo pipefail

if [ $# -eq 0 ]; then
    echo "请传入键的前缀作为参数。"
    exit 1
fi

NODES=(
  "192.168.192.12:7001"
  "192.168.192.12:7002"
  "192.168.192.13:7001"
  "192.168.192.13:7002"
  "192.168.192.14:7001"
  "192.168.192.14:7002"
  "192.168.192.15:7001"
  "192.168.192.15:7002"
)

PATTERN="$1*"
SCAN_COUNT=5000

deleted_key_count=0

echo "*** 即将删除集群中所有以 $1 为前缀的键。"
read -p "*** 确认要执行删除操作吗？(输入 y 或 Y 确认，其他任意键取消): " confirm

if [ "$confirm" != "y" ] && [ "$confirm" != "Y" ]; then
    echo "删除操作已取消。"
    exit 0
fi

for NODE in "${NODES[@]}"
do
    IFS=':' read -r HOST PORT <<< "$NODE"

    echo "------------------------------------------------------------------------"
    echo "检查节点 $NODE 角色..."

    role=$(redis-cli --raw -h "$HOST" -p "$PORT" ROLE 2>&1 | head -n 1)
    role_status=$?

    if [ $role_status -ne 0 ]; then
        echo "节点 $NODE ROLE 检查失败：$role"
        continue
    fi

    if [ "$role" != "master" ]; then
        echo "节点 $NODE 不是 master，当前角色: $role，跳过。"
        continue
    fi

    echo "----开始扫描并 pipeline 删除 $NODE 中匹配 $PATTERN 的 key..."

    cursor=0
    node_deleted_count=0

    while true
    do
        result=$(redis-cli --raw -h "$HOST" -p "$PORT" SCAN "$cursor" MATCH "$PATTERN" COUNT "$SCAN_COUNT" 2>&1)
        scan_status=$?

        if [ $scan_status -ne 0 ]; then
            echo "节点 $NODE 执行 SCAN 失败，cursor=$cursor"
            echo "$result"
            break
        fi

        new_cursor=$(printf '%s\n' "$result" | head -n 1)
        keys=$(printf '%s\n' "$result" | tail -n +2)

        if [ -z "$new_cursor" ]; then
            echo "节点 $NODE 返回的 SCAN cursor 为空，原始返回："
            echo "$result"
            break
        fi

        if [ -n "$keys" ]; then
            batch_count=$(printf '%s\n' "$keys" | sed '/^$/d' | wc -l | tr -d ' ')

            if [ "$batch_count" -gt 0 ]; then
                echo "节点 $NODE 本批提交 UNLINK key 数: $batch_count"

                pipe_output=$(
                    printf '%s\n' "$keys" \
                    | sed '/^$/d' \
                    | LC_ALL=C awk '
                        {
                            key = $0
                            printf "*2\r\n$6\r\nUNLINK\r\n$%d\r\n%s\r\n", length(key), key
                        }
                    ' \
                    | redis-cli -h "$HOST" -p "$PORT" --pipe 2>&1
                )
                pipe_status=$?

                echo "$pipe_output"

                if [ $pipe_status -ne 0 ]; then
                    echo "节点 $NODE redis-cli --pipe 返回非 0，状态码: $pipe_status"
                    echo "建议检查是否存在 MOVED、ASK、NOAUTH、READONLY、connection refused 等错误。"
                    break
                fi

                if ! printf '%s\n' "$pipe_output" | grep -q "errors: 0"; then
                    echo "节点 $NODE pipeline 删除存在 Redis 返回错误。"
                    echo "停止当前节点扫描，避免继续误删或刷错误。"
                    break
                fi

                node_deleted_count=$((node_deleted_count + batch_count))
                deleted_key_count=$((deleted_key_count + batch_count))
            fi
        fi

        if [ "$new_cursor" = "0" ]; then
            break
        fi

        cursor="$new_cursor"
    done

    echo "节点 $NODE 执行完成，本节点提交删除 key 数: $node_deleted_count"
done

echo "------------------------------------------------------------------------"
echo "删除操作执行完毕，总共提交删除 key 数: $deleted_key_count"