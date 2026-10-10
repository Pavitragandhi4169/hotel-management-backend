package com.hotel.hms.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotel.hms.Entity.Staff;

public interface StaffRepository extends JpaRepository<Staff, Integer> {

}