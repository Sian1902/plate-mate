package com.example.plate_mate.data.meal.model;

import com.google.firebase.firestore.PropertyName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FirebasePlannedMeal {
    @PropertyName("date")
    private Long date;

    @PropertyName("meal_id")
    private String mealId;

    @PropertyName("user_id")
    private String userId;

    @PropertyName("meal_type")
    private String mealType;

}