package com.pia.telekom.repository;

import com.pia.telekom.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer>,
        JpaSpecificationExecutor<Customer> {

    @Override
    @EntityGraph(attributePaths = "region")
    Page<Customer> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "region")
    Page<Customer> findAll(Specification<Customer> spec, Pageable pageable);

    Page<Customer> findByRegion_RegionId(Integer regionId, Pageable pageable);

    @Query("SELECT c.customerId, c.hasAutopay FROM Customer c")
    List<Object[]> findAllCustomerIdAndAutopay();
}