package com.gopoli.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "trip_members")
@IdClass(TripMemberId.class)
@Getter
@Setter
@NoArgsConstructor
public class TripMember {

    @Id
    @Column(name = "trip_id")
    private Integer tripId;

    @Id
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "group_role")
    private String groupRole;

    @Column(name = "participation_role")
    private String participationRole;
}
