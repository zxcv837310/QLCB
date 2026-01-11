
-- Tạo bảng giả STORM cho H2 (Dùng cho demo SQL Injection)
-- Lưu ý: "year" phải để trong ngoặc kép vì là từ khóa của H2
CREATE TABLE IF NOT EXISTS storm (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255),
    "year" INT, 
    start_date TIMESTAMP,
    max_level INT
);

-- Thêm dữ liệu mẫu (Cũng phải bọc "year" trong ngoặc kép)
INSERT INTO storm (id, name, "year", max_level) VALUES ('1', 'Bão Yagi (Fake SQL)', 2024, 14);
INSERT INTO storm (id, name, "year", max_level) VALUES ('2', 'Bão Noru (Fake SQL)', 2023, 12);