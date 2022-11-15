package com.acornworks.projectset.processors;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BarSeriesManager;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.Position;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.criteria.pnl.GrossReturnCriterion;

import com.acornworks.projectset.domains.StockPrice;
import com.acornworks.projectset.domains.StrategyName;
import com.acornworks.projectset.domains.TradingData;
import com.acornworks.projectset.domains.TradingResult;

import ta4jexamples.strategies.ADXStrategy;
import ta4jexamples.strategies.CCICorrectionStrategy;
import ta4jexamples.strategies.GlobalExtremaStrategy;
import ta4jexamples.strategies.MovingMomentumStrategy;
import ta4jexamples.strategies.RSI2Strategy;

@Component
public class StrategyProcessor {
    public BarSeries getBarSeriesFromStockData(List<StockPrice> stockPrices) {
        final BarSeries barSeries = new BaseBarSeriesBuilder().build();

        stockPrices.forEach(stockPrice -> {
            ZonedDateTime dateTime = ZonedDateTime.ofInstant(
                stockPrice.getDate().toInstant(), TimeZone.getTimeZone("GMT").toZoneId());
            
            barSeries.addBar(
                dateTime, 
                stockPrice.getOpen().toString(), 
                stockPrice.getHigh().toString(), 
                stockPrice.getLow().toString(), 
                stockPrice.getClose().toString(),
                stockPrice.getVolume().toString()
            );
        });

        return barSeries;
    }

    public TradingResult getAnalysisResult(List<StockPrice> stockPrices, StrategyName strategyName) {
        final BarSeries barSeries = getBarSeriesFromStockData(stockPrices);
        final Strategy strategy;

        if (strategyName.equals(StrategyName.ADX)) {
            strategy = ADXStrategy.buildStrategy(barSeries);
        } else if (strategyName.equals(StrategyName.CCICorrelation)) {
            strategy = CCICorrectionStrategy.buildStrategy(barSeries);
        } else if (strategyName.equals(StrategyName.GlobalExtrema)) {
            strategy = GlobalExtremaStrategy.buildStrategy(barSeries);
        } else if (strategyName.equals(StrategyName.MovingMomentum)) {
            strategy = MovingMomentumStrategy.buildStrategy(barSeries);
        } else if (strategyName.equals(StrategyName.RSI2)) {
            strategy = RSI2Strategy.buildStrategy(barSeries);
        } else {
            strategy = null;
        }

        final List<TradingData> tradingData = stockPrices.stream().map(stockPrice -> {
            final TradingData data = new TradingData();
            data.importFromStockPrice(stockPrice);

            return data;
        }).collect(Collectors.toList());

        final BarSeriesManager manager = new BarSeriesManager(barSeries);
        final TradingRecord record = manager.run(strategy);

        for(Position position : record.getPositions()) {
            boolean isBuy = position.getStartingType().equals(TradeType.BUY);

            for (int idx = position.getEntry().getIndex(); idx <= position.getExit().getIndex(); idx++) {
                tradingData.get(idx).setBuy(isBuy);
                tradingData.get(idx).setHold(true);
            }
        }

        final BigDecimal grossReturn = new BigDecimal(new GrossReturnCriterion().calculate(barSeries, record).toString());
        final TradingResult tradingResult = new TradingResult(tradingData, grossReturn);

        return tradingResult;
    }
}
