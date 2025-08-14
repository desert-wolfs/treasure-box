package com.douniu.box;

import cn.hutool.core.date.DateUtil;
import com.douniu.box.redis.RedisUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.BitFieldSubCommands;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@SpringBootTest
class RedisTests {

    @Autowired
    private RedisUtils redisUtils;

    @Test
    void contextLoads() {
//        redisUtils.setBit("testBit", 14, true);
//        boolean test = redisUtils.getBit("testBit", 14);
//        System.out.println(test);
        long l = redisUtils.bitCount("testBit");
        System.out.println(l);
        Long l1 = redisUtils.bitPos("testBit", false);
        System.out.println(l1);
        Long l2 = redisUtils.bitPos("testBit", true, 0,0);
        System.out.println(l2);

        System.out.println(signCount());
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
