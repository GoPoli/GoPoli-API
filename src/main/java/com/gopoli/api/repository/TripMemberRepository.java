package com.gopoli.api.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gopoli.api.model.TripMember;
import com.gopoli.api.model.TripMemberId;

public interface TripMemberRepository extends JpaRepository<TripMember, TripMemberId> {

    List<TripMember> findByTripId(Integer tripId);

    List<TripMember> findByTripIdIn(Collection<Integer> tripIds);

    List<TripMember> findByUserId(Integer userId);

    long countByTripId(Integer tripId);
}
