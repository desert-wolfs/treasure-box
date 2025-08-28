package com.douniu.box;

import cn.hutool.core.date.DateUtil;
import com.douniu.box.redis.RedisUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.BitFieldSubCommands;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@SpringBootTest
class RedisTests {

    @Autowired
    private RedisUtils redisUtils;

    @Test
    void contextLoads() {
        // 操作位从左到右
//        long l = redisUtils.bitCount("testBit");
//        System.out.println(l);
//        Long l1 = redisUtils.bitPos("testBit", false);
//        System.out.println(l1);
//        Long l2 = redisUtils.bitPos("testBit", true, 0,0);
//        System.out.println(l2);
//        boolean b = redisUtils.setBit("testBit", 1, true);
//        System.out.println(b);
//        Long l = redisUtils.incrBitField("testBit", 8, 3, 222500);
//        System.out.println(l);

        bitFieldOperate();


    }

    private void bitFieldOperate() {
        // 1. 定义多个位段配置（模拟真实业务场景）
        List<RedisUtils.BitFieldConfig> configs = Arrays.asList(
                new RedisUtils.BitFieldConfig(false, 1, 0, 1),   // 位0: 1bit无符号（开关状态）
                new RedisUtils.BitFieldConfig(false, 4, 1, 15),  // 位1-4: 4bit无符号（0-15等级）
                new RedisUtils.BitFieldConfig(true, 3, 5, -3)    // 位5-7: 3bit有符号（-4~3数值）
        );

        // 2. 设置多段位值（原子操作）
        List<Long> setResults = redisUtils.setMultiBitField("testBit", configs);
        System.out.println("设置结果（旧值）: " + setResults);  // 首次设置返回null列表

        // 3. 读取多段位值
        List<Long> getResults = redisUtils.getMultiBitField("testBit", configs);
        System.out.println("读取结果: " + getResults);  // 应输出 [1, 15, -3]

        // 4. 验证结果（推荐添加断言）
        assert getResults.get(0) == 1 : "开关状态设置失败";
        assert getResults.get(1) == 15 : "会员等级设置失败";
        assert getResults.get(2) == -3 : "有符号数值设置失败";
    }

    public static Integer signCount() {
        //1.获取登录用户
        Long userid = 0L;

        //4.获取今天是本月的第几天
        int dayOfMonth = DateUtil.dayOfMonth(new Date());
        //5.获取本月截至今天为止的所有的签到记录,返回的是一个十进制的数字BITFIELD sign:5:202301 GET u
//        List<Long> result = stringRedisTemplate.opsForValuwe().bitField(
//                "key",
//                BitFieldSubCommands.create()
//                        .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0));
        List<Long> result = List.of(5L);
        //没有任务签到结果
        if (result == null ||  result.isEmpty()) {
            return 0;
        }
        Long num = result.get(0);
        if (num == null || num == 0) {
            return 0;
        }
        //6.循环遍历
        int count = 0;
        while (true) {
            //6.1让这个数字与1做与运算,得到数字的最后一个bit位可断这个数字是否为
            if ((num & 1) == 0) {
                //如果为0,签到结束
                break;
            } else {
                count ++;
            }
            num >>>= 1;
        }
        return count;
    }

}
