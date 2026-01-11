package com.java.m3.demo.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.java.m3.demo.dto.DamageReportDTO;
import com.java.m3.demo.model.Damage;
import com.java.m3.demo.model.Province;
import com.java.m3.demo.model.Question;
import com.java.m3.demo.model.Storm;
import com.java.m3.demo.model.User; 
import com.java.m3.demo.repository.DamageRepository;
import com.java.m3.demo.repository.ProvinceRepository;
import com.java.m3.demo.service.StormService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class QLCBController {

    @Autowired
    private StormService stormService;
    @Autowired
    private DamageRepository damageRepo;
    @Autowired
    private ProvinceRepository provinceRepo;

    private static List<Question> questionList = new ArrayList<>();
    static {
        Question q1 = new Question();
        q1.setId(UUID.randomUUID().toString());
        q1.setAskerName("Admin Hệ Thống");
        q1.setContent("Làm sao để xem dự báo thời tiết?");
        q1.setAnswer("Bạn có thể xem ở trang Dashboard hoặc mục Bản đồ nhé.");
        q1.setAnswered(true);
        q1.setCreatedAt(new Date());
        questionList.add(q1);
    }

    private User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute("currentUser");
    }

    @GetMapping("/")
    public String index(Model model, HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        model.addAttribute("currentUser", getCurrentUser(session));
        model.addAttribute("currentURI", request.getRequestURI());

        // Lấy danh sách bão
        List<Storm> storms = stormService.getAllStorms();
        model.addAttribute("storms", storms);

        // --- TÍNH TOÁN BIỂU ĐỒ THỦ CÔNG (FIX LỖI MẤT DỮ LIỆU) ---
        long strongCount = 0;
        int[] monthlyData2024 = new int[12];

        for (Storm s : storms) {
            if (s.getMaxLevel() >= 12) strongCount++; // Đếm bão mạnh

            if (s.getYear() == 2024 && s.getStartDate() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(s.getStartDate());
                int month = cal.get(Calendar.MONTH);
                if (month >= 0 && month < 12) monthlyData2024[month]++;
            }
        }

        // Đẩy dữ liệu sang View
        model.addAttribute("barData2024", monthlyData2024);
        model.addAttribute("pieValue1", strongCount);
        model.addAttribute("pieValue2", storms.size() - strongCount);

        // Mockup biểu đồ tỉnh
        Map<String, Integer> provinceMap = new LinkedHashMap<>();
        provinceMap.put("Quảng Ninh", 4);
        provinceMap.put("Hải Phòng", 3);
        provinceMap.put("Thanh Hóa", 2);
        model.addAttribute("provinceLabels", provinceMap.keySet().toArray());
        model.addAttribute("provinceData", provinceMap.values().toArray());
        model.addAttribute("activeStormCount", stormService.countActiveStormsRealTime());

        // ---------------------------------------------------------
        // [LỖI 1] INSECURE COOKIE (Không có HttpOnly/Secure)
        // ---------------------------------------------------------
        Cookie trackingCookie = new Cookie("TrackingID", "user_123456_location_vn");
        // trackingCookie.setHttpOnly(true); // <--- LỖI: Thiếu dòng này
        // trackingCookie.setSecure(true);   // <--- LỖI: Thiếu dòng này
        response.addCookie(trackingCookie);
        
        return "index";
    }

    // [LỖI MỚI - A05: Injection (Path Traversal)]
    // Tính năng: Tải báo cáo
    // SonarQube Rule: "Paths should not be constructed from user-controlled data" (S2083)
    @GetMapping("/report/download")
    @ResponseBody
    public String downloadReport(@RequestParam("filename") String filename, HttpServletResponse response) {
        try {
            // NGUY HIỂM: Ghép chuỗi trực tiếp để tạo đường dẫn file
            // Hacker có thể nhập filename = "../../Windows/win.ini" hoặc "../../etc/passwd"
            String basePath = "C:\\reports\\"; 
            File file = new File(basePath + filename); 

            if (file.exists()) {
                // Giả lập logic đọc file (để SonarQube thấy biến file được sử dụng)
                FileInputStream fis = new FileInputStream(file);
                fis.close();
                return "Đã tìm thấy file: " + file.getAbsolutePath(); 
            } else {
                return "File không tồn tại!";
            }
        } catch (Exception e) {
            e.printStackTrace(); // Lỗi A02
            return "Lỗi server!";
        }
    }

    @GetMapping("/storm/{id}")
    public String stormDetail(@PathVariable("id") String stormId, Model model, HttpSession session, HttpServletRequest request) {
        model.addAttribute("currentUser", getCurrentUser(session));
        model.addAttribute("currentURI", request.getRequestURI());
        Storm storm = stormService.getStormById(stormId);
        if (storm == null) return "redirect:/";

        List<Damage> damages = damageRepo.findByStormId(stormId);
        List<Province> provinces = provinceRepo.findAll();
        Map<String, String> provinceMap = provinces.stream().collect(Collectors.toMap(Province::getId, Province::getName));

        List<DamageReportDTO> reports = new ArrayList<>();
        long totalEconomicLoss = 0;
        int totalDead = 0;
        for (Damage d : damages) {
            DamageReportDTO dto = new DamageReportDTO();
            dto.setProvinceName(provinceMap.getOrDefault(d.getProvinceId(), d.getProvinceId()));
            dto.setHumanLoss(d.getHumanLoss());
            dto.setAssetLoss(d.getAssetLoss());
            reports.add(dto);
            if (d.getAssetLoss() != null) totalEconomicLoss += d.getAssetLoss().getEconomicValue();
            if (d.getHumanLoss() != null) totalDead += d.getHumanLoss().getDead();
        }
        model.addAttribute("storm", storm);
        model.addAttribute("reports", reports);
        model.addAttribute("totalEconomicLoss", totalEconomicLoss);
        model.addAttribute("totalDead", totalDead);
        return "storm-detail";
    }

    @GetMapping("/storm/add")
    public String showAddForm(Model model, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null || !"ADMIN".equals(user.getRole())) return "redirect:/";
        model.addAttribute("storm", new Storm());
        return "storm-form";
    }
    
    @PostMapping("/storm/save")
    public String saveStorm(@ModelAttribute("storm") Storm storm, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null || !"ADMIN".equals(user.getRole())) return "redirect:/";
        stormService.saveStorm(storm);
        return "redirect:/stats";
    }

    @GetMapping("/stats")
    public String showStatsPage(Model model, HttpSession session, HttpServletRequest request) {
        User user = getCurrentUser(session);
        if (user == null) return "redirect:/login";
        model.addAttribute("currentUser", user);
        model.addAttribute("currentURI", request.getRequestURI());
        model.addAttribute("storms", stormService.getAllStorms());
        return "stats";
    }

    @GetMapping("/search")
    public String search(@RequestParam(value = "keyword", required = false) String keyword, 
                         Model model, HttpSession session, HttpServletRequest request) {
        User user = getCurrentUser(session);
        if (user == null) return "redirect:/login";
        model.addAttribute("currentUser", user);
        model.addAttribute("currentURI", request.getRequestURI());
        
        List<Storm> results = stormService.searchStorms(keyword);
        model.addAttribute("storms", results);
        model.addAttribute("searchKeyword", keyword);
        return "stats";
    }
    
    @GetMapping("/charts")
    public String showChartsPage(Model model, HttpSession session, HttpServletRequest request) {
        User user = getCurrentUser(session);
        if (user == null) return "redirect:/login";
        model.addAttribute("currentUser", user);
        model.addAttribute("currentURI", request.getRequestURI());
        model.addAttribute("barData2024", stormService.getStormCountByMonthInYear(2024));
        long strong = stormService.countStrongStorms();
        long total = stormService.getAllStorms().size();
        model.addAttribute("pieValue1", strong);
        model.addAttribute("pieValue2", total - strong);
        return "chart"; 
    }
    
    @GetMapping("/map")
    public String showMapPage(Model model, HttpSession session, HttpServletRequest request) {
        User user = getCurrentUser(session);
        if (user == null) return "redirect:/login";
        model.addAttribute("currentUser", user);
        model.addAttribute("currentURI", request.getRequestURI());
        return "map";
    }

    @GetMapping("/help")
    public String showHelpPage(Model model, HttpSession session, HttpServletRequest request) {
        User user = getCurrentUser(session);
        model.addAttribute("currentUser", user);
        model.addAttribute("currentURI", request.getRequestURI());

        List<Question> faqList = questionList.stream().filter(Question::isAnswered).collect(Collectors.toList());
        model.addAttribute("faqList", faqList);

        if (user != null && "ADMIN".equals(user.getRole())) {
            List<Question> pendingList = questionList.stream().filter(q -> !q.isAnswered()).collect(Collectors.toList());
            model.addAttribute("pendingList", pendingList);
        } else {
            model.addAttribute("pendingList", new ArrayList<>()); 
        }
        model.addAttribute("newQuestion", new Question()); 
        return "help";
    }

    @PostMapping("/help/send")
    public String sendQuestion(@ModelAttribute("newQuestion") Question question, HttpSession session) {
        User user = getCurrentUser(session);
        question.setId(UUID.randomUUID().toString());
        question.setCreatedAt(new Date());
        if (user != null) {
            question.setAskerName(user.getFullName());
        }
        questionList.add(0, question);
        return "redirect:/help?success"; 
    }

    @PostMapping("/help/reply")
    public String replyQuestion(@RequestParam("id") String id, 
                                @RequestParam("answer") String answer,
                                HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/help";
        }

        for (Question q : questionList) {
            if (q.getId().equals(id)) {
                q.setAnswer(answer);
                q.setAnswered(true); 
                break;
            }
        }
        return "redirect:/help?replied";
    }

        // ======================================================
    // CÁC HÀM TẠO LỖI BẢO MẬT ZAP (ĐÃ FIX ĐỂ KHÔNG BỊ LỖI 500)
    // ======================================================

    // Hàm phụ trợ: Nạp dữ liệu mặc định để trang help.html không bị lỗi
    private void addHelpPageDefaults(Model model, HttpSession session, HttpServletRequest request) {
        model.addAttribute("currentUser", getCurrentUser(session));
        model.addAttribute("currentURI", request.getRequestURI());
        // Gửi các object rỗng để Thymeleaf không báo lỗi null
        model.addAttribute("faqList", new ArrayList<>()); 
        model.addAttribute("pendingList", new ArrayList<>());
        model.addAttribute("newQuestion", new Question()); 
    }

    // [LỖI 2] OPEN REDIRECT
    @GetMapping("/external-link")
    public String unsafeRedirect(@RequestParam("url") String url) {
        return "redirect:" + url;
    }

    // [LỖI 3] PATH TRAVERSAL
    @GetMapping("/view-log")
    public String viewLogSystem(@RequestParam("file") String file, Model model, HttpSession session, HttpServletRequest request) {
        addHelpPageDefaults(model, session, request); // <--- QUAN TRỌNG: Thêm dòng này
        
        if (file.contains("../") || file.contains("..\\")) {
            model.addAttribute("logContent", "root:x:0:0:root:/root:/bin/bash (Giả lập nội dung file hệ thống bị lộ)");
        } else {
            model.addAttribute("logContent", "Đang xem nội dung file log: " + file);
        }
        return "help"; 
    }

    // [LỖI 4] REFLECTED XSS
    @GetMapping("/test-xss")
    public String testXss(@RequestParam("msg") String msg, Model model, HttpSession session, HttpServletRequest request) {
        addHelpPageDefaults(model, session, request); // <--- QUAN TRỌNG: Thêm dòng này
        
        model.addAttribute("xssContent", msg);
        return "help"; 
    }

    // [LỖI 5] OS COMMAND INJECTION
    @GetMapping("/test-cmd")
    public String testCmd(@RequestParam("ip") String ip, Model model, HttpSession session, HttpServletRequest request) {
        addHelpPageDefaults(model, session, request); // <--- QUAN TRỌNG: Thêm dòng này
        
        if (ip.contains(";") || ip.contains("|") || ip.contains("&")) {
            model.addAttribute("cmdResult", "uid=0(root) gid=0(root) groups=0(root) (Giả lập: Lệnh hệ thống đã chạy!)");
        } else {
            model.addAttribute("cmdResult", "Pinging " + ip + " ... Success!");
        }
        return "help";
    }
}

// Test pipeline 1
// Test pipeline 2
// Test pipeline 3
// Test pipeline 4
// Test pipeline 5
// Test pipeline 6