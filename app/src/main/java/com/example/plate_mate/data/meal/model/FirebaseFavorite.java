package com.example.plate_mate.data.meal.model;


import com.google.firebase.firestore.PropertyName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FirebaseFavorite {
    @PropertyName("meal_id")
    private String mealId;

    @PropertyName("user_id")
    private String userId;


}