package com.gopoli.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gopoli.api.model.Message;

public interface MessageRepository extends JpaRepository<Message, Integer> {

    List<Message> findByTripIdOrderBySentAtAsc(Integer tripId);
}
