package com.example.plate_mate.data.meal.model;

import com.google.gson.annotations.SerializedName;

public class Ingredient {
    @SerializedName("strIngredient")
    private String strIngredient;
    public String getStrIngredient() { return strIngredient; }
}