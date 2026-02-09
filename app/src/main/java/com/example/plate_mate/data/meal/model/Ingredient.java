package com.example.plate_mate.data.meal.model;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class Ingredient {
    @SerializedName("strIngredient")
    private String strIngredient;
}