package com.gopoli.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "name")
    private String name;

    @Column(name = "phone")
    private String phone;

    @Column(name = "program_id")
    private Integer programId;

    @Column(name = "status_id")
    private Integer statusId;

    @Column(name = "user_type_id")
    private Integer userTypeId;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "profile_photo", columnDefinition = "TEXT")
    private String profilePhoto;
}
