package com.java.m3.demo.service;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.java.m3.demo.model.Storm;
import com.java.m3.demo.repository.StormRepository;

@Service
public class StormService {

    @Autowired
    private StormRepository stormRepo;

    public List<Storm> getAllStorms() {
        // [LỖI A09: Security Logging and Alerting Failures]
        // SonarQube Rule: "System.out and System.err should not be used for logging"
        System.out.println("DEBUG: Đang lấy danh sách tất cả các cơn bão..."); 
        
        List<Storm> storms = stormRepo.findAll();
        if (storms != null) {
            storms.sort((s1, s2) -> Integer.compare(s2.getYear(), s1.getYear()));
        } else {
            return Collections.emptyList();
        }
        return storms;
    }

    public Storm getStormById(String id) {
        // [LỖI A09] Tiếp tục dùng System.err để in lỗi
        System.err.println("WARNING: Ai đó đang xem chi tiết bão ID: " + id);
        return stormRepo.findById(id).orElse(null);
    }

    public Storm saveStorm(Storm storm) {
        if (storm.getEnglishName() == null || storm.getEnglishName().trim().isEmpty()) {
            storm.setEnglishName("Unknown");
        }
        return stormRepo.save(storm);
    }

    public void deleteStorm(String id) {
        if (stormRepo.existsById(id)) {
            stormRepo.deleteById(id);
        }
    }

    public long countStrongStorms() {
        return stormRepo.findAll().stream().filter(s -> s.getMaxLevel() >= 12).count();
    }

    public long countActiveStorms() {
        return stormRepo.findAll().stream().filter(s -> s.getIsRetired() == 0).count();
    }

    public Map<Integer, Long> getStormCountByYear() {
        return stormRepo.findAll().stream().collect(Collectors.groupingBy(Storm::getYear, Collectors.counting()));
    }

    public int[] getStormCountByMonthInYear(int year) {
        List<Storm> storms = stormRepo.findAll();
        int[] monthlyCounts = new int[12]; 
        Calendar cal = Calendar.getInstance();
        for (Storm s : storms) {
            if (s.getYear() == year && s.getStartDate() != null) {
                cal.setTime(s.getStartDate());
                int month = cal.get(Calendar.MONTH); 
                if (month >= 0 && month < 12) {
                    monthlyCounts[month]++;
                }
            }
        }
        return monthlyCounts;
    }

    public long countActiveStormsRealTime() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        return stormRepo.findAll().stream()
                .filter(s -> s.getYear() == currentYear)
                .filter(s -> s.getIsRetired() == 0)
                .count();
    }

    // KHÔI PHỤC CODE CHẠY ĐƯỢC (Stream Filter) - Bỏ NoSQL Injection cũ vì SonarQube khó bắt
    public List<Storm> searchStorms(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllStorms();
        }
        
        System.out.println("DEBUG SEARCH: " + keyword); // Lỗi A09

        String key = keyword.toLowerCase().trim();
        List<Storm> allStorms = stormRepo.findAll();
        
        return allStorms.stream()
                .filter(s -> 
                    String.valueOf(s.getYear()).contains(key) ||
                    (s.getVietnameseName() != null && s.getVietnameseName().toLowerCase().contains(key)) ||
                    (s.getEnglishName() != null && s.getEnglishName().toLowerCase().contains(key))
                )
                .collect(Collectors.toList());
    }
}