package com.hotel.hms.Controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotel.hms.Entity.Staff;
import com.hotel.hms.Service.StaffService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    public List<Staff> getAllStaff() {
        return staffService.getAllStaff();
    }

    @GetMapping("/{id}")
    public Staff getStaffById(@PathVariable Integer id) {
        return staffService.getStaffById(id);
    }

    @PostMapping
    public Staff createStaff(@Valid @RequestBody Staff staff) {
        return staffService.saveStaff(staff);
    }

    @PutMapping("/{id}")
    public Staff updateStaff(
            @PathVariable Integer id,
            @Valid @RequestBody Staff staff) {

        staff.setId(id);

        return staffService.saveStaff(staff);
    }

    @DeleteMapping("/{id}")
    public String deleteStaff(@PathVariable Integer id) {
        staffService.deleteStaff(id);
        return "Staff deleted successfully";
    }
}