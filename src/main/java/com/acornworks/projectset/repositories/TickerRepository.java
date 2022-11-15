package com.acornworks.projectset.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.acornworks.projectset.domains.Ticker;

@Repository
public interface TickerRepository extends CrudRepository<Ticker, String> {
    
}
