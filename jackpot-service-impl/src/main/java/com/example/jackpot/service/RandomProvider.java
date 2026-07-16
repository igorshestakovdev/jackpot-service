package com.example.jackpot.service;

import java.math.BigDecimal;

public interface RandomProvider {

    BigDecimal nextValue();
}
