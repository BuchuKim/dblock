package com.buchu.dblock.application;

import com.buchu.dblock.domain.Customer;
import com.buchu.dblock.domain.Goods;
import com.buchu.dblock.infrastructure.CustomerJpaRepository;
import com.buchu.dblock.infrastructure.GoodsJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MarketService {
    private final CustomerJpaRepository customerJpaRepository;
    private final GoodsJpaRepository goodsJpaRepository;

    /**
     * customer가 number 개수의 goods를 구매한다. 별다른 동시성 제어를 하지 않는다.
     * @param customerName 구매자 이름
     * @param goodsName 상품 이름
     * @param number 상품 개수
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void buyGoodsWithoutLock(String customerName, String goodsName, int number) {
        Customer customer = customerJpaRepository.findByName(customerName)
                .orElseThrow(EntityNotFoundException::new);
        Goods goods = goodsJpaRepository.findByName(goodsName)
                .orElseThrow(EntityNotFoundException::new);

        customer.buy(goods, number);
        log.info("{}의 구매 후 {}",customerName,goods.getStockNumber());
    }

    /**
     * customer가 number 개수의 goods를 구매한다.
     * 배타락을 통해, 아래 트랜잭션이 레코드 락을 획득한 이후엔 다른 트랜잭션의 RW 접근이 불가하다.
     * @param customerName 구매자 이름
     * @param goodsName 상품 이름
     * @param number 상품 개수
     */
    @Transactional
    public void buyGoodsWithLock(String customerName, String goodsName, int number) {
        Customer customer = customerJpaRepository.findByName(customerName)
                .orElseThrow(EntityNotFoundException::new);
        Goods goods = goodsJpaRepository.findWithLockByName(goodsName)
                .orElseThrow(EntityNotFoundException::new);

        customer.buy(goods, number);
    }
}
