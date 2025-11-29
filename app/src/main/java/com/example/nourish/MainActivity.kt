package com.example.nourish

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.FloatingActionButton
import androidx.compose.foundation.BorderStroke
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material3.CircularProgressIndicator
import com.google.firebase.auth.EmailAuthProvider
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuthException

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardActions

import androidx.compose.ui.text.input.ImeAction

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.geometry.Offset
import kotlin.random.Random

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.json.JSONArray

// ----------------------------- DATA MODELS -----------------------------

data class Recipe(
    val id: Int,
    val name: String,
    val imageRes: Int,
    val prepTime: String,
    val category: String, // e.g. "Breakfast", "Dessert", "Veg"
    val ingredients: List<String>,
    val steps: List<String>,
    val difficulty: String    // 🔥 NEW: "Easy" / "Medium" / "Hard"
)

data class Course(
    val id: Int,
    val title: String,
    val bannerRes: Int,
    val duration: String,
    val level: String,
    val instructor: String,
    val startDate: String,
    val outline: List<String>
)


enum class BottomTab { HOME, SEARCH, COURSES, COOKAI }

// ----------------------------- SAMPLE DATA -----------------------------

fun sampleRecipes(): List<Recipe> = listOf(
    Recipe(
        id = 1,
        name = "Creamy Pasta Alfredo",
        imageRes = R.drawable.recipe_pasta,
        prepTime = "25 min",
        category = "Dinner",
        ingredients = listOf(
            "200g fettuccine pasta",
            "2 tbsp butter",
            "2 cloves garlic, minced",
            "1 cup heavy cream",
            "1/2 cup grated parmesan",
            "Salt & pepper to taste"
        ),
        steps = listOf(
            "Boil pasta until al dente, then drain.",
            "In a pan, melt butter and sauté garlic.",
            "Add cream and simmer on low heat.",
            "Stir in parmesan until sauce thickens.",
            "Add pasta, toss well, season and serve hot."
        ),
        difficulty = "Medium"
    ),
    Recipe(
        id = 2,
        name = "Fresh Garden Salad",
        imageRes = R.drawable.recipe_salad,
        prepTime = "10 min",
        category = "Vegetarian",
        ingredients = listOf(
            "Lettuce leaves",
            "Cherry tomatoes",
            "Cucumber slices",
            "Olive oil",
            "Lemon juice",
            "Salt & pepper"
        ),
        steps = listOf(
            "Wash and chop all vegetables.",
            "Add to a large bowl.",
            "Drizzle olive oil and lemon juice.",
            "Season with salt and pepper.",
            "Toss gently and serve fresh."
        ),
        difficulty = "Easy"
    ),
    Recipe(
        id = 3,
        name = "Fluffy Pancakes",
        imageRes = R.drawable.recipe_pancakes,
        prepTime = "20 min",
        category = "Breakfast",
        ingredients = listOf(
            "1 cup all-purpose flour",
            "2 tbsp sugar",
            "1 tsp baking powder",
            "1 cup milk",
            "1 egg",
            "1 tbsp melted butter"
        ),
        steps = listOf(
            "Mix dry ingredients in a bowl.",
            "Whisk milk, egg, and butter separately.",
            "Combine wet and dry ingredients gently.",
            "Heat a pan and pour batter.",
            "Cook until bubbles appear, flip and cook the other side."
        ),
        difficulty = "Easy"
    )
)

fun sampleCourses(): List<Course> = listOf(
    Course(
        id = 1,
        title = "Baking Basics: Cookies & Cakes",
        bannerRes = R.drawable.recipe_pancakes,
        duration = "4 weeks",
        level = "Easy",
        instructor = "Chef Ayesha Rahman",
        startDate = "10 December 2025",
        outline = listOf(
            "Week 1: Intro to baking & oven safety",
            "Week 2: Classic cookies",
            "Week 3: Basic sponge cakes",
            "Week 4: Decorating & presentation"
        )
    ),
    Course(
        id = 2,
        title = "Mastering Bangladeshi Curries",
        bannerRes = R.drawable.recipe_pasta,
        duration = "6 weeks",
        level = "Medium",
        instructor = "Chef Mahmud Hasan",
        startDate = "20 December 2025",
        outline = listOf(
            "Spice fundamentals",
            "Chicken & beef curries",
            "Fish & seafood curries",
            "Vegetarian curries",
            "Meal planning",
            "Final live cooking session"
        )
    ),
    Course(
        id = 3,
        title = "Advanced Pastry & Desserts",
        bannerRes = R.drawable.recipe_salad,
        duration = "8 weeks",
        level = "Advanced",
        instructor = "Chef Nabila Karim",
        startDate = "5 January 2026",
        outline = listOf(
            "Pâte à choux & éclairs",
            "Tarts & pies",
            "Layered desserts",
            "Chocolate work",
            "Plated dessert design"
        )
    )
)

// ----------------------------- ACTIVITY -----------------------------

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Initialize Firebase
        FirebaseApp.initializeApp(this)

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFFFF7043),
                    secondary = Color(0xFFFFCC80),
                    background = Color(0xFFFFFBF5),
                    onPrimary = Color.White
                )
            ) {
                CookingApp()
            }
        }
    }
}

// ----------------------------- NAVIGATION ROOT -----------------------------

// REPLACE YOUR CookingApp FUNCTION (around line 240) WITH THIS UPDATED VERSION:

// ----------------------------- NAVIGATION ROOT -----------------------------

// REPLACE YOUR ENTIRE CookingApp FUNCTION (starting around line 265) WITH THIS:

// ----------------------------- NAVIGATION ROOT -----------------------------

// REPLACE YOUR ENTIRE CookingApp FUNCTION WITH THIS FIXED VERSION:

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingApp() {
    val navController = rememberNavController()
    val context = LocalContext.current

    var recipes by remember { mutableStateOf(sampleRecipes()) }
    LaunchedEffect(Unit) {
        recipes = loadRecipesFromJson(context)
    }

    // ✅ State for saved recipes and enrolled courses
    var savedRecipeIds by remember { mutableStateOf(setOf<Int>()) }
    var enrolledCourseIds by remember { mutableStateOf(setOf<Int>()) }
    var isLoadingUserData by remember { mutableStateOf(true) }
    var currentUserId by remember { mutableStateOf<String?>(null) }  // ✅ Track current user

    val courses = remember { sampleCourses() }
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // ✅ Load user data whenever the current user changes
    LaunchedEffect(currentUserId) {
        val user = auth.currentUser
        val uid = user?.uid

        // Clear data when user logs out
        if (uid == null) {
            savedRecipeIds = setOf()
            enrolledCourseIds = setOf()
            isLoadingUserData = false
            android.util.Log.d("CookingApp", "User logged out - cleared data")
            return@LaunchedEffect
        }

        // Load data for the current user
        android.util.Log.d("CookingApp", "Loading data for user: $uid")

        // Load saved recipes
        db.collection("users").document(uid).collection("savedRecipes")
            .get()
            .addOnSuccessListener { documents ->
                val ids = documents.mapNotNull { it.getLong("recipeId")?.toInt() }.toSet()
                savedRecipeIds = ids
                android.util.Log.d("CookingApp", "Loaded saved recipes for user $uid: $ids")
            }
            .addOnFailureListener { e ->
                android.util.Log.e("CookingApp", "Error loading saved recipes", e)
            }

        // Load enrolled courses
        db.collection("users").document(uid).collection("enrolledCourses")
            .get()
            .addOnSuccessListener { documents ->
                val ids = documents.mapNotNull { it.getLong("courseId")?.toInt() }.toSet()
                enrolledCourseIds = ids
                android.util.Log.d("CookingApp", "Loaded enrolled courses for user $uid: $ids")
                isLoadingUserData = false
            }
            .addOnFailureListener { e ->
                android.util.Log.e("CookingApp", "Error loading enrolled courses", e)
                isLoadingUserData = false
            }
    }

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onGetStarted = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    // ✅ Update currentUserId to trigger data reload
                    currentUserId = auth.currentUser?.uid
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onSignUpClick = {
                    navController.navigate("signup")
                }
            )
        }

        composable("signup") {
            SignUpScreen(
                onSignUpComplete = {
                    navController.popBackStack()
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable("main") {
            MainScreen(
                navController = navController,
                recipes = recipes,
                savedRecipeIds = savedRecipeIds,
                onLogout = {
                    // ✅ Clear current user ID to trigger data reload on next login
                    currentUserId = null
                },
                onToggleSave = { id ->
                    val user = auth.currentUser
                    val uid = user?.uid

                    if (uid != null) {
                        val recipeRef = db.collection("users")
                            .document(uid)
                            .collection("savedRecipes")
                            .document(id.toString())

                        if (id in savedRecipeIds) {
                            // Remove from Firestore
                            recipeRef.delete()
                                .addOnSuccessListener {
                                    savedRecipeIds = savedRecipeIds - id
                                    android.util.Log.d("CookingApp", "Recipe $id removed from saved for user $uid")
                                }
                                .addOnFailureListener { e ->
                                    android.util.Log.e("CookingApp", "Error removing recipe", e)
                                }
                        } else {
                            // Add to Firestore
                            val data = hashMapOf(
                                "recipeId" to id,
                                "savedAt" to System.currentTimeMillis()
                            )
                            recipeRef.set(data)
                                .addOnSuccessListener {
                                    savedRecipeIds = savedRecipeIds + id
                                    android.util.Log.d("CookingApp", "Recipe $id added to saved for user $uid")
                                }
                                .addOnFailureListener { e ->
                                    android.util.Log.e("CookingApp", "Error saving recipe", e)
                                }
                        }
                    } else {
                        android.util.Log.e("CookingApp", "User not logged in - cannot save recipe")
                    }
                },
                onRecipeClick = { id ->
                    navController.navigate("detail/$id")
                },
                courses = courses,
                onCourseClick = { courseId ->
                    navController.navigate("courseDetail/$courseId")
                },
                enrolledCourseIds = enrolledCourseIds
            )
        }

        // Recipe details
        composable(
            route = "detail/{recipeId}",
            arguments = listOf(
                navArgument("recipeId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getInt("recipeId") ?: return@composable
            val recipe = recipes.first { it.id == recipeId }
            val isSaved = savedRecipeIds.contains(recipeId)

            RecipeDetailScreen(
                recipe = recipe,
                isSaved = isSaved,
                onToggleSave = {
                    val user = auth.currentUser
                    val uid = user?.uid

                    if (uid != null) {
                        val recipeRef = db.collection("users")
                            .document(uid)
                            .collection("savedRecipes")
                            .document(recipeId.toString())

                        if (recipeId in savedRecipeIds) {
                            recipeRef.delete()
                                .addOnSuccessListener {
                                    savedRecipeIds = savedRecipeIds - recipeId
                                    android.util.Log.d("CookingApp", "Recipe $recipeId removed for user $uid")
                                }
                                .addOnFailureListener { e ->
                                    android.util.Log.e("CookingApp", "Error removing recipe", e)
                                }
                        } else {
                            val data = hashMapOf(
                                "recipeId" to recipeId,
                                "savedAt" to System.currentTimeMillis()
                            )
                            recipeRef.set(data)
                                .addOnSuccessListener {
                                    savedRecipeIds = savedRecipeIds + recipeId
                                    android.util.Log.d("CookingApp", "Recipe $recipeId saved for user $uid")
                                }
                                .addOnFailureListener { e ->
                                    android.util.Log.e("CookingApp", "Error saving recipe", e)
                                }
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Course details
        composable(
            route = "courseDetail/{courseId}",
            arguments = listOf(
                navArgument("courseId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getInt("courseId") ?: return@composable
            val course = courses.first { it.id == courseId }
            val isEnrolled = enrolledCourseIds.contains(courseId)

            CourseDetailScreen(
                course = course,
                isEnrolled = isEnrolled,
                onBack = { navController.popBackStack() },
                onEnrollClick = { id ->
                    if (!isEnrolled) {
                        navController.navigate("courseEnroll/$id")
                    }
                }
            )
        }

        // Course enroll page (form)
        composable(
            route = "courseEnroll/{courseId}",
            arguments = listOf(
                navArgument("courseId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getInt("courseId") ?: return@composable
            val course = courses.first { it.id == courseId }

            CourseEnrollScreen(
                course = course,
                onBack = { navController.popBackStack() },
                onEnrollSuccess = { enrollmentData ->
                    val user = auth.currentUser
                    val uid = user?.uid

                    if (uid != null) {
                        // Save enrollment data to Firestore
                        val enrollmentRef = db.collection("users")
                            .document(uid)
                            .collection("enrolledCourses")
                            .document(courseId.toString())

                        val data = hashMapOf(
                            "courseId" to courseId,
                            "courseTitle" to course.title,
                            "fullName" to enrollmentData["fullName"],
                            "email" to enrollmentData["email"],
                            "phone" to enrollmentData["phone"],
                            "bkashNumber" to enrollmentData["bkashNumber"],
                            "trxId" to enrollmentData["trxId"],
                            "enrolledAt" to System.currentTimeMillis()
                        )

                        enrollmentRef.set(data)
                            .addOnSuccessListener {
                                enrolledCourseIds = enrolledCourseIds + courseId
                                android.util.Log.d("CookingApp", "Enrolled in course $courseId for user $uid")
                                navController.navigate("enrollSuccess")
                            }
                            .addOnFailureListener { e ->
                                android.util.Log.e("CookingApp", "Error enrolling in course", e)
                            }
                    } else {
                        android.util.Log.e("CookingApp", "User not logged in - cannot enroll")
                    }
                }
            )
        }

        composable("enrollSuccess") {
            EnrollSuccessScreen(
                onDone = {
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = false }
                    }
                }
            )
        }

        composable("profile") {
            ProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("myRecipes") {
            MyRecipesScreen(
                recipes = recipes,
                savedRecipeIds = savedRecipeIds,
                onToggleSave = { id ->
                    val user = auth.currentUser
                    val uid = user?.uid

                    if (uid != null) {
                        val recipeRef = db.collection("users")
                            .document(uid)
                            .collection("savedRecipes")
                            .document(id.toString())

                        if (id in savedRecipeIds) {
                            recipeRef.delete()
                                .addOnSuccessListener {
                                    savedRecipeIds = savedRecipeIds - id
                                }
                                .addOnFailureListener { e ->
                                    android.util.Log.e("CookingApp", "Error removing recipe", e)
                                }
                        } else {
                            val data = hashMapOf(
                                "recipeId" to id,
                                "savedAt" to System.currentTimeMillis()
                            )
                            recipeRef.set(data)
                                .addOnSuccessListener {
                                    savedRecipeIds = savedRecipeIds + id
                                }
                                .addOnFailureListener { e ->
                                    android.util.Log.e("CookingApp", "Error saving recipe", e)
                                }
                        }
                    }
                },
                onRecipeClick = { id ->
                    navController.navigate("detail/$id")
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("myCourses") {
            MyCoursesScreen(
                courses = courses,
                enrolledCourseIds = enrolledCourseIds,
                onCourseClick = { id ->
                    navController.navigate("courseDetail/$id")
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
// ----------------------------- SCREENS -----------------------------

@Composable
fun SplashScreen(onGetStarted: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_bg),
            contentDescription = "Cooking splash",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Discover delicious recipes\nand cook like a pro.",
                fontSize = 18.sp,
                color = Color.White,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onGetStarted,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                )
            ) {
                Text(
                    text = "Get Started",
                    color = Color(0xFF000000),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}



@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onSignUpClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val auth = remember { FirebaseAuth.getInstance() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome Back!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sign in to start your cooking journey.",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            if (error.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = error, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val trimmedEmail = email.trim()
                    val trimmedPassword = password.trim()

                    if (trimmedEmail.isBlank() || trimmedPassword.isBlank()) {
                        error = "Please enter both email and password."
                        return@Button
                    }

                    isLoading = true
                    error = ""

                    auth.signInWithEmailAndPassword(trimmedEmail, trimmedPassword)
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                onLoginSuccess()
                            } else {
                                error = task.exception?.localizedMessage
                                    ?: "Login failed. Please try again."
                            }
                        }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = "Sign in")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Don't have an account?")
                TextButton(onClick = onSignUpClick) {
                    Text(text = "Sign up")
                }
            }
        }
    }
}


// REPLACE YOUR ENTIRE MainScreen FUNCTION (starting around line 820) WITH THIS:

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    recipes: List<Recipe>,
    savedRecipeIds: Set<Int>,
    onLogout: () -> Unit,
    onToggleSave: (Int) -> Unit,
    onRecipeClick: (Int) -> Unit,
    courses: List<Course>,
    onCourseClick: (Int) -> Unit,
    enrolledCourseIds: Set<Int>
) {
    var currentTab by remember { mutableStateOf(BottomTab.HOME) }

    // Landing (Home) search
    var landingSearchText by remember { mutableStateOf("") }

    // Search tab state
    var homeSearchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedDifficulty by remember { mutableStateOf("All") }   // Easy / Medium / Hard
    var sortOption by remember { mutableStateOf("Recommended") }   // Recommended / A–Z / Z–A

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var displayName by remember { mutableStateOf<String?>(null) }
    var displayInitial by remember { mutableStateOf("U") }

    LaunchedEffect(Unit) {
        val user = auth.currentUser
        val uid = user?.uid

        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    val name = doc.getString("username")
                    displayName = name ?: "User"

                    displayInitial = name?.firstOrNull()?.uppercase()?.toString() ?: "U"
                }
        }
    }


    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFFF3E5F5) // light lilac
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Menu",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                NavigationDrawerItem(
                    label = { Text("Profile") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("profile")
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("My Recipes") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("myRecipes")
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("My Courses") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("myCourses")
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Logout button
                NavigationDrawerItem(
                    label = {
                        Text(
                            "Logout",
                            color = Color(0xFFD32F2F) // Red color
                        )
                    },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            // ✅ Clear user data
                            onLogout()
                            // Sign out from Firebase
                            auth.signOut()

                            // Navigate to login screen
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedTextColor = Color(0xFFD32F2F)
                    )
                )
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    // NEW: Home tab
                    NavigationBarItem(
                        selected = currentTab == BottomTab.HOME,
                        onClick = { currentTab = BottomTab.HOME },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )
                    // OLD Home → now Search
                    NavigationBarItem(
                        selected = currentTab == BottomTab.SEARCH,
                        onClick = { currentTab = BottomTab.SEARCH },
                        icon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                        label = { Text("Search") }
                    )
                    NavigationBarItem(
                        selected = currentTab == BottomTab.COURSES,
                        onClick = { currentTab = BottomTab.COURSES },
                        icon = { Icon(Icons.Filled.Star, contentDescription = "Courses") },
                        label = { Text("Courses") }
                    )
                    NavigationBarItem(
                        selected = currentTab == BottomTab.COOKAI,
                        onClick = { currentTab = BottomTab.COOKAI },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_cookai),
                                contentDescription = "CookAi",
                                modifier = Modifier.size(28.dp)
                            )
                        },
                        label = { Text("CookAi") }
                    )
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (currentTab) {
                    BottomTab.HOME -> {
                        HomeLandingScreen(
                            landingSearchText = landingSearchText,
                            onLandingSearchChange = { landingSearchText = it },
                            recipes = recipes,
                            courses = courses,
                            onRecipeClick = onRecipeClick,
                            onCourseClick = onCourseClick,
                            onOpenDrawer = {
                                scope.launch { drawerState.open() }
                            },
                            onSearchSubmit = { query ->
                                homeSearchText = query              // put query into Search screen
                                currentTab = BottomTab.SEARCH      // jump to Search tab
                            }
                        )
                    }

                    BottomTab.SEARCH -> {
                        SearchRecipesScreen(
                            homeSearchText = homeSearchText,
                            onHomeSearchChange = { homeSearchText = it },
                            selectedCategory = selectedCategory,
                            onCategoryChange = { selectedCategory = it },
                            selectedDifficulty = selectedDifficulty,
                            onDifficultyChange = { selectedDifficulty = it },
                            sortOption = sortOption,
                            onSortChange = { sortOption = it },
                            recipes = recipes,
                            savedRecipeIds = savedRecipeIds,
                            onToggleSave = onToggleSave,
                            onRecipeClick = onRecipeClick,
                            onOpenDrawer = {
                                scope.launch { drawerState.open() }
                            }
                        )
                    }

                    BottomTab.COURSES -> {
                        CoursesScreen(
                            courses = courses,
                            onCourseClick = onCourseClick,
                            enrolledCourseIds = enrolledCourseIds
                        )
                    }

                    BottomTab.COOKAI -> {
                        CookAiScreen()
                    }
                }

            }
        }
    }
}
@Composable
fun SignUpScreen(
    onSignUpComplete: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Create Account",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sign up to start cooking!",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Username") },
                singleLine = true,
                enabled = !loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                enabled = !loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                enabled = !loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Confirm Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                enabled = !loading
            )

            if (error.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = error, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    error = ""

                    // Validation
                    when {
                        username.isBlank() || email.isBlank() ||
                                password.isBlank() || confirmPassword.isBlank() -> {
                            error = "Please fill all fields."
                            return@Button
                        }
                        password.length < 6 -> {
                            error = "Password should be at least 6 characters."
                            return@Button
                        }
                        password != confirmPassword -> {
                            error = "Passwords do not match."
                            return@Button
                        }
                    }

                    loading = true

                    scope.launch {
                        try {
                            // Step 1: Create Firebase Auth user
                            val authResult = auth.createUserWithEmailAndPassword(
                                email.trim(),
                                password.trim()
                            ).await()

                            val uid = authResult.user?.uid
                            if (uid == null) {
                                throw IllegalStateException("User ID is null after sign up")
                            }

                            // Step 2: Save user data to Firestore
                            val userData = hashMapOf(
                                "username" to username.trim(),
                                "email" to email.trim(),
                                "createdAt" to System.currentTimeMillis()
                            )

                            db.collection("users")
                                .document(uid)
                                .set(userData)
                                .await()

                            // Step 3: Success! Navigate to login
                            withContext(Dispatchers.Main) {
                                loading = false
                                onBackToLogin()
                            }

                        } catch (e: FirebaseAuthException) {
                            withContext(Dispatchers.Main) {
                                loading = false
                                error = when (e.errorCode) {
                                    "ERROR_EMAIL_ALREADY_IN_USE" -> "Email already registered. Try signing in."
                                    "ERROR_INVALID_EMAIL" -> "Invalid email address."
                                    "ERROR_WEAK_PASSWORD" -> "Password too weak. Use at least 6 characters."
                                    "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Check your connection."
                                    else -> "Auth error: ${e.message}"
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                loading = false
                                error = "Error: ${e.message}"
                            }
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    disabledContainerColor = Color(0xFFBDBDBD)
                )
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Sign up",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Already have an account?")
                TextButton(onClick = onBackToLogin) {
                    Text(text = "Sign in", color = Color(0xFF4CAF50))
                }
            }
        }
    }
}






@Composable
fun HomeLandingScreen(
    landingSearchText: String,
    onLandingSearchChange: (String) -> Unit,
    recipes: List<Recipe>,
    courses: List<Course>,
    onRecipeClick: (Int) -> Unit,
    onCourseClick: (Int) -> Unit,
    onOpenDrawer: () -> Unit,
    onSearchSubmit: (String) -> Unit
) {
    // Get current Firebase user
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    // UI state for name + initial
    var displayName by remember { mutableStateOf<String?>(null) }
    var displayInitial by remember { mutableStateOf("U") }

    // Load from Firestore once when component mounts
    LaunchedEffect(Unit) {
        val user = auth.currentUser
        val uid = user?.uid

        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    val name = doc.getString("username")
                    displayName = name ?: "User"
                    displayInitial = name?.firstOrNull()?.uppercase()?.toString() ?: "U"
                }
        }
    }

    val greetingName = displayName ?: "Foodie"
    val popularRecipes = recipes.take(10)
    val recommendedCourses = courses

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header row: avatar + greeting
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFCC80))
                    .clickable { onOpenDrawer() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayInitial,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5D4037),
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = "Hi, $greetingName!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Welcome back to Nourish.",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search on top
        OutlinedTextField(
            value = landingSearchText,
            onValueChange = onLandingSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search recipes or courses…") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearchSubmit(landingSearchText)
                }
            ),
            trailingIcon = {
                IconButton(onClick = {
                    onSearchSubmit(landingSearchText)
                }) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search"
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFF7043),
                unfocusedBorderColor = Color.LightGray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFFFE0B2))
        ) {
            Image(
                painter = painterResource(id = R.drawable.splash_bg),
                contentDescription = "Cooking banner",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.25f))
            )
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(20.dp)
            ) {
                Text(
                    text = "Cook smarter, not harder.",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Discover recipes, follow guided steps,\nand learn from online courses.",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Popular recipes
        Text(
            text = "Popular recipes",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(popularRecipes) { recipe ->
                RecipeSquareCard(
                    recipe = recipe,
                    onClick = { onRecipeClick(recipe.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Recommended courses
        Text(
            text = "Recommended courses",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(recommendedCourses) { course ->
                CourseHorizontalCard(
                    course = course,
                    onCourseClick = onCourseClick
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun RecipeSquareCard(
    recipe: Recipe,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(160.dp)
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        tonalElevation = 3.dp
    ) {
        Box {
            Image(
                painter = painterResource(id = recipe.imageRes),
                contentDescription = recipe.name,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.25f))
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = recipe.name,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${recipe.prepTime} • ${recipe.difficulty}",
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
        }
    }
}
@Composable
fun CourseHorizontalCard(
    course: Course,
    onCourseClick: (Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .width(220.dp)
            .height(150.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onCourseClick(course.id) },
        tonalElevation = 3.dp
    ) {
        Column {
            Image(
                painter = painterResource(id = course.bannerRes),
                contentDescription = course.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .padding(8.dp)
            ) {
                Text(
                    text = course.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Level: ${course.level}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Starts: ${course.startDate}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
@Composable
fun SearchRecipesScreen(
    homeSearchText: String,
    onHomeSearchChange: (String) -> Unit,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    selectedDifficulty: String,
    onDifficultyChange: (String) -> Unit,
    sortOption: String,
    onSortChange: (String) -> Unit,
    recipes: List<Recipe>,
    savedRecipeIds: Set<Int>,
    onToggleSave: (Int) -> Unit,
    onRecipeClick: (Int) -> Unit,
    onOpenDrawer: () -> Unit
) {
    val categories = listOf(
        "All",
        "Breakfast",
        "Lunch",
        "Dinner",
        "Vegetarian",
        "Dessert"
    )

    val difficultyOptions = listOf("All", "Easy", "Medium", "Hard")

    val filteredRecipes = recipes
        .filter { recipe ->
            val matchesSearch =
                homeSearchText.isBlank() ||
                        recipe.name.contains(homeSearchText, ignoreCase = true)

            val matchesCategory =
                selectedCategory == "All" ||
                        recipe.category.equals(selectedCategory, ignoreCase = true)

            val matchesDifficulty =
                selectedDifficulty == "All" ||
                        recipe.difficulty.equals(selectedDifficulty, ignoreCase = true)

            matchesSearch && matchesCategory && matchesDifficulty
        }
        .let { list ->
            when (sortOption) {
                "A–Z" -> list.sortedBy { it.name.lowercase() }
                "Z–A" -> list.sortedByDescending { it.name.lowercase() }
                else -> list
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {



        OutlinedTextField(
            value = homeSearchText,
            onValueChange = onHomeSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search recipes…") },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search"
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFF7043),
                unfocusedBorderColor = Color.LightGray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Filter by type
        Text(
            text = "Filter by type",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            categories.forEach { cat ->
                val selected = selectedCategory == cat
                Surface(
                    onClick = { onCategoryChange(cat) },
                    shape = RoundedCornerShape(50),
                    tonalElevation = if (selected) 4.dp else 1.dp,
                    color = if (selected) Color(0xFFFF7043) else Color(0xFFFFF3E0),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = cat,
                        fontSize = 12.sp,
                        color = if (selected) Color.White else Color.Black,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter by difficulty
        Text(
            text = "Filter by difficulty",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            difficultyOptions.forEach { diff ->
                val selected = selectedDifficulty == diff
                Surface(
                    onClick = { onDifficultyChange(diff) },
                    shape = RoundedCornerShape(50),
                    tonalElevation = if (selected) 4.dp else 1.dp,
                    color = if (selected) Color(0xFFE1BEE7) else Color(0xFFF5F5F5),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = diff,
                        fontSize = 12.sp,
                        color = if (selected) Color(0xFF4A148C) else Color.Black,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sort
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Sort by",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Row {
                val sortOptions = listOf("Recommended", "A–Z", "Z–A")
                sortOptions.forEach { opt ->
                    val selected = sortOption == opt
                    TextButton(onClick = { onSortChange(opt) }) {
                        Text(
                            text = opt,
                            fontSize = 12.sp,
                            color = if (selected) Color(0xFFFF7043) else Color.DarkGray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        RecipeList(
            recipes = filteredRecipes,
            savedRecipeIds = savedRecipeIds,
            onToggleSave = onToggleSave,
            onRecipeClick = onRecipeClick
        )
    }
}

// ----------------------------- PROFILE / MY PAGES -----------------------------

// REPLACE YOUR ProfileScreen FUNCTION (around line 1340) WITH THIS COMPLETE CODE:

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(false) }
    var isLoadingData by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    // Load user data from Firestore when screen opens
    LaunchedEffect(Unit) {
        val user = auth.currentUser
        val uid = user?.uid

        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    username = doc.getString("username") ?: ""
                    email = doc.getString("email") ?: user.email ?: ""
                    isLoadingData = false
                }
                .addOnFailureListener {
                    email = user.email ?: ""
                    isLoadingData = false
                }
        } else {
            isLoadingData = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_media_previous),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoadingData) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Account Details",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { }, // Email cannot be changed
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Gray,
                        disabledBorderColor = Color.LightGray,
                        disabledLabelColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Email cannot be changed",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Change Password (Optional)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !loading
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !loading
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm new password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !loading
                )

                if (error.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = error, color = Color.Red, fontSize = 12.sp)
                }

                if (successMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = successMessage, color = Color(0xFF4CAF50), fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        error = ""
                        successMessage = ""

                        // Validate username
                        if (username.isBlank()) {
                            error = "Username cannot be empty."
                            return@Button
                        }

                        // If user wants to change password, validate it
                        val wantsToChangePassword = currentPassword.isNotBlank() ||
                                newPassword.isNotBlank() ||
                                confirmPassword.isNotBlank()

                        if (wantsToChangePassword) {
                            when {
                                currentPassword.isBlank() -> {
                                    error = "Please enter your current password."
                                    return@Button
                                }
                                newPassword.isBlank() -> {
                                    error = "Please enter a new password."
                                    return@Button
                                }
                                newPassword.length < 6 -> {
                                    error = "New password must be at least 6 characters."
                                    return@Button
                                }
                                newPassword != confirmPassword -> {
                                    error = "New passwords do not match."
                                    return@Button
                                }
                            }
                        }

                        loading = true

                        scope.launch {
                            try {
                                val user = auth.currentUser
                                val uid = user?.uid

                                if (uid == null || user == null) {
                                    throw Exception("User not logged in")
                                }

                                // Step 1: Update username in Firestore
                                val userData = hashMapOf(
                                    "username" to username.trim(),
                                    "email" to email.trim(),
                                    "updatedAt" to System.currentTimeMillis()
                                )

                                db.collection("users")
                                    .document(uid)
                                    .update(userData as Map<String, Any>)
                                    .await()

                                // Step 2: If password change requested, update it
                                if (wantsToChangePassword) {
                                    // Re-authenticate user before changing password
                                    val credential = com.google.firebase.auth.EmailAuthProvider
                                        .getCredential(email, currentPassword)

                                    user.reauthenticate(credential).await()

                                    // Now update password
                                    user.updatePassword(newPassword).await()

                                    // Clear password fields after success
                                    withContext(Dispatchers.Main) {
                                        currentPassword = ""
                                        newPassword = ""
                                        confirmPassword = ""
                                    }
                                }

                                withContext(Dispatchers.Main) {
                                    loading = false
                                    successMessage = "Changes saved successfully!"
                                }

                            } catch (e: FirebaseAuthException) {
                                withContext(Dispatchers.Main) {
                                    loading = false
                                    error = when (e.errorCode) {
                                        "ERROR_WRONG_PASSWORD" -> "Current password is incorrect."
                                        "ERROR_WEAK_PASSWORD" -> "New password is too weak."
                                        "ERROR_REQUIRES_RECENT_LOGIN" -> "Please log out and log back in to change password."
                                        else -> "Error: ${e.message}"
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    loading = false
                                    error = "Error: ${e.message}"
                                }
                            }
                        }
                    },
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        disabledContainerColor = Color(0xFFBDBDBD)
                    )
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save Changes")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRecipesScreen(
    recipes: List<Recipe>,
    savedRecipeIds: Set<Int>,
    onToggleSave: (Int) -> Unit,
    onRecipeClick: (Int) -> Unit,
    onBack: () -> Unit
) {
    val savedList = recipes.filter { it.id in savedRecipeIds }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Recipes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_media_previous),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (savedList.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "You haven’t saved any recipes yet.",
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(savedList) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        isSaved = true,
                        onToggleSave = { onToggleSave(recipe.id) },
                        onClick = { onRecipeClick(recipe.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCoursesScreen(
    courses: List<Course>,
    enrolledCourseIds: Set<Int>,
    onCourseClick: (Int) -> Unit,
    onBack: () -> Unit
) {
    val enrolledList = courses.filter { it.id in enrolledCourseIds }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Courses") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_media_previous),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (enrolledList.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "You haven’t enrolled in any courses yet.",
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(enrolledList) { course ->
                    CourseCard(
                        course = course,
                        onCourseClick = onCourseClick
                    )
                }
            }
        }
    }
}

// ----------------------------- REUSABLE UI -----------------------------

@Composable
fun RecipeList(
    recipes: List<Recipe>,
    savedRecipeIds: Set<Int>,
    onToggleSave: (Int) -> Unit,
    onRecipeClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(recipes) { recipe ->
            RecipeCard(
                recipe = recipe,
                isSaved = savedRecipeIds.contains(recipe.id),
                onToggleSave = { onToggleSave(recipe.id) },
                onClick = { onRecipeClick(recipe.id) }
            )
        }
    }
}

@Composable
fun RecipeCard(
    recipe: Recipe,
    isSaved: Boolean,
    onToggleSave: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = recipe.imageRes),
                    contentDescription = recipe.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = recipe.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Prep time: ${recipe.prepTime}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = recipe.category,
                    fontSize = 12.sp,
                    color = Color(0xFFFF7043)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Difficulty: ${recipe.difficulty}",
                    fontSize = 12.sp,
                    color = Color(0xFF616161)
                )
            }

            IconButton(onClick = onToggleSave) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = if (isSaved) "Saved" else "Save",
                    tint = if (isSaved) Color(0xFFFFC107) else Color.Gray
                )
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    isSaved: Boolean,
    onToggleSave: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = recipe.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(
                                id = android.R.drawable.ic_media_previous
                            ),
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onToggleSave) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = if (isSaved) "Saved" else "Save",
                            tint = if (isSaved) Color(0xFFFFC107) else Color.Gray
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                Image(
                    painter = painterResource(id = recipe.imageRes),
                    contentDescription = recipe.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Prep: ${recipe.prepTime}",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Ingredients",
                modifier = Modifier.padding(horizontal = 16.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                recipe.ingredients.forEach { ingredient ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF7043))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = ingredient, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Instructions",
                modifier = Modifier.padding(horizontal = 16.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                recipe.steps.forEachIndexed { index, step ->
                    Row(
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${index + 1}.",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(24.dp)
                        )
                        Text(
                            text = step,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ----------------------------- COURSES UI -----------------------------

// REPLACE YOUR CoursesScreen FUNCTION (around line 1710) WITH THIS:

@Composable
fun CoursesScreen(
    courses: List<Course>,
    onCourseClick: (Int) -> Unit,
    enrolledCourseIds: Set<Int> = emptySet()  // ✅ NEW parameter
) {
    var searchText by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf("All") }

    val levels = listOf("All", "Easy", "Medium", "Advanced")

    val filteredCourses = courses.filter { course ->
        val matchesSearch =
            searchText.isBlank() ||
                    course.title.contains(searchText, ignoreCase = true) ||
                    course.instructor.contains(searchText, ignoreCase = true)

        val matchesLevel =
            selectedLevel == "All" ||
                    course.level.equals(selectedLevel, ignoreCase = true)

        matchesSearch && matchesLevel
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Cooking & Baking Courses",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Learn from expert instructors and level up your skills.",
            fontSize = 13.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ------- Search bar for courses -------
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search courses…") },
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ------- Filter by difficulty / level -------
        Text(
            text = "Filter by difficulty",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            levels.forEach { level ->
                val selected = selectedLevel == level
                Surface(
                    onClick = { selectedLevel = level },
                    shape = RoundedCornerShape(50),
                    tonalElevation = if (selected) 4.dp else 1.dp,
                    color = if (selected) Color(0xFFFF7043) else Color(0xFFFFF3E0),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = level,
                        fontSize = 12.sp,
                        color = if (selected) Color.White else Color.Black,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredCourses) { course ->
                CourseCard(
                    course = course,
                    onCourseClick = onCourseClick,
                    isEnrolled = enrolledCourseIds.contains(course.id)  // ✅ Pass enrolled status
                )
            }
        }
    }
}

// REPLACE YOUR CourseCard FUNCTION (around line 1790) WITH THIS:

@Composable
fun CourseCard(
    course: Course,
    onCourseClick: (Int) -> Unit,
    isEnrolled: Boolean = false  // ✅ NEW parameter with default value
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box {
                Image(
                    painter = painterResource(id = course.bannerRes),
                    contentDescription = course.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentScale = ContentScale.Crop
                )

                // ✅ NEW: Show "ENROLLED" badge if user is enrolled
                if (isEnrolled) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF4CAF50))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ENROLLED",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = course.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Duration: ${course.duration}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Level: ${course.level}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { onCourseClick(course.id) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("See details")
                }
            }
        }
    }
}
// REPLACE YOUR CourseDetailScreen FUNCTION (around line 1810) WITH THIS:

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    course: Course,
    isEnrolled: Boolean,  // ✅ NEW parameter
    onBack: () -> Unit,
    onEnrollClick: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = course.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_media_previous),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Box {
                Image(
                    painter = painterResource(id = course.bannerRes),
                    contentDescription = course.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )

                // ✅ NEW: Show "ENROLLED" badge if user is enrolled
                if (isEnrolled) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF4CAF50))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "ENROLLED",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = course.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Instructor: ${course.instructor}")
                Text("Start date: ${course.startDate}")
                Text("Duration: ${course.duration}")
                Text("Level: ${course.level}")

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Course Outline",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                course.outline.forEachIndexed { index, item ->
                    Text(text = "${index + 1}. $item", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Certificate of participation will be provided upon completion.",
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Why you should do this course",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You'll learn practical, hands-on recipes and techniques you can use at home, guided by experienced instructors. Interactive sessions, Q&A, and real-time feedback will help you improve quickly.",
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (!isEnrolled) {
                    Text(
                        text = "Hurry up, seats are limited",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFFD32F2F)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onEnrollClick(course.id) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isEnrolled,  // ✅ Disable if already enrolled
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEnrolled) Color.Gray else Color(0xFF4CAF50),
                        disabledContainerColor = Color.Gray
                    )
                ) {
                    Text(if (isEnrolled) "Already Enrolled" else "Enroll now")
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
// REPLACE YOUR CourseEnrollScreen FUNCTION (around line 1900) WITH THIS:

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseEnrollScreen(
    course: Course,
    onBack: () -> Unit,
    onEnrollSuccess: (Map<String, String>) -> Unit  // ✅ Changed to pass enrollment data
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Enroll in ${course.title}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_media_previous),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            EnrollmentForm(
                onSubmit = { enrollmentData ->
                    onEnrollSuccess(enrollmentData)
                }
            )
        }
    }
}

// REPLACE YOUR EnrollmentForm FUNCTION WITH THIS:

@Composable
fun EnrollmentForm(
    onSubmit: (Map<String, String>) -> Unit  // ✅ Changed to pass enrollment data
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var bkashNumber by remember { mutableStateOf("") }
    var trxId by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Text(
        text = "Please fill the information and pay 2000 tk via Bkash merchant number: 01623456789 or 01734568970 to avail a seat. Once you complete payment and fill the form, an email will be sent to you with zoom links and credentials.",
        fontSize = 14.sp
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = fullName,
        onValueChange = { fullName = it },
        label = { Text("Full Name") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Email") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = phone,
        onValueChange = { phone = it },
        label = { Text("Phone Number") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = bkashNumber,
        onValueChange = { bkashNumber = it },
        label = { Text("Bkash Number") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = trxId,
        onValueChange = { trxId = it },
        label = { Text("Trx ID") },
        modifier = Modifier.fillMaxWidth()
    )

    if (error.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = error, color = Color.Red, fontSize = 12.sp)
    }

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = {
            // Validate fields
            when {
                fullName.isBlank() -> {
                    error = "Please enter your full name"
                }
                email.isBlank() -> {
                    error = "Please enter your email"
                }
                phone.isBlank() -> {
                    error = "Please enter your phone number"
                }
                bkashNumber.isBlank() -> {
                    error = "Please enter your Bkash number"
                }
                trxId.isBlank() -> {
                    error = "Please enter transaction ID"
                }
                else -> {
                    // All fields filled, submit data
                    val enrollmentData = mapOf(
                        "fullName" to fullName.trim(),
                        "email" to email.trim(),
                        "phone" to phone.trim(),
                        "bkashNumber" to bkashNumber.trim(),
                        "trxId" to trxId.trim()
                    )
                    onSubmit(enrollmentData)
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text("Submit")
    }
}

// ✅ Separate success page with confetti + heartfelt message
@Composable
fun EnrollSuccessScreen(
    onDone: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBF5))
    ) {
        // Confetti in the background
        ConfettiOverlay(visible = true)

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.9f))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF66BB6A),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Enrollment Successful! 🎉",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Thank you for joining the course. 💛",
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "We’ve received your details and payment.\n" +
                        "You’ll soon receive an email with the Zoom link,\n" +
                        "schedule, and all the instructions you need.\n\n" +
                        "We’re so excited to cook and bake with you! 🍰🍳",
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDone,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Home")
            }
        }
    }
}

// ----------------------------- COOK AI UI -----------------------------

@Composable
fun CookAiScreen() {
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var userInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // ✅ CORRECT: Using Google AI SDK (Gemini)
    val generativeModel = remember {
        try {
            GenerativeModel(
                modelName = "gemini-2.5-flash",
                apiKey = "AIzaSyCmkhxY32hEyq9nCxZGeZ8GJ-UxgWvAd7c",  // ⚠️ YOU MUST ADD YOUR API KEY HERE
                generationConfig = generationConfig {
                    temperature = 0.7f
                    topK = 40
                    topP = 0.95f
                    maxOutputTokens = 1024
                }
            )

        } catch (e: Exception) {
            android.util.Log.e("CookAi", "Error initializing Gemini AI", e)
            null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBF5))
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFF6B35))
                .padding(16.dp)
        ) {
            Text(
                text = "Cook AI Assistant",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "👨‍🍳",
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Hi! I'm your cooking assistant",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D3748)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ask me anything about recipes, cooking techniques, or ingredients!",
                            fontSize = 14.sp,
                            color = Color(0xFF718096),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }

                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(
                            listOf(
                                "Suggest a quick dinner recipe",
                                "How do I make pasta from scratch?",
                                "What can I cook with chicken and rice?",
                                "Tips for baking bread"
                            )
                        ) { suggestion ->
                            SuggestionChip(
                                text = suggestion,
                                onClick = {
                                    userInput = suggestion
                                }
                            )
                        }
                    }
                }
            }

            items(messages) { message ->
                ChatBubble(message = message)
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFFFF6B35)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Thinking...",
                            color = Color(0xFF718096),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Error message
        errorMessage?.let { error ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFEE2E2))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚠️",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = error,
                        color = Color(0xFFC53030),
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { errorMessage = null }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFFC53030)
                        )
                    }
                }
            }
        }

        // Input area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = userInput,
                onValueChange = { userInput = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask me anything about cooking...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF6B35),
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                ),
                shape = RoundedCornerShape(24.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.width(8.dp))

            FloatingActionButton(
                onClick = {
                    if (userInput.isNotBlank() && !isLoading) {
                        val userMessage = ChatMessage(
                            text = userInput,
                            isUser = true,
                            timestamp = System.currentTimeMillis()
                        )
                        messages = messages + userMessage
                        val question = userInput
                        userInput = ""
                        errorMessage = null

                        coroutineScope.launch {
                            listState.animateScrollToItem(messages.size)
                        }

                        // Send to AI
                        coroutineScope.launch {
                            isLoading = true
                            try {
                                if (generativeModel != null) {
                                    val response = generativeModel.generateContent(question)
                                    val aiMessage = ChatMessage(
                                        text = response.text ?: "Sorry, I couldn't generate a response.",
                                        isUser = false,
                                        timestamp = System.currentTimeMillis()
                                    )
                                    messages = messages + aiMessage
                                    listState.animateScrollToItem(messages.size)
                                } else {
                                    errorMessage = "AI model not initialized. Please check your API key."
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("CookAi", "Error generating response", e)
                                errorMessage = "Error: ${e.message ?: "Unknown error occurred"}"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                containerColor = Color(0xFFFF6B35),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun SuggestionChip(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 14.sp,
            color = Color(0xFF2D3748)
        )
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 16.dp
            ),
            color = if (message.isUser) Color(0xFFFF6B35) else Color.White,
            modifier = Modifier.widthIn(max = 280.dp),
            shadowElevation = 2.dp
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                color = if (message.isUser) Color.White else Color(0xFF2D3748),
                fontSize = 15.sp
            )
        }
    }
}

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long
)
// ----------------------------- CONFETTI -----------------------------

data class ConfettiParticle(
    val xFraction: Float,   // 0f..1f (relative to width)
    val speed: Float,       // falling speed multiplier
    val phase: Float,       // starting offset
    val color: Color
)

@Composable
fun ConfettiOverlay(visible: Boolean) {
    if (!visible) return

    val particles = remember {
        val colors = listOf(
            Color(0xFFFFC107),
            Color(0xFFFF7043),
            Color(0xFFFF9800),
            Color(0xFF4CAF50),
            Color(0xFF29B6F6),
            Color(0xFFE91E63)
        )
        List(80) {
            ConfettiParticle(
                xFraction = Random.nextFloat(),
                speed = 0.5f + Random.nextFloat() * 1.5f,
                phase = Random.nextFloat(),
                color = colors.random()
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confettiProgress"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val w = size.width
        val h = size.height
        val radius = 4.dp.toPx()

        particles.forEach { p ->
            val x = p.xFraction * w
            val yFraction = (progress * p.speed + p.phase) % 1f
            val y = yFraction * h

            drawCircle(
                color = p.color,
                radius = radius,
                center = Offset(x, y)
            )
        }
    }
}
// ----------------------------- JSON HELPERS -----------------------------

fun imageNameToRes(context: Context, imageName: String): Int {
    return context.resources.getIdentifier(
        imageName,
        "drawable",
        context.packageName
    )
}

fun loadRecipesFromJson(context: Context): List<Recipe> {
    return try {
        val json = context.assets.open("database.json")
            .bufferedReader()
            .use { it.readText() }

        val array = JSONArray(json)
        val list = mutableListOf<Recipe>()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)

            val name = obj.optString("name", "Untitled recipe")
            val imageName = obj.optString("image", "")
            val prepTime = obj.optString("prepTime", "N/A")
            val category = obj.optString("category", "Other")
            val difficulty = obj.optString("difficulty", "Medium")

            val ingredientsJson = obj.optJSONArray("ingredients")
            val stepsJson = obj.optJSONArray("steps")

            val ingredients = mutableListOf<String>()
            val steps = mutableListOf<String>()

            if (ingredientsJson != null) {
                for (j in 0 until ingredientsJson.length()) {
                    ingredients.add(ingredientsJson.optString(j))
                }
            }

            if (stepsJson != null) {
                for (j in 0 until stepsJson.length()) {
                    steps.add(stepsJson.optString(j))
                }
            }

            val imageResId = imageNameToRes(context, imageName)
            val safeImageRes =
                if (imageResId != 0) imageResId else R.drawable.ic_launcher_foreground

            list.add(
                Recipe(
                    id = i + 1,
                    name = name,
                    imageRes = safeImageRes,
                    prepTime = prepTime,
                    category = category,
                    ingredients = if (ingredients.isNotEmpty()) ingredients
                    else listOf("No ingredients listed."),
                    steps = if (steps.isNotEmpty()) steps
                    else listOf("No instructions provided."),
                    difficulty = difficulty
                )
            )
        }

        if (list.isEmpty()) sampleRecipes() else list
    } catch (e: Exception) {
        e.printStackTrace()
        sampleRecipes()
    }
}

