package com.java.m3.demo.model;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
@Document(collection = "storms") // Map với storms.json đã import vào MongoDB
public class Storm {
    @Id
    private String id;

    @Field("vietnamese_name")
    private String vietnameseName;

    @Field("english_name")
    private String englishName;

    private int year;

    @Field("start_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd") // Định dạng khớp với input type="date"
    private Date startDate;

    @Field("end_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    @Field("is_retired")
    private int isRetired;

    @Field("max_level")
    private int maxLevel;

    @Field("max_wind_speed")
    private int maxWindSpeed;

    // Map mảng path trong JSON
    private List<Path> path;

    @Data
    public static class Path {
        @Field("recorded_at")
        private Date recordedAt;
        private double lat;
        private double lng;
        private int level;
        @Field("wind_speed")
        private int windSpeed;
        private String direction;
        private int pressure;
    }
}