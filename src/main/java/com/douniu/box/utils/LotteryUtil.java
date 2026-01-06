package com.douniu.box.utils;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

/**
 * LotteryUtil
 *
 * @date 2023/6/20 15:02
 */
public class LotteryUtil {

    /**
     * 随机抽奖
     *
     * @param prizes 奖品列表
     * @param function 概率获取
     * @return 奖品
     * @param <T> 奖品
     */
    public static <T> T lottery(List<T> prizes, Function<T, Number> function) {
        double[] rates = toRates(prizes, function);
        int index = lottery(rates);
        return index >= 0 ? prizes.get(index) : null;
    }

    /**
     * 多次随机抽奖
     *
     * @param prizes 奖品列表
     * @param function 概率获取
     * @param count 次数
     * @return 奖品
     * @param <T> 奖品
     */
    public static <T> List<T> lottery(List<T> prizes, Function<T, Number> function, int count) {
        double[] rates = toRates(prizes, function);
        List<T> lotteryPrizes = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int index = lottery(rates);
            if (index < 0) continue;

            lotteryPrizes.add(prizes.get(index));
        }
        return lotteryPrizes;
    }

    /**
     * 随机抽奖
     * 根据概率占比进行随机抽奖
     * 因此概率和可以不等于1
     *
     * @param rates 原始的概率列表
     * @return 索引
     */
    public static int lottery(double[] rates) {
        double sumRate = Arrays.stream(rates).sum();
        if (sumRate <= 0) return -1;

        // 计算每个物品在总概率的基础下的概率情况
        int size = rates.length;
        double tempSumRate = 0d;
        double[] newRates = new double[size + 1];
        for (int i = 0; i < size; i++) {
            tempSumRate += rates[i];
            newRates[i] = tempSumRate / sumRate;
        }

        // 根据区块值来获取抽取到的物品索引
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double randomPoint = random.nextDouble();
        newRates[size] = randomPoint;
        Arrays.sort(newRates);
        return Arrays.binarySearch(newRates, randomPoint);
    }

    /**
     * 一次抽奖
     * 指定概率返回是否中奖
     *
     * @param rate 概率
     * @return boolean
     */
    public static boolean once(double rate) {
        if (rate >= 1) return true;

        ThreadLocalRandom random = ThreadLocalRandom.current();
        double randomPoint = random.nextDouble();
        return randomPoint <= rate;
    }

    /**
     * 转换数据结构为Rates数组
     *
     * @param prizes 奖励
     * @param function 概率获取
     * @return Rates数组
     * @param <T> 奖品
     */
    private static <T> double[] toRates(List<T> prizes, Function<T, Number> function) {
        return CollectionUtils.isEmpty(prizes)
                ? ArrayUtils.EMPTY_DOUBLE_ARRAY
                : prizes.stream()
                    .filter(Objects::nonNull)
                    .map(function)
                    .mapToDouble(Number::doubleValue)
                    .toArray();
    }

    public static void main(String[] args) {
        List<Prize> prizes = List.of(
                new Prize(0, 0.1),
                new Prize(1, 0.2),
                new Prize(2, 0.3),
                new Prize(3, 0.4)
        );
        System.out.println(lottery(prizes, Prize::rate));
    }

    public record Prize(int index, double rate) {
    }
}
