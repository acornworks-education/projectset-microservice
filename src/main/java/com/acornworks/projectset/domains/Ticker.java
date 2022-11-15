package com.acornworks.projectset.domains;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Entity
@Getter
@AllArgsConstructor
public class Ticker {
    public Ticker() {}

    @Id
    private String ticker;

    private String name;

    @Override
    public boolean equals(Object t) {
        if (t == null) return false;

        final Ticker tickerObj = (Ticker)t;

        return this.ticker.equals(tickerObj.getTicker()) && this.name.equals(tickerObj.getName());
    }
}
