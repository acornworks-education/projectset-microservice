package com.acornworks.projectset.domains;

import java.math.BigDecimal;
import java.util.Calendar;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SpotData {
    private String ticker;
    private BigDecimal price;
    private Calendar timestamp;
}
