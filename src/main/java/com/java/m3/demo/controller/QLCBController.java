package com.java.m3.demo.controller;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class QLCBController {

    @Autowired
    private StormService stormService;
    @Autowired
    private DamageRepository damageRepo;
    @Autowired
    private ProvinceRepository provinceRepo;
    @PersistenceContext
    private EntityManager entityManager;

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

        List<Storm> storms = stormService.getAllStorms();
        model.addAttribute("storms", storms);

        // --- TÍNH TOÁN BIỂU ĐỒ (Giữ nguyên logic hiển thị) ---
        long strongCount = 0;
        int[] monthlyData2024 = new int[12];

        for (Storm s : storms) {
            if (s.getMaxLevel() >= 12) strongCount++;

            if (s.getYear() == 2024 && s.getStartDate() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(s.getStartDate());
                int month = cal.get(Calendar.MONTH);
                if (month >= 0 && month < 12) monthlyData2024[month]++;
            }
        }

        model.addAttribute("barData2024", monthlyData2024);
        model.addAttribute("pieValue1", strongCount);
        model.addAttribute("pieValue2", storms.size() - strongCount);

        Map<String, Integer> provinceMap = new LinkedHashMap<>();
        provinceMap.put("Quảng Ninh", 4);
        provinceMap.put("Hải Phòng", 3);
        provinceMap.put("Thanh Hóa", 2);
        model.addAttribute("provinceLabels", provinceMap.keySet().toArray());
        model.addAttribute("provinceData", provinceMap.values().toArray());
        model.addAttribute("activeStormCount", stormService.countActiveStormsRealTime());

        // [ĐÃ XÓA] Đoạn code tạo Cookie không an toàn ở đây
        
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

    // =========================================================
    // TRANG HELP & HÀM PHỤ TRỢ (Để tránh lỗi 500)
    // =========================================================
    
    // Hàm này giúp nạp dữ liệu cần thiết cho trang Help
    private void addHelpPageDefaults(Model model, HttpSession session, HttpServletRequest request) {
        model.addAttribute("currentUser", getCurrentUser(session));
        model.addAttribute("currentURI", request.getRequestURI());
        
        // Nạp danh sách câu hỏi để không bị null
        List<Question> faqList = questionList.stream().filter(Question::isAnswered).collect(Collectors.toList());
        model.addAttribute("faqList", faqList);
        model.addAttribute("pendingList", new ArrayList<>()); // Admin list rỗng
        model.addAttribute("newQuestion", new Question());    // Form object
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

    // ========================================================================
    // [MỚI] 5 LỖI BẢO MẬT OWASP 2025 (THAY THẾ CÁC LỖI CŨ)
    // ========================================================================

    // 1. [A05] INJECTION (SQL Injection)
    // Test: /vuln/sql-injection?name=' OR '1'='1
    @GetMapping("/vuln/sql-injection")
    public String sqlInjection(@RequestParam("name") String name, Model model, HttpSession session, HttpServletRequest request) {
        addHelpPageDefaults(model, session, request);
        
        String sql = "SELECT * FROM storm WHERE name = '" + name + "'"; // Lỗi: Cộng chuỗi
        try {
            List<Object> result = entityManager.createNativeQuery(sql, Storm.class).getResultList();
            model.addAttribute("sqlResult", "Tìm thấy " + result.size() + " bão (Query: " + sql + ")");
        } catch (Exception e) {
            model.addAttribute("sqlResult", "Lỗi Query: " + e.getMessage());
        }
        return "help";
    }

    // 2. [A01] BROKEN ACCESS CONTROL (Thay thế lỗi Logic giảm giá cũ)
    // Nghiệp vụ: Quản lý bão - Chỉ Admin mới được xóa
    // Lỗi: Cho phép bất kỳ ai biết đường dẫn đều có thể xóa hồ sơ bão
    // Test: /vuln/delete-storm?id=STORM_2024_YAGI
    @GetMapping("/vuln/delete-storm")
    public String brokenAccessControl(@RequestParam("id") String id, 
                                      Model model, HttpSession session, HttpServletRequest request) {
        addHelpPageDefaults(model, session, request);
        model.addAttribute("bacResult", "SYSTEM ALERT: Đã thực hiện lệnh XÓA VĨNH VIỄN cơn bão có ID: [" + id + "].\n(Lỗ hổng: Server đã thực thi lệnh nhạy cảm mà không kiểm tra quyền Admin!)");
        return "help";
    }

    // 3. [A07] AUTHENTICATION FAILURES (Mật khẩu yếu, Không Rate Limit)
    // Test: /vuln/quick-login?user=admin&pass=123
    @GetMapping("/vuln/quick-login")
    public String weakAuth(@RequestParam("user") String user, @RequestParam("pass") String pass, 
                           Model model, HttpSession session, HttpServletRequest request) {
        addHelpPageDefaults(model, session, request);
        
        // Lỗi: Hardcode credentials, không có cơ chế chống Brute Force
        if ("admin".equals(user) && "123456".equals(pass)) {
            model.addAttribute("authResult", "Đăng nhập thành công! (Quyền Admin - Mật khẩu quá yếu)");
        } else {
            model.addAttribute("authResult", "Sai mật khẩu! (Hacker có thể thử lại hàng triệu lần)");
        }
        return "help";
    }

    // 4. [A08] SOFTWARE INTEGRITY FAILURES (Insecure Deserialization)
    // Test: /vuln/deserialize?data=...
    @GetMapping("/vuln/deserialize")
    public String insecureDeserialization(@RequestParam("data") String base64Data, 
                                          Model model, HttpSession session, HttpServletRequest request) {
        addHelpPageDefaults(model, session, request);
        
        try {
            byte[] data = Base64.getDecoder().decode(base64Data);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            Object obj = ois.readObject(); // Lỗi: Giải mã object từ nguồn không tin cậy
            ois.close();
            model.addAttribute("integrityResult", "Đã giải mã object: " + obj.toString());
        } catch (Exception e) {
            model.addAttribute("integrityResult", "Lỗi giải mã (An toàn): " + e.getMessage());
        }
        return "help";
    }

    // 5. [A10] MISHANDLING EXCEPTIONS (Lộ Stack Trace)
    // Test: /vuln/error-handling?id=abc
    @GetMapping("/vuln/error-handling")
    public String mishandlingError(@RequestParam("id") String id, 
                                   Model model, HttpSession session, HttpServletRequest request) {
        addHelpPageDefaults(model, session, request);
        
        try {
            int val = Integer.parseInt(id);
            model.addAttribute("errorResult", "Số: " + val);
        } catch (Exception e) {
            // Lỗi: In toàn bộ Stack Trace ra màn hình cho người dùng xem
            StringBuilder stackTrace = new StringBuilder();
            for (StackTraceElement element : e.getStackTrace()) {
                stackTrace.append(element.toString()).append("\n");
            }
            model.addAttribute("errorResult", "SYSTEM CRITICAL ERROR:\n" + e.toString() + "\n" + stackTrace.toString());
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
// Test pipeline 7
// Test pipeline 8
// Test pipeline 9
// Test pipeline 10
// Test pipeline 11
// Test pipeline 12
// Test pipeline 13
// Test pipeline 14
// Test pipeline 15
// Test pipeline 16
// Test pipeline 17
// Test pipeline 18
// Test pipeline 19
// Test pipeline 20
// Test pipeline 21
// Test pipeline 22
// Test pipeline 23
// Test pipeline 24
// Test pipeline 25
// Test pipeline 26