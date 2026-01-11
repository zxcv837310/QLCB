package com.java.m3.demo.dto;

import com.java.m3.demo.model.Damage;

import lombok.Data;

@Data
public class DamageReportDTO {
    private String provinceName;
    private Damage.HumanLoss humanLoss;
    private Damage.AssetLoss assetLoss;
}