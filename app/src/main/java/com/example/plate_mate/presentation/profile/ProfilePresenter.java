package com.example.plate_mate.presentation.profile;
import com.example.plate_mate.data.auth.datastore.remote.AuthRemoteDataSource;
import com.example.plate_mate.data.auth.model.User;
import com.example.plate_mate.data.auth.repo.AuthRepo;
import com.example.plate_mate.data.meal.datasource.remote.FirebaseSyncDataSource;
import com.example.plate_mate.data.meal.model.FirebaseFavorite;
import com.example.plate_mate.data.meal.model.FirebasePlannedMeal;
import com.example.plate_mate.data.meal.model.Meal;
import com.example.plate_mate.data.meal.model.MealType;
import com.example.plate_mate.data.meal.model.PlannedMeal;
import com.example.plate_mate.data.meal.repository.MealRepository;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ProfilePresenter implements ProfileContract.Presenter {

    private ProfileContract.View view;
    private final AuthRepo authRepo;
    private final AuthRemoteDataSource remoteDataSource;
    private final MealRepository mealRepository;
    private final FirebaseSyncDataSource firebaseSyncDataSource;
    private final CompositeDisposable disposables = new CompositeDisposable();

    public ProfilePresenter(AuthRepo authRepo,
                            AuthRemoteDataSource remoteDataSource,
                            MealRepository mealRepository,
                            FirebaseSyncDataSource firebaseSyncDataSource) {
        this.authRepo = authRepo;
        this.remoteDataSource = remoteDataSource;
        this.mealRepository = mealRepository;
        this.firebaseSyncDataSource = firebaseSyncDataSource;
    }

    @Override
    public void attachView(ProfileContract.View view) {
        this.view = view;
        loadUserProfile();
        boolean isDarkModeEnabled = authRepo.isDarkModeEnabled();
        if (view != null) {
            view.updateDarkModeSwitch(isDarkModeEnabled);
        }
    }

    @Override
    public void detachView() {
        this.view = null;
        disposables.clear();
    }

    @Override
    public void loadUserProfile() {
        if (view == null) return;

        view.showLoading(true);

        try {
            FirebaseUser firebaseUser = remoteDataSource.getCurrentUser();

            if (firebaseUser != null) {
                User user = new User(
                        firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "",
                        firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "User",
                        firebaseUser.getUid()
                );
                view.showUserData(user);
            } else {
                view.showError("No user logged in");
            }

            view.showLoading(false);
        } catch (Exception e) {
            view.showLoading(false);
            view.showError("Failed to load profile: " + e.getMessage());
        }
    }

    @Override
    public void onDarkModeToggled(boolean isEnabled) {
        authRepo.setDarkMode(isEnabled);
        if (view != null) {
            view.showSuccess(isEnabled ? "Dark mode enabled" : "Dark mode disabled");
        }
    }

    @Override
    public void onResetPasswordClicked() {
        if (view == null) return;

        FirebaseUser user = remoteDataSource.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            view.showLoading(true);

            com.google.firebase.auth.FirebaseAuth.getInstance()
                    .sendPasswordResetEmail(user.getEmail())
                    .addOnSuccessListener(aVoid -> {
                        if (view != null) {
                            view.showLoading(false);
                            view.showSuccess("Password reset email sent to " + user.getEmail());
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (view != null) {
                            view.showLoading(false);
                            view.showError("Failed to send reset email: " + e.getMessage());
                        }
                    });
        } else {
            view.showError("No email associated with this account");
        }
    }

    @Override
    public void onLogoutClicked() {
        if (view == null) return;

        view.showLoading(true);

        disposables.add(
                authRepo.logout()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    if (view != null) {
                                        view.showLoading(false);
                                        view.showSuccess("Logged out successfully");
                                        view.navigateToLogin();
                                    }
                                },
                                error -> {
                                    if (view != null) {
                                        view.showLoading(false);
                                        view.showError("Logout failed: " + error.getMessage());
                                    }
                                }
                        )
        );
    }

    @Override
    public void onDownloadDataClicked() {
        if (view == null) return;

        FirebaseUser user = remoteDataSource.getCurrentUser();
        if (user == null) {
            view.showError("No user logged in");
            return;
        }

        String userId = user.getUid();
        view.showLoading(true);
        view.showSyncProgress("Downloading your data from Firebase...");

        disposables.add(
                Observable.zip(
                                downloadFavorites(userId).toObservable(),
                                downloadPlannedMeals(userId).toObservable(),
                                (favCount, plannedCount) -> new int[]{favCount, plannedCount}
                        )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                counts -> {
                                    if (view != null) {
                                        view.showLoading(false);
                                        view.showSyncComplete(counts[0], counts[1]);
                                    }
                                },
                                error -> {
                                    if (view != null) {
                                        view.showLoading(false);
                                        view.showError("Download failed: " + error.getMessage());
                                    }
                                }
                        )
        );
    }

    @Override
    public void onUploadDataClicked() {
        if (view == null) return;

        FirebaseUser user = remoteDataSource.getCurrentUser();
        if (user == null) {
            view.showError("No user logged in");
            return;
        }

        String userId = user.getUid();
        view.showLoading(true);
        view.showSyncProgress("Uploading your data to Firebase...");

        disposables.add(
                Observable.zip(
                                uploadFavorites(userId).toObservable(),
                                uploadPlannedMeals(userId).toObservable(),
                                (favCount, plannedCount) -> new int[]{favCount, plannedCount}
                        )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                counts -> {
                                    if (view != null) {
                                        view.showLoading(false);
                                        view.showUploadComplete(counts[0], counts[1]);
                                    }
                                },
                                error -> {
                                    if (view != null) {
                                        view.showLoading(false);
                                        view.showError("Upload failed: " + error.getMessage());
                                    }
                                }
                        )
        );
    }

    @Override
    public void onFullSyncClicked() {
        if (view == null) return;

        FirebaseUser user = remoteDataSource.getCurrentUser();
        if (user == null) {
            view.showError("No user logged in");
            return;
        }

        String userId = user.getUid();
        view.showLoading(true);
        view.showSyncProgress("Syncing your data...");

        // First upload, then download
        disposables.add(
                uploadFavorites(userId)
                        .flatMap(favCount -> uploadPlannedMeals(userId)) // Chain Single to Single
                        .flatMapObservable(plannedCount -> Observable.zip( // Chain Single to Observable
                                downloadFavorites(userId).toObservable(),
                                downloadPlannedMeals(userId).toObservable(),
                                (favs, planned) -> new int[]{favs, planned}
                        ))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                counts -> {
                                    if (view != null) {
                                        view.showLoading(false);
                                        view.showSuccess("Full sync complete!");
                                    }
                                },
                                error -> {
                                    if (view != null) {
                                        view.showLoading(false);
                                        view.showError("Sync failed: " + error.getMessage());
                                    }
                                }
                        )
        );
    }

    // ==================== DOWNLOAD HELPERS ====================

    private io.reactivex.rxjava3.core.Single<Integer> downloadFavorites(String userId) {
        return firebaseSyncDataSource.fetchUserFavorites(userId)
                .flatMap(firebaseFavorites -> {
                    if (firebaseFavorites.isEmpty()) {
                        return io.reactivex.rxjava3.core.Single.just(0);
                    }

                    return Observable.fromIterable(firebaseFavorites)
                            .flatMapSingle(favorite ->
                                    mealRepository.getMealById(favorite.getMealId())
                                            .flatMap(mealResponse -> {
                                                if (mealResponse.getMeals() != null && !mealResponse.getMeals().isEmpty()) {
                                                    Meal meal = mealResponse.getMeals().get(0);
                                                    return mealRepository.insertFavorite(meal)
                                                            .toSingleDefault(1)
                                                            .onErrorReturnItem(0);
                                                }
                                                return io.reactivex.rxjava3.core.Single.just(0);
                                            })
                            )
                            .reduce(0, Integer::sum);
                });
    }

    private io.reactivex.rxjava3.core.Single<Integer> downloadPlannedMeals(String userId) {
        return firebaseSyncDataSource.fetchUserPlannedMeals(userId)
                .flatMap(firebasePlannedMeals -> {
                    if (firebasePlannedMeals.isEmpty()) {
                        return io.reactivex.rxjava3.core.Single.just(0);
                    }

                    return Observable.fromIterable(firebasePlannedMeals)
                            .flatMapSingle(plannedMeal ->
                                    mealRepository.getMealById(plannedMeal.getMealId())
                                            .flatMap(mealResponse -> {
                                                if (mealResponse.getMeals() != null && !mealResponse.getMeals().isEmpty()) {
                                                    Meal meal = mealResponse.getMeals().get(0);

                                                    PlannedMeal localPlannedMeal = new PlannedMeal(
                                                            plannedMeal.getDate(),
                                                            MealType.valueOf(plannedMeal.getMealType()),
                                                            plannedMeal.getMealId(),
                                                            meal,
                                                            System.currentTimeMillis()
                                                    );

                                                    return mealRepository.insertPlannedMeal(localPlannedMeal)
                                                            .toSingleDefault(1)
                                                            .onErrorReturnItem(0);
                                                }
                                                return io.reactivex.rxjava3.core.Single.just(0);
                                            })
                            )
                            .reduce(0, Integer::sum);
                });
    }

    // ==================== UPLOAD HELPERS ====================

    private io.reactivex.rxjava3.core.Single<Integer> uploadFavorites(String userId) {
        return mealRepository.getAllFavorites()
                .firstOrError()
                .flatMap(localFavorites -> {
                    if (localFavorites.isEmpty()) {
                        return io.reactivex.rxjava3.core.Single.just(0);
                    }

                    List<FirebaseFavorite> firebaseFavorites = new ArrayList<>();
                    for (Meal meal : localFavorites) {
                        firebaseFavorites.add(new FirebaseFavorite(meal.getIdMeal(), userId));
                    }

                    return firebaseSyncDataSource.uploadFavorites(firebaseFavorites, userId)
                            .toSingleDefault(firebaseFavorites.size());
                });
    }

    private io.reactivex.rxjava3.core.Single<Integer> uploadPlannedMeals(String userId) {
        return mealRepository.getAllPlannedMeals()
                .firstOrError()
                .flatMap(localPlannedMeals -> {
                    if (localPlannedMeals.isEmpty()) {
                        return io.reactivex.rxjava3.core.Single.just(0);
                    }

                    List<FirebasePlannedMeal> firebasePlannedMeals = new ArrayList<>();
                    for (PlannedMeal meal : localPlannedMeals) {
                        firebasePlannedMeals.add(new FirebasePlannedMeal(
                                meal.getDate(),
                                meal.getMealId(),
                                userId,
                                meal.getMealType().name()
                        ));
                    }

                    return firebaseSyncDataSource.uploadPlannedMeals(firebasePlannedMeals, userId)
                            .toSingleDefault(firebasePlannedMeals.size());
                });
    }
}