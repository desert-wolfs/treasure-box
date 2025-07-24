#!/bin/bash

# 检查是否传入前缀参数
if [ $# -eq 0 ]; then
    echo "请传入键的前缀作为参数。"
    exit 1
fi

# Redis 集群节点列表(根据自身redis集群)
NODES=("127.0.0.1:7001" "127.0.0.1:7002")
PASSWORD="你的redis密码, 没有就不需要这个"
# 从命令行参数获取前缀
PATTERN="$1*"
# 初始化删除 key 的计数器
deleted_key_count=0

echo "*** 即将删除集群中所有以 $1 为前缀的键。"
read -p "*** 确认要执行删除操作吗？(输入 y 或 Y 确认，其他任意键取消): " confirm

if [ "$confirm" = "y" ] || [ "$confirm" = "Y" ]; then
    for NODE in "${NODES[@]}"
    do
        IFS=':' read -r HOST PORT <<< "$NODE"
        cursor=0
        while true
        do
            # 使用 scan 命令扫描符合前缀的键
            result=$(redis-cli -c -h "$HOST" -p "$PORT" --pass "$PASSWORD" --no-auth-warning scan "$cursor" MATCH "$PATTERN" COUNT 10000)
            new_cursor=$(echo "$result" | head -n 1)
            keys=$(echo "$result" | tail -n +2)
            if [ -n "$keys" ]; then
                for key in $keys
                do
                    # 使用 unlink 命令删除键
                    echo "------------------删除 $key --------------------"
 		                redis-cli -c -h "$HOST" -p "$PORT" --pass "$PASSWORD" --no-auth-warning unlink "$key"
                    # 判断删除是否成功，成功则计数器加 1
                    if [ $? -eq 0 ]; then
                        ((deleted_key_count++))
                    fi
		            done
            fi
	          echo "----游标 $new_cursor 已完成---"
            if [ "$new_cursor" = "0" ]; then
                break
            fi
            cursor=$new_cursor
        done
        echo "--------------------------节点 $NODE 已执行完成----------------------------------------------"
    done
    echo "删除操作执行完毕, 总共删除了 $deleted_key_count 个 key。"
else
    echo "删除操作已取消。"
fi
