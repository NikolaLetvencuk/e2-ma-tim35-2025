    package com.example.dailyboss;

    import android.Manifest;
    import android.content.Context;
    import android.content.Intent;
    import android.content.pm.PackageManager;
    import android.os.Build;
    import android.os.Bundle;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.content.ContextCompat;

    // Dodajte uvoz za Firebase
    import com.example.dailyboss.data.SharedPreferencesHelper;
    import com.example.dailyboss.data.dao.UserDao;
    import com.example.dailyboss.data.repository.UserStatisticRepository;
    import com.example.dailyboss.domain.model.User;
    import com.example.dailyboss.domain.usecase.UpdateActiveDaysUseCase;
    import com.example.dailyboss.presentation.fragments.CategoryListFragment;
    import com.example.dailyboss.presentation.fragments.FriendsFragment;
    import com.example.dailyboss.presentation.fragments.ShopFragment;
    import com.example.dailyboss.presentation.fragments.UserProfileFragment;
    import com.example.dailyboss.presentation.fragments.UserSearchDialogFragment;
    import com.example.dailyboss.receiver.MorningReceiver;
    import com.example.dailyboss.service.BadgeService;
    import com.google.firebase.auth.FirebaseAuth;

    import com.example.dailyboss.presentation.activities.AuthenticationActivity;
    import com.example.dailyboss.presentation.fragments.BattleFragment;
    import com.example.dailyboss.presentation.fragments.HomeFragment;
    import com.example.dailyboss.presentation.fragments.TasksFragment;
    import com.google.android.material.bottomnavigation.BottomNavigationView;
    import android.app.AlarmManager;
    import android.app.PendingIntent;
    import android.util.Log;
    import androidx.activity.result.ActivityResultLauncher; // DODATI
    import androidx.activity.result.contract.ActivityResultContracts; // DODATI
    import android.util.Log;
    import com.example.dailyboss.presentation.fragments.ShopFragment;
import com.example.dailyboss.presentation.fragments.EquipmentActivationFragment;


    import java.util.Calendar;
    import com.example.dailyboss.receiver.ResetReceiver;


    public class MainActivity extends AppCompatActivity {

        private FirebaseAuth firebaseAuth;
        private SharedPreferencesHelper prefs;
        private UpdateActiveDaysUseCase updateActiveDaysUseCase;

        private final ActivityResultLauncher<String> requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        Log.d("NotifPermission", "POST_NOTIFICATIONS granted.");
                    } else {
                        Log.w("NotifPermission", "POST_NOTIFICATIONS denied. Notifications will not work.");
                    }
                });

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            firebaseAuth = FirebaseAuth.getInstance();
            prefs = new SharedPreferencesHelper(this);

            boolean isLoggedInLocally = prefs.isUserLoggedIn();
            String userId = prefs.getLoggedInUserId();


            if (userId == null) {
                navigateToAuth();
                return;
            }
            checkAndUpdateActiveDays();
            UserStatisticRepository statisticRepository = new UserStatisticRepository(this);

            Log.d("tag", "onCreate: pre");
            updateActiveDaysUseCase = new UpdateActiveDaysUseCase(statisticRepository);

            UserDao userDao = new UserDao(this);
            User localUser = userDao.getUser(userId);

            if (localUser == null) {
                prefs.logoutUser();
                navigateToAuth();
                return;
            }
            if (firebaseAuth.getCurrentUser() == null || !isLoggedInLocally) {
                navigateToAuth();
                return;
            }
            updateActiveDaysUseCase.execute();

            setContentView(R.layout.activity_main);
            askNotificationPermission();

            scheduleMidnightReset(this);
            scheduleMorningReceiver(this);
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
            testImmediateReceiver(this);
            testImmediateReceiver2(this);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();

            bottomNavigationView.setOnItemSelectedListener(item -> {

                if (item.getItemId() == R.id.activity_tasks) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new BattleFragment())
                            .commit();
                    return true;
                } else if (item.getItemId() == R.id.nav_categories) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new FriendsFragment())
                            .commit();
                    return true;
                } else if (item.getItemId() == R.id.nav_categories_list) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new ShopFragment())
                            .commit();
                    return true;
                } else if (item.getItemId() == R.id.nav_shop) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new TasksFragment())
                            .commit();
                    return true;
                } else if (item.getItemId() == R.id.activity_profile) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container,  new UserProfileFragment())
                            .commit();
                    return true;
                }
                return false;
            });
        }

        private void checkAndUpdateActiveDays() {

        }

        private void askNotificationPermission() {
            // Proveri da li je Android 13 (API 33) ili noviji
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_GRANTED
                ) {
                    // Dozvola već postoji, sve je OK
                } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    // Prikazati obrazloženje zašto je potrebna dozvola, pa pozvati requestPermissionLauncher.launch(...)
                } else {
                    // Traži dozvolu
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                }
            }
        }

        // Preimenovana metoda
        private void navigateToAuth() {
            Intent intent = new Intent(this, AuthenticationActivity.class);
            startActivity(intent);
            finish();
        }

        private void scheduleMidnightReset(Context context) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(context, ResetReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
            );

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 20);
            calendar.set(Calendar.MINUTE, 39);
            calendar.set(Calendar.SECOND, 35);

            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );
        }

        private void testImmediateReceiver(Context context) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, MorningReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_IMMUTABLE);

            // Ovo pokreće odmah bez potrebe za SCHEDULE_EXACT_ALARM
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 1000, // posle 1s
                    pendingIntent
            );
        }

        private void testImmediateReceiver2(Context context) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, ResetReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_IMMUTABLE);

            // Ovo pokreće odmah bez potrebe za SCHEDULE_EXACT_ALARM
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 1000, // posle 1s
                    pendingIntent
            );
        }

        private void scheduleMorningReceiver(Context context) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, MorningReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_IMMUTABLE);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 20); // 00:01
            calendar.set(Calendar.MINUTE, 39);
            calendar.set(Calendar.SECOND, 30);

            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );
        }

    }