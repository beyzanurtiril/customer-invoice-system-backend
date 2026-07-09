package com.pia.telekom.specification;

import com.pia.telekom.entity.Customer;
import com.pia.telekom.entity.Invoice;
import com.pia.telekom.entity.Subscription;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class CustomerSpecification {

    public static Specification<Customer> filterBy(String name, String surname,
                                                   Integer regionId, String cityType,
                                                   String subscriptionType,
                                                   Integer minOverdueCount) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (name != null && !name.isBlank()) {
                String[] tokens = name.trim().split("\\s+");
                var searchPredicate = cb.conjunction();

                for (String token : tokens) {
                    String pattern = "%" + token.toLowerCase() + "%";
                    var tokenPredicate = cb.or(
                            cb.like(cb.lower(root.get("name")), pattern),
                            cb.like(cb.lower(root.get("surname")), pattern)
                    );
                    searchPredicate = cb.and(searchPredicate, tokenPredicate);
                }

                predicates = cb.and(predicates, searchPredicate);
            }
            if (surname != null && !surname.isBlank()) {
                predicates = cb.and(predicates,
                        cb.like(cb.lower(root.get("surname")), "%" + surname.toLowerCase() + "%"));
            }
            if (regionId != null) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("region").get("regionId"), regionId));
            }
            if (cityType != null && !cityType.isBlank()) {
                predicates = cb.and(predicates,
                        cb.equal(cb.lower(root.get("region").get("cityType")), cityType.toLowerCase()));
            }

            if (subscriptionType != null && !subscriptionType.isBlank()) {
                Subquery<Integer> subSub = query.subquery(Integer.class);
                var subRoot = subSub.from(Subscription.class);
                subSub.select(subRoot.get("customer").get("customerId"));
                subSub.where(cb.equal(cb.lower(subRoot.get("product").get("subscriptionType")), subscriptionType.toLowerCase()));

                predicates = cb.and(predicates, root.get("customerId").in(subSub));
            }

            if (minOverdueCount != null) {
                Subquery<Long> overdueSub = query.subquery(Long.class);
                var invoiceRoot = overdueSub.from(Invoice.class);
                overdueSub.select(cb.count(invoiceRoot));
                overdueSub.where(
                        cb.equal(invoiceRoot.get("customer"), root),
                        cb.isNull(invoiceRoot.get("paymentDate")),
                        cb.lessThan(invoiceRoot.get("dueDate"), LocalDate.now()),
                        cb.greaterThan(invoiceRoot.get("invoiceDate"), LocalDate.now().minusMonths(12))
                );
                predicates = cb.and(predicates,
                        cb.greaterThanOrEqualTo(overdueSub, minOverdueCount.longValue()));
            }

            return predicates;
        };
    }
}