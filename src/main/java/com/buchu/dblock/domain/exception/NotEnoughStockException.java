package com.buchu.dblock.domain.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class NotEnoughStockException extends RuntimeException {
    private String goodsName;
}
