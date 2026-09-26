package com.gopoli.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gopoli.api.model.RecurringRoute;

public interface RecurringRouteRepository extends JpaRepository<RecurringRoute, Integer> {

    List<RecurringRoute> findByUserIdOrderByIdAsc(Integer userId);
}
