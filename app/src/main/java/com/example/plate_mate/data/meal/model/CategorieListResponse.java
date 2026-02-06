package com.example.plate_mate.data.meal.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CategorieListResponse {

    @SerializedName("meals")
    private List<Category> meal;

    public List<Category> getMeal() {
        return meal;
    }

}
