package com.gopoli.api.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gopoli.api.model.Trip;

public interface TripRepository extends JpaRepository<Trip, Integer> {

    List<Trip> findByCreatorIdAndStatusId(Integer creatorId, Integer statusId);

    List<Trip> findByStatusId(Integer statusId);

    List<Trip> findByIdInAndStatusId(Collection<Integer> ids, Integer statusId);
}
