package com.buchu.dblock.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class MarketDistributedLock {
    private final RedissonClient redissonClient;
    private final MarketService marketService;

    /**
     * Redisson의 분산 락(distributed lock)을 이용해 race condition을 컨트롤한다.
     * 동작 결과는 나머지 메소드와 일치한다.
     * @param customerName 구매자 이름
     * @param goodsName 상품 이름
     * @param number 상품 개수
     */
    public void buyGoodsWithDistributedLock(String customerName, String goodsName, int number) {
        RLock lock = redissonClient.getLock(goodsName + ":lock");
        try {
            // 분산락 획득
            if (!lock.tryLock(3L,3L, TimeUnit.SECONDS)) {
                return;
            }
            marketService.buyGoodsWithoutLock(customerName, goodsName, number);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (lock.isLocked()) {
                lock.unlock();
            }
        }
    }
}
