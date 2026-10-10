package com.hotel.hms.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotel.hms.Entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {

    User findByEmail(String email);

}