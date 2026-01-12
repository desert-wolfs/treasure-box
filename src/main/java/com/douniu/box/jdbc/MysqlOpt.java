package com.douniu.box.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class MysqlOpt {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/treasure_box?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&rewriteBatchedStatements=true";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "123456";

    private static final int TOTAL_RECORDS = 2000000;  // 总记录数
    private static final int BATCH_SIZE = 1000;        // 批量大小
    private static final int MAX_USER_ID = 50000;      // 最大用户ID
    private static final int SCORE_TYPE_MAX = 9;       // 最大score_type
    private static final int SCORE_TOTAL_MIN = 100;    // 最小分数
    private static final int SCORE_TOTAL_MAX = 10000;  // 最大分数

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            // 加载驱动
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 建立连接
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            connection.setAutoCommit(false);

            // 创建预处理语句
            String sql = "INSERT INTO `treasure_box`.`activity_test_score` (`id`, `user_id`, `score_type`, `score_total`, `score_day`, `create_time`) VALUES (?, ?, ?, ?, ?, ?)";
            preparedStatement = connection.prepareStatement(sql);

            // 生成最近7天的日期列表
            Date[] recentDates = generateRecent7Days();

            Random random = new Random();
            long startTime = System.currentTimeMillis();

            // 批量插入数据
            for (int i = 1; i <= TOTAL_RECORDS; i++) {
                // 设置参数
                preparedStatement.setInt(1, i);  // id自增
                preparedStatement.setInt(2, random.nextInt(MAX_USER_ID) + 1);  // user_id: 1-50000
                preparedStatement.setInt(3, random.nextInt(SCORE_TYPE_MAX) + 1);  // score_type: 1-9
                preparedStatement.setInt(4, random.nextInt(SCORE_TOTAL_MAX - SCORE_TOTAL_MIN + 1) + SCORE_TOTAL_MIN);  // score_total: 100-10000

                // 随机选择最近7天的日期
                Date selectedDate = recentDates[random.nextInt(recentDates.length)];
                preparedStatement.setString(5, DATE_FORMAT.format(selectedDate));  // score_day

                // 生成当天的随机时间
                Date randomTime = generateRandomTimeOfDay(selectedDate);
                preparedStatement.setString(6, DATETIME_FORMAT.format(randomTime));  // create_time

                // 添加到批处理
                preparedStatement.addBatch();

                // 每BATCH_SIZE条执行一次批处理
                if (i % BATCH_SIZE == 0) {
                    preparedStatement.executeBatch();
                    connection.commit();
                    preparedStatement.clearBatch();

                    // 输出进度
                    if (i % 100000 == 0) {
                        long elapsedTime = System.currentTimeMillis() - startTime;
                        System.out.printf("已插入 %d 条数据，耗时 %.2f 秒%n", i, elapsedTime / 1000.0);
                    }
                }
            }

            // 执行剩余的批处理
            preparedStatement.executeBatch();
            connection.commit();

            long endTime = System.currentTimeMillis();
            System.out.printf("\n✅ 批量插入完成！");
            System.out.printf("总记录数: %d 条", TOTAL_RECORDS);
            System.out.printf("总耗时: %.2f 秒", (endTime - startTime) / 1000.0);
            System.out.printf("平均速度: %.2f 条/秒", TOTAL_RECORDS / ((endTime - startTime) / 1000.0));

        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            // 关闭资源
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 生成最近7天的日期数组
     */
    private static Date[] generateRecent7Days() {
        Date[] dates = new Date[7];
        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < 7; i++) {
            calendar.add(Calendar.DAY_OF_MONTH, -i);
            dates[i] = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, i); // 恢复日期
        }

        return dates;
    }

    /**
     * 生成指定日期的随机时间
     */
    private static Date generateRandomTimeOfDay(Date date) {
        Random random = new Random();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        // 生成随机时间：00:00:00 - 23:59:59
        calendar.set(Calendar.HOUR_OF_DAY, random.nextInt(24));
        calendar.set(Calendar.MINUTE, random.nextInt(60));
        calendar.set(Calendar.SECOND, random.nextInt(60));
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }
}
