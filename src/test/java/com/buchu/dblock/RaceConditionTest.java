package com.buchu.dblock;

import com.buchu.dblock.application.MarketDistributedLock;
import com.buchu.dblock.application.MarketService;
import com.buchu.dblock.domain.Customer;
import com.buchu.dblock.domain.Goods;
import com.buchu.dblock.infrastructure.CustomerJpaRepository;
import com.buchu.dblock.infrastructure.GoodsJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest
public class RaceConditionTest {
    @Autowired
    private CustomerJpaRepository customerJpaRepository;

    @Autowired
    private GoodsJpaRepository goodsJpaRepository;

    @Autowired
    private MarketService marketService;

    @Autowired
    private MarketDistributedLock marketDistributedLock;

    @BeforeEach
    public void setUp() {
        customerJpaRepository.deleteAll();
        goodsJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("동시성 제어 없이 레코드를 수정하면 race condition이 발생한다.")
    public void raceConditionTest() throws InterruptedException {
        // given : 구매자 100명과 재고가 100개인 대파가 존재한다.
        List<Customer> customers = IntStream.range(0,100)
                .mapToObj(i -> Customer.builder().name("구매자" + i).build())
                .map(customer -> customerJpaRepository.save(customer)).toList();
        Goods leek = goodsJpaRepository.save(
                Goods.builder().name("대파").stockNumber(100).build());
        CountDownLatch countDownLatch = new CountDownLatch(100);

        // when : 구매자 100명이 lock 없는 메소드를 통해 동시에 대파를 구매한다.
        List<Thread> threads = customers.stream().map(
                customer -> new Thread(() -> {
                    marketService.buyGoodsWithoutLock(
                            customer.getName(), leek.getName(), 1);
                    countDownLatch.countDown();
                })).toList();
        threads.forEach(Thread::start);
        countDownLatch.await();

        // then : 재고가 0개가 아니다.
        goodsJpaRepository.findByName("대파").ifPresent(
                goods -> assertNotEquals(0,goods.getStockNumber()));
    }

    @Test
    @DisplayName("배타락을 통해 동시성 이슈를 해결한다.")
    public void pessimisticLockTest() throws InterruptedException {
        // given : 구매자 100명과 재고가 100개인 대파가 존재한다.
        List<Customer> customers = IntStream.range(0,100)
                .mapToObj(i -> Customer.builder().name("구매자" + i).build())
                .map(customer -> customerJpaRepository.save(customer)).toList();
        Goods leek = goodsJpaRepository.save(
                Goods.builder().name("대파").stockNumber(100).build());
        CountDownLatch countDownLatch = new CountDownLatch(100);

        // when : 구매자 100명이 PESSIMIST_WRITE 락이 필요한 트랜잭션 메소드를 사용한다.
        List<Thread> threads = customers.stream().map(
                customer -> new Thread(() -> {
                    marketService.buyGoodsWithLock(
                            customer.getName(), leek.getName(), 1);
                    countDownLatch.countDown();
                })).toList();
        threads.forEach(Thread::start);
        countDownLatch.await();

        // then : 재고가 0개다.
        goodsJpaRepository.findByName("대파").ifPresent(
                goods -> assertEquals(0,goods.getStockNumber()));
    }

    @Test
    @DisplayName("분산락을 통해 동시성 이슈를 해결한다.")
    public void distributedLockTest() throws InterruptedException {
        // given : 구매자 100명과 재고가 100개인 대파가 존재한다.
        List<Customer> customers = IntStream.range(0,100)
                .mapToObj(i -> Customer.builder().name("구매자" + i).build())
                .map(customer -> customerJpaRepository.save(customer)).toList();
        goodsJpaRepository.save(
                Goods.builder().name("leek").stockNumber(100).build());
        CountDownLatch countDownLatch = new CountDownLatch(100);

        // when : 구매자 100명이 PESSIMIST_WRITE 락이 필요한 트랜잭션 메소드를 사용한다.
        List<Thread> threads = customers.stream().map(
                customer -> new Thread(() -> {
                    marketDistributedLock.buyGoodsWithDistributedLock(
                            customer.getName(), "leek", 1);
                    countDownLatch.countDown();
                })).toList();
        threads.forEach(Thread::start);
        countDownLatch.await();

        // then : 재고가 0개다.
        goodsJpaRepository.findByName("leek").ifPresent(
                goods -> assertEquals(0,goods.getStockNumber()));
    }
}
