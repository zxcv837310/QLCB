package com.java.m3.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

@Data
@Document(collection = "damages") // Map với damages.json
public class Damage {
    @Id
    private String id;

    @Field("storm_id")
    private String stormId;

    @Field("province_id")
    private String provinceId;

    @Field("human_loss")
    private HumanLoss humanLoss;

    @Field("asset_loss")
    private AssetLoss assetLoss;

    @Data
    public static class HumanLoss {
        private int dead;
        private int injured;
        private int missing;
    }

    @Data
    public static class AssetLoss {
        @Field("economic_value")
        private long economicValue;
        @Field("houses_destroyed")
        private int housesDestroyed;
        @Field("houses_damaged")
        private int housesDamaged;
        @Field("agriculture_area_ha")
        private double agricultureAreaHa;
    }
}