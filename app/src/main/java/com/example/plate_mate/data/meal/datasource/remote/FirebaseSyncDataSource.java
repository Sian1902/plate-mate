package com.example.plate_mate.data.meal.datasource.remote;

import com.example.plate_mate.data.meal.model.FirebaseFavorite;
import com.example.plate_mate.data.meal.model.FirebasePlannedMeal;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class FirebaseSyncDataSource {

    private final FirebaseFirestore firestore;
    private static final String FAVORITES_COLLECTION = "favorites";
    private static final String PLANNED_MEALS_COLLECTION = "planned_meals";

    public FirebaseSyncDataSource() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    // DOWNLOAD: Fetch the single document for the user and extract the list
    public Single<List<FirebaseFavorite>> fetchUserFavorites(String userId) {
        return Single.create(emitter -> {
            firestore.collection(FAVORITES_COLLECTION).document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        List<FirebaseFavorite> favorites = new ArrayList<>();
                        if (documentSnapshot.exists()) {
                            List<String> ids = (List<String>) documentSnapshot.get("meal_ids");
                            if (ids != null) {
                                for (String id : ids) {
                                    favorites.add(new FirebaseFavorite(id, userId));
                                }
                            }
                        }
                        emitter.onSuccess(favorites);
                    })
                    .addOnFailureListener(emitter::onError);
        });
    }

    public Single<List<FirebasePlannedMeal>> fetchUserPlannedMeals(String userId) {
        return Single.create(emitter -> {
            firestore.collection(PLANNED_MEALS_COLLECTION).document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        List<FirebasePlannedMeal> plannedMeals = new ArrayList<>();
                        if (documentSnapshot.exists()) {
                            List<Map<String, Object>> list = (List<Map<String, Object>>) documentSnapshot.get("meals");
                            if (list != null) {
                                for (Map<String, Object> map : list) {
                                    FirebasePlannedMeal meal = new FirebasePlannedMeal(
                                            (Long) map.get("date"),
                                            (String) map.get("meal_id"),
                                            userId,
                                            (String) map.get("meal_type")
                                    );
                                    plannedMeals.add(meal);
                                }
                            }
                        }
                        emitter.onSuccess(plannedMeals);
                    })
                    .addOnFailureListener(emitter::onError);
        });
    }

    // UPLOAD: Overwrite the document for the user with the new list
    public Completable uploadFavorites(List<FirebaseFavorite> favorites, String userId) {
        return Completable.create(emitter -> {
            List<String> ids = new ArrayList<>();
            for (FirebaseFavorite fav : favorites) {
                ids.add(fav.getMealId());
            }

            Map<String, Object> data = new HashMap<>();
            data.put("meal_ids", ids);

            firestore.collection(FAVORITES_COLLECTION).document(userId)
                    .set(data) // .set() replaces the whole document, solving the duplicate issue
                    .addOnSuccessListener(aVoid -> emitter.onComplete())
                    .addOnFailureListener(emitter::onError);
        });
    }

    public Completable uploadPlannedMeals(List<FirebasePlannedMeal> plannedMeals, String userId) {
        return Completable.create(emitter -> {
            List<Map<String, Object>> mealList = new ArrayList<>();
            for (FirebasePlannedMeal meal : plannedMeals) {
                Map<String, Object> mealMap = new HashMap<>();
                mealMap.put("meal_id", meal.getMealId());
                mealMap.put("date", meal.getDate());
                mealMap.put("meal_type", meal.getMealType());
                mealList.add(mealMap);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("meals", mealList);

            firestore.collection(PLANNED_MEALS_COLLECTION).document(userId)
                    .set(data)
                    .addOnSuccessListener(aVoid -> emitter.onComplete())
                    .addOnFailureListener(emitter::onError);
        });
    }
}