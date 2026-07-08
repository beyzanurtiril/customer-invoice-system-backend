package com.pia.telekom.repository;

import com.pia.telekom.entity.CustomerRiskAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRiskAnalysisRepository extends JpaRepository<CustomerRiskAnalysis, Integer> {
    @Query("SELECT c.customerId FROM CustomerRiskAnalysis c")
    List<Integer> findAllIds();

    @Query("SELECT c.recommendAction, COUNT(c) FROM CustomerRiskAnalysis c WHERE c.recommendAction IS NOT NULL GROUP BY c.recommendAction")
    List<Object[]> countGroupedByRecommendAction();
}