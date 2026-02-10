package com.example.plate_mate.data.meal.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.TypeConverters;

import com.example.plate_mate.data.database.converters.MealTypeConverter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity(tableName = "planned_meals", primaryKeys = {"date", "meal_type"})
@NoArgsConstructor
@AllArgsConstructor
@TypeConverters(MealTypeConverter.class)
public class PlannedMeal {

    @NonNull
    @ColumnInfo(name = "date")
    private Long date;

    @NonNull
    @ColumnInfo(name = "meal_type")
    private MealType mealType;

    @NonNull
    @ColumnInfo(name = "meal_id")
    private String mealId;

    @Embedded
    private Meal meal;

    @ColumnInfo(name = "created_at")
    private Long createdAt;
}