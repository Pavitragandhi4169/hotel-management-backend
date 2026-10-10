package com.hotel.hms.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hotel.hms.Entity.Staff;
import com.hotel.hms.Exception.ResourceNotFoundException;
import com.hotel.hms.Repository.StaffRepository;

@Service
public class StaffService {

    private final StaffRepository staffRepository;

    public StaffService(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    // Get all staff
    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }

    // Get staff by ID
    public Staff getStaffById(Integer id) {
        return staffRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Staff not found"));
    }

    // Create or update staff
    public Staff saveStaff(Staff staff) {
        return staffRepository.save(staff);
    }

    // Delete staff
    public void deleteStaff(Integer id) {

        Staff staff = staffRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Staff not found"));

        staffRepository.delete(staff);
    }
}