package com.example.plate_mate.data.meal.model;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class Category {
    @SerializedName("strCategory")
    private String strCategory;

}
