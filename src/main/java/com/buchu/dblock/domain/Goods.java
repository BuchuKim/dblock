package com.buchu.dblock.domain;

import com.buchu.dblock.domain.exception.NotEnoughStockException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Goods {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @NotBlank
    @Length(max = 30)
    private String name;

    @Column
    private int stockNumber;

    /**
     * stockNumber만큼 현재 상품의 재고를 감소시킨다.
     * @param stockNumber
     */
    public void decreaseStock(int stockNumber) {
        isAvailable(stockNumber);
        this.stockNumber -= stockNumber;
    }

    /**
     * required 이상의 재고가 존재하는지 검증한다.
     * @param required
     */
    private void isAvailable(int required) {
        if (required > stockNumber) {
            throw new NotEnoughStockException(name);
        }
    }

    /**
     * 분산 락에 사용할 객체의 key를 반환한다.
     * @return key
     */
    public String generateKey() {
        return "goodsLock:" + this.id;
    }
}
