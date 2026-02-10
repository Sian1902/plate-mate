package com.example.plate_mate.data.database.converters;

import androidx.room.TypeConverter;

import com.example.plate_mate.data.meal.model.MealType;

public class MealTypeConverter {

    @TypeConverter
    public static MealType fromMealTypeString(String value) {
        return value == null ? null : MealType.valueOf(value);
    }

    @TypeConverter
    public static String mealTypeToString(MealType mealType) {
        return mealType == null ? null : mealType.name();
    }
}