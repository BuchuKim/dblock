package com.buchu.dblock.infrastructure;

import com.buchu.dblock.domain.Goods;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface GoodsJpaRepository extends JpaRepository<Goods,Long> {
    Optional<Goods> findByName(String name);

    /**
     * 조회하는 레코드에 락을 걸어 race condition을 타파한다.
     * PESSIMISTIC_READ : 조회되는 레코드에 공유락을 건다.
     * PESSIMISTIC_WRITE : 조회되는 레코드에 배타락을 건다.
     * @param name 상품 이름
     * @return
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Goods> findWithLockByName(String name);
}
