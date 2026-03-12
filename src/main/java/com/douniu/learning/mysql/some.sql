-- 榜单求前三名 top n问题
SELECT
    final.live_id,
    final.user_id,
    final.total_score
FROM (
         SELECT
             t.live_id,
             t.user_id,
             t.total_score,
             -- 核心逻辑：如果当前行的 live_id 等于上一行，则排名+1，否则重置为1
             @rn := IF(@prev_live = t.live_id, @rn + 1, 1) AS rn,
        -- 将当前 live_id 赋值给变量，供下一行比较
        @prev_live := t.live_id
         FROM (
             -- 第一层：先聚合算出总分
             SELECT
             live_id,
             user_id,
             SUM(send_total) as total_score
             FROM activity_ramadan_send
             -- 【优化点】这里最好加上 WHERE send_time 范围，否则全表扫
             GROUP BY live_id, user_id
             -- 【关键】必须先排序，变量法依赖有序数据
             ORDER BY live_id ASC, total_score DESC
             limit 99999999
             ) t,
             -- 初始化变量
             (SELECT @rn := 0, @prev_live := NULL) vars
     ) final
WHERE final.rn <= 3;