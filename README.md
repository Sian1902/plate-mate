# Plate Mate - System Architecture Documentation

## 📱 Project Overview

**Plate Mate** is an Android mobile application designed to help users discover recipes, plan meals, and organize their cooking journey. The app provides a comprehensive meal planning experience with features for browsing recipes, saving favorites, and scheduling meals for the week ahead.

### 🎯 Key Features

- **Recipe Discovery**: Browse and search thousands of recipes from TheMealDB API
- **Smart Filtering**: Filter meals by category (dessert, seafood, etc.), area/cuisine (Italian, Mexican, etc.), and ingredients
- **Favorites Management**: Save favorite recipes locally for quick access
- **Meal Planning**: Plan breakfast, lunch, and dinner for the next 7 days
- **User Authentication**: Sign in with email/password or Google authentication via Firebase
- **Cloud Sync**: Upload and sync favorites and planned meals to Firebase Firestore
- **Dark Mode**: Toggle between light and dark themes for comfortable viewing
- **Offline Support**: Cached data allows browsing recipes without internet connection

### 🏗️ Architecture

The application follows the **Model-View-Presenter (MVP)** architectural pattern with a clean separation of concerns across three main layers:

- **Presentation Layer**: Views (Fragments/Activities) and Presenters handling UI logic
- **Domain Layer**: Repository interfaces and business models
- **Data Layer**: Local (Room Database, SharedPreferences) and Remote (Retrofit, Firebase) data sources

### 🛠️ Tech Stack

**Android Development**
- Java
- Android SDK
- Material Design Components
- RecyclerView for efficient list rendering
- Navigation Component for screen navigation

**Architecture & Patterns**
- MVP (Model-View-Presenter)
- Repository Pattern
- Singleton Pattern
- Observer Pattern

**Data Persistence**
- Room Database (local SQLite storage)
- SharedPreferences (caching and user preferences)

**Networking & APIs**
- Retrofit 2 (REST API client)
- RxJava 3 (reactive programming)
- Gson (JSON serialization)
- TheMealDB API (recipe data)

**Backend & Authentication**
- Firebase Authentication (email/password, Google Sign-In)
- Firebase Firestore (cloud data sync)

**Dependency Injection**
- Manual DI (constructor injection)

### 📊 Data Flow

1. **Initial Load**: App preloads categories, areas, ingredients, and sample meals on first launch
2. **Caching**: Data is cached in SharedPreferences for offline access
3. **User Actions**: Favorites and planned meals are stored in Room Database
4. **Cloud Sync**: Users can upload their local data to Firebase for backup/sync across devices

### 👥 User Journey

1. **Onboarding**: First-time users see onboarding screens introducing app features
2. **Authentication**: Users can sign in or continue as guest (limited features)
3. **Home**: Browse featured meals, filter by category/area/ingredient, search recipes
4. **Meal Details**: View full recipe with ingredients, measurements, and cooking instructions
5. **Saved**: Access all favorited recipes in one place
6. **Planner**: Schedule meals for the next 7 days (breakfast, lunch, dinner)
7. **Profile**: Manage account, toggle dark mode, sync data to cloud, sign out

---

## 📐 Architecture Diagrams

## 1. High-Level Layered Architecture

```mermaid
graph TB
    subgraph "Presentation Layer"
        UI[Views & Fragments]
        PRES[Presenters]
    end
    
    subgraph "Domain Layer"
        REPO[Repositories]
        MODEL[Models]
    end
    
    subgraph "Data Layer"
        LOCAL[Local Storage]
        REMOTE[Remote APIs]
    end
    
    subgraph "External"
        API[TheMealDB API]
        FB[Firebase Services]
    end
    
    UI --> PRES
    PRES --> REPO
    REPO --> MODEL
    REPO --> LOCAL
    REPO --> REMOTE
    REMOTE --> API
    REMOTE --> FB
    
    style UI fill:#e1f5ff
    style PRES fill:#fff3e0
    style REPO fill:#f3e5f5
    style MODEL fill:#f3e5f5
    style LOCAL fill:#e8f5e9
    style REMOTE fill:#e8f5e9
    style API fill:#ffebee
    style FB fill:#ffebee
```

## 2. MVP Pattern - Home Feature

```mermaid
graph LR
    HF[HomeFragment]
    HP[HomePresenterImp]
    MR[MealRepository]
    
    HF -->|implements| HV[HomeView Interface]
    HP -->|updates| HV
    HF -->|creates & uses| HP
    HP -->|uses| MR
    
    style HF fill:#e1f5ff
    style HP fill:#fff3e0
    style MR fill:#f3e5f5
```

## 3. MVP Pattern - Saved Feature

```mermaid
graph LR
    SF[SavedFragment]
    SP[SavedPresenterImp]
    MR[MealRepository]
    
    SF -->|implements| SV[SavedView Interface]
    SP -->|updates| SV
    SF -->|creates & uses| SP
    SP -->|uses| MR
    
    style SF fill:#e1f5ff
    style SP fill:#fff3e0
    style MR fill:#f3e5f5
```

## 4. MVP Pattern - Planner Feature

```mermaid
graph LR
    PF[PlannerFragment]
    PP[PlannerPresenterImp]
    MR[MealRepository]
    
    PF -->|implements| PV[PlannerView Interface]
    PP -->|updates| PV
    PF -->|creates & uses| PP
    PP -->|uses| MR
    
    style PF fill:#e1f5ff
    style PP fill:#fff3e0
    style MR fill:#f3e5f5
```

## 5. MVP Pattern - Profile Feature

```mermaid
graph LR
    PRF[ProfileFragment]
    PRP[ProfilePresenterImp]
    AR[AuthRepository]
    MR[MealRepository]
    FS[FirebaseSyncDataSource]
    
    PRF -->|implements| PRV[ProfileView Interface]
    PRP -->|updates| PRV
    PRF -->|creates & uses| PRP
    PRP -->|uses| AR
    PRP -->|uses| MR
    PRP -->|uses| FS
    
    style PRF fill:#e1f5ff
    style PRP fill:#fff3e0
    style AR fill:#f3e5f5
    style MR fill:#f3e5f5
    style FS fill:#e8f5e9
```

## 6. Repository Layer - MealRepository

```mermaid
graph TB
    MR[MealRepoImp]
    
    subgraph "Local Data Sources"
        FDS[FavoriteLocalDataStore]
        PDS[PlannedMealLocalDataStore]
        SPM[MealSharedPrefManager]
    end
    
    subgraph "Remote Data Sources"
        RDS[MealRemoteDataSource]
    end
    
    MR --> FDS
    MR --> PDS
    MR --> SPM
    MR --> RDS
    
    FDS --> FD[FavoriteDao]
    PDS --> PD[PlannedMealDao]
    FD --> DB[(Room Database)]
    PD --> DB
    SPM --> SP[(SharedPreferences)]
    RDS --> API[TheMealDB API]
    
    style MR fill:#f3e5f5
    style FDS fill:#e8f5e9
    style PDS fill:#e8f5e9
    style SPM fill:#e8f5e9
    style RDS fill:#e8f5e9
```

## 7. Repository Layer - AuthRepository

```mermaid
graph TB
    AR[AuthRepoImp]
    
    subgraph "Local Storage"
        APM[AuthPrefManager]
    end
    
    subgraph "Remote Storage"
        ARS[AuthRemoteDataSource]
    end
    
    AR --> APM
    AR --> ARS
    
    APM --> SP[(SharedPreferences)]
    ARS --> FB[Firebase Auth]
    
    style AR fill:#f3e5f5
    style APM fill:#e8f5e9
    style ARS fill:#e8f5e9
```

---

## 8. Database Schema - Core Tables

```mermaid
erDiagram
    MEAL {
        string idMeal PK
        string strMeal
        string strCategory
        string strArea
        string strInstructions
        string strMealThumb
        string strYoutube
    }
    
    PLANNED_MEAL {
        long date PK
        enum meal_type PK
        string meal_id FK
        long created_at
    }
    
    MEAL ||--o{ PLANNED_MEAL : "embedded in"
    PLANNED_MEAL }o--|| MEAL : "references"
```

## 9. Database Schema - Meal Ingredients (20 fields)

```mermaid
erDiagram
    MEAL {
        string idMeal PK
        string strIngredient1
        string strIngredient2
        string strIngredient3
        string strIngredient4
        string strIngredient5
        string strIngredient6
        string strIngredient7
        string strIngredient8
        string strIngredient9
        string strIngredient10
        string strIngredient11
        string strIngredient12
        string strIngredient13
        string strIngredient14
        string strIngredient15
        string strIngredient16
        string strIngredient17
        string strIngredient18
        string strIngredient19
        string strIngredient20
    }
```

## 10. Database Schema - Meal Measures (20 fields)

```mermaid
erDiagram
    MEAL {
        string idMeal PK
        string strMeasure1
        string strMeasure2
        string strMeasure3
        string strMeasure4
        string strMeasure5
        string strMeasure6
        string strMeasure7
        string strMeasure8
        string strMeasure9
        string strMeasure10
        string strMeasure11
        string strMeasure12
        string strMeasure13
        string strMeasure14
        string strMeasure15
        string strMeasure16
        string strMeasure17
        string strMeasure18
        string strMeasure19
        string strMeasure20
    }
```

## 11. Database Schema - Firebase Collections

```mermaid
erDiagram
    FIREBASE_FAVORITES {
        string user_id PK
        array meal_ids
    }
    
    FIREBASE_PLANNED_MEALS {
        string user_id PK
        array meals
    }
    
    SHARED_PREFERENCES {
        string key PK
        string value
    }
```

---

## 12. UML Class Diagram - Domain Models

```mermaid
classDiagram
    class Meal {
        <<entity>>
        -String idMeal
        -String strMeal
        -String strCategory
        -String strArea
        -String strInstructions
        -String strMealThumb
        -String strYoutube
        -String strIngredient1-20
        -String strMeasure1-20
        +getId() String
        +getName() String
        +getCategory() String
    }
    
    class PlannedMeal {
        <<entity>>
        -Long date
        -MealType mealType
        -String mealId
        -Meal meal
        -Long createdAt
        +getDate() Long
        +getMealType() MealType
        +getMeal() Meal
    }
    
    class User {
        -String email
        -String name
        -String uid
        +getEmail() String
        +getName() String
    }
    
    class MealType {
        <<enumeration>>
        BREAKFAST
        LUNCH
        DINNER
    }
    
    class Category {
        -String idCategory
        -String strCategory
        -String strCategoryThumb
    }
    
    class Ingredient {
        -String idIngredient
        -String strIngredient
    }
    
    class Area {
        -String strArea
    }
    
    PlannedMeal --> Meal : embeds
    PlannedMeal --> MealType : uses
```

## 13. UML - Home Feature

```mermaid
classDiagram
    class HomeView {
        <<interface>>
        +setupUi(List~Meal~, Meal)
        +setFilterOptions(List, List, List)
        +updateFavorites(Set~String~)
        +showError(String)
    }
    
    class HomePresenter {
        <<interface>>
        +loadHomeData()
        +filterMeals(String, String, String)
        +searchMeals(String)
        +toggleFavorite(Meal)
    }
    
    class HomePresenterImp {
        -MealRepository mealRepo
        -HomeView homeView
        +loadHomeData()
        +filterMeals(...)
        +toggleFavorite(Meal)
    }
    
    class HomeFragment {
        -HomePresenterImp presenter
        +setupUi(List~Meal~, Meal)
    }
    
    HomeFragment ..|> HomeView
    HomePresenterImp ..|> HomePresenter
    HomeFragment --> HomePresenterImp
    HomePresenterImp --> HomeView
```

## 14. UML - Saved Feature

```mermaid
classDiagram
    class SavedView {
        <<interface>>
        +showFavorites(List~Meal~)
        +showEmptyState()
        +showError(String)
    }
    
    class SavedPresenter {
        <<interface>>
        +loadFavorites()
        +toggleFavorite(Meal)
    }
    
    class SavedPresenterImp {
        -MealRepository mealRepo
        +loadFavorites()
        +toggleFavorite(Meal)
    }
    
    class SavedFragment {
        -SavedPresenterImp presenter
        +showFavorites(List~Meal~)
    }
    
    SavedFragment ..|> SavedView
    SavedPresenterImp ..|> SavedPresenter
    SavedFragment --> SavedPresenterImp
    SavedPresenterImp --> SavedView
```

## 15. UML - Planner Feature

```mermaid
classDiagram
    class PlannerView {
        <<interface>>
        +showPlannedMeals(List~PlannedMeal~)
        +showLoading()
        +showError(String)
    }
    
    class PlannerPresenter {
        <<interface>>
        +loadPlannedMealsForNextSevenDays()
        +loadPlannedMealsForDate(Long)
        +cleanupOldMeals()
    }
    
    class PlannerPresenterImp {
        -MealRepository repository
        +loadPlannedMealsForNextSevenDays()
        +cleanupOldMeals()
    }
    
    class PlannerFragment {
        -PlannerPresenterImp presenter
        +showPlannedMeals(List~PlannedMeal~)
    }
    
    PlannerFragment ..|> PlannerView
    PlannerPresenterImp ..|> PlannerPresenter
    PlannerFragment --> PlannerPresenterImp
    PlannerPresenterImp --> PlannerView
```

## 16. UML - Profile Feature

```mermaid
classDiagram
    class ProfileView {
        <<interface>>
        +showUserData(User)
        +showLoading(boolean)
        +updateDarkModeSwitch(boolean)
    }
    
    class ProfilePresenter {
        <<interface>>
        +loadUserProfile()
        +onDarkModeToggled(boolean)
        +onLogoutClicked()
        +onUploadDataClicked()
    }
    
    class ProfilePresenterImp {
        -AuthRepo authRepo
        -MealRepository mealRepo
        -FirebaseSyncDataSource firebaseSync
        +loadUserProfile()
        +onUploadDataClicked()
    }
    
    class ProfileFragment {
        -ProfilePresenterImp presenter
        +showUserData(User)
    }
    
    ProfileFragment ..|> ProfileView
    ProfilePresenterImp ..|> ProfilePresenter
    ProfileFragment --> ProfilePresenterImp
    ProfilePresenterImp --> ProfileView
```

## 17. UML - Repository Implementation

```mermaid
classDiagram
    class MealRepository {
        <<interface>>
        +getCachedSplashData()
        +getAllFavorites()
        +insertFavorite(Meal)
        +getPlannedMealsForNextSevenDays()
    }
    
    class MealRepoImp {
        -MealRemoteDataSource remoteDataSource
        -FavoriteLocalDataStore favoriteStore
        -PlannedMealLocalDataStore plannedStore
        -MealSharedPrefManager prefManager
        +getInstance(Context) MealRepoImp
    }
    
    class AuthRepo {
        <<interface>>
        +login(String, String)
        +register(String, String, String)
        +logout()
    }
    
    class AuthRepoImp {
        -AuthRemoteDataSource remoteDataSource
        -AuthPrefManager localDataSource
    }
    
    MealRepoImp ..|> MealRepository
    AuthRepoImp ..|> AuthRepo
```

## 18. UML - Local Data Sources

```mermaidclassDiagram
    class MealsDatabase {
        <<abstract>>
        +favoriteDao() FavoriteDao
        +plannedMealDao() PlannedMealDao
    }

    class FavoriteDao {
        <<interface>>
        +insertFavorite(meal: Meal) : void
        +getAllFavorites() : List~Meal~
        +deleteFavorite(mealId: String) : void
        +deleteAllFavorites() : void
    }

    class PlannedMealDao {
        <<interface>>
        +insertPlannedMeal(plannedMeal: PlannedMeal) : void
        +getPlannedMealsInRange(start: Long, end: Long) : List~PlannedMeal~
        +deletePlannedMeal(id: String) : void
        +deleteOldPlannedMeals(before: Long) : void
    }

    class FavoriteLocalDataSource {
        -favoriteDao: FavoriteDao
        +insertFavorite(meal: Meal)
        +getAllFavorites()
        +deleteFavorite(mealId: String)
    }

    class PlannedMealLocalDataSource {
        -plannedMealDao: PlannedMealDao
        +insertPlannedMeal(plannedMeal: PlannedMeal)
        +getPlannedMealsInRange(start: Long, end: Long)
        +deleteOldPlannedMeals(before: Long)
    }

    class MealSharedPrefManager {
        -sharedPreferences: SharedPreferences
        +saveInitialData(data: InitialMealData)
        +getCachedInitialData() : InitialMealData
        +clearInitialData()
    }

    class AuthPrefManager {
        -sharedPreferences: SharedPreferences
        +saveUserSession(userId: String, token: String)
        +getUserId() : String
        +isLoggedIn() : boolean
        +clearSession()
    }

    MealsDatabase --> FavoriteDao
    MealsDatabase --> PlannedMealDao
    FavoriteLocalDataSource --> FavoriteDao
    PlannedMealLocalDataSource --> PlannedMealDao

```

## 19. UML - Remote Data Sources

```mermaidclassDiagram
    class MealRemoteDataSource {
        -mealService: MealService
        +listCategories() : Single~CategoryResponse~
        +filterByCategory(category: String) : Single~MealResponse~
        +searchMealByName(name: String) : Single~MealResponse~
        +getMealById(id: String) : Single~MealDetailsResponse~
        +getRandomMeal() : Single~MealDetailsResponse~
    }

    class MealService {
        <<interface>>
        +listCategories()
        +searchByCategory(category: String)
        +searchByName(name: String)
        +getMealById(id: String)
        +getRandomMeal()
    }

    class RetrofitClient {
        +getMealApiService() : MealService
    }

    class AuthRemoteDataSource {
        -firebaseAuth: FirebaseAuth
        +signIn(email: String, password: String)
        +signUp(email: String, password: String)
        +logout()
        +getCurrentUser() : FirebaseUser
    }

    class FirebaseSyncDataSource {
        -firestore: FirebaseFirestore
        +fetchUserFavorites(userId: String)
        +uploadFavorites(favorites: List~Meal~, userId: String)
        +fetchUserPlannedMeals(userId: String)
        +uploadPlannedMeals(plannedMeals: List~PlannedMeal~, userId: String)
    }

    MealRemoteDataSource --> MealService
    RetrofitClient --> MealService
    AuthRemoteDataSource --> FirebaseAuth
    FirebaseSyncDataSource --> FirebaseFirestore

```

## 20. Data Flow - Add Favorite

```mermaid
sequenceDiagram
    participant User
    participant HomeFragment
    participant HomePresenter
    participant MealRepo
    participant FavoriteStore
    participant RoomDB
    
    User->>HomeFragment: Click Favorite Button
    HomeFragment->>HomePresenter: toggleFavorite(meal)
    HomePresenter->>MealRepo: insertFavorite(meal)
    MealRepo->>FavoriteStore: insertFavorite(meal)
    FavoriteStore->>RoomDB: insert(meal)
    RoomDB-->>FavoriteStore: success
    FavoriteStore-->>MealRepo: Completable.complete()
    MealRepo-->>HomePresenter: success
    HomePresenter->>HomePresenter: update favoriteMealIds
    HomePresenter->>HomeFragment: updateFavorites(Set)
    HomeFragment->>User: Update UI (heart icon)
```

## 21. Data Flow - Load Planned Meals

```mermaid
sequenceDiagram
    participant User
    participant PlannerFragment
    participant PlannerPresenter
    participant MealRepo
    participant PlannedStore
    participant RoomDB
    
    User->>PlannerFragment: Open Planner Screen
    PlannerFragment->>PlannerPresenter: loadPlannedMealsForNextSevenDays()
    PlannerPresenter->>PlannerFragment: showLoading()
    PlannerPresenter->>MealRepo: getPlannedMealsForNextSevenDays()
    MealRepo->>PlannedStore: getPlannedMealsForNextSevenDays()
    PlannedStore->>RoomDB: query date range
    RoomDB-->>PlannedStore: List<PlannedMeal>
    PlannedStore-->>MealRepo: Observable<List<PlannedMeal>>
    MealRepo-->>PlannerPresenter: data
    PlannerPresenter->>PlannerFragment: showPlannedMeals(list)
    PlannerFragment->>User: Display meals by date
```

## 22. Data Flow - Firebase Sync

```mermaid
sequenceDiagram
    participant User
    participant ProfileFragment
    participant ProfilePresenter
    participant MealRepo
    participant FirebaseSync
    participant Firestore
    
    User->>ProfileFragment: Click Upload Data
    ProfileFragment->>ProfilePresenter: onUploadDataClicked()
    ProfilePresenter->>MealRepo: getAllFavorites()
    MealRepo-->>ProfilePresenter: List<Meal>
    ProfilePresenter->>MealRepo: getAllPlannedMeals()
    MealRepo-->>ProfilePresenter: List<PlannedMeal>
    ProfilePresenter->>ProfilePresenter: convert to Firebase models
    ProfilePresenter->>FirebaseSync: uploadFavorites(list, userId)
    FirebaseSync->>Firestore: set document
    Firestore-->>FirebaseSync: success
    ProfilePresenter->>FirebaseSync: uploadPlannedMeals(list, userId)
    FirebaseSync->>Firestore: set document
    Firestore-->>FirebaseSync: success
    ProfilePresenter->>ProfileFragment: showUploadComplete(counts)
    ProfileFragment->>User: Show success message
```
