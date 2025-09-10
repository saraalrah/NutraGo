package com.example.nutrago;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.nutrago.ui.home.HomeFragment;
import com.example.nutrago.ui.Gallery.GalleryFragment;
import com.example.nutrago.ui.cart.CartFragment;
import com.example.nutrago.ui.about.AboutUsFragment;
import com.example.nutrago.ui.contact.ContactUsFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.view.MenuItem;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView navView;
    private SoundPool soundPool;
    private int welcomeSoundId = -1;
    private boolean soundLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            // Initialize CartManager - Very important!

            // Setup welcome sound (but don't play automatically)
            setupWelcomeSound();

            navView = findViewById(R.id.nav_view);

            // Setup Action Bar with large logo on the left
            setupActionBarWithLogo();

            // First page to show
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment_activity_main, new HomeFragment())
                    .commitAllowingStateLoss();

            setupBottomNavigation();

        } catch (Exception e) {
            e.printStackTrace();
            // In case of general error, try basic app setup
            setupBasicNavigation();
        }
    }

    // Setup welcome sound using SoundPool (without auto-play)
    private void setupWelcomeSound() {
        try {
            checkAudioSettings();

            // Check if file exists first
            if (getResources().getIdentifier("welcome", "raw", getPackageName()) == 0) {
                return;
            }

            // Setup SoundPool for modern devices (API 21+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();

                soundPool = new SoundPool.Builder()
                        .setMaxStreams(1)
                        .setAudioAttributes(audioAttributes)
                        .build();
            } else {
                // For older devices
                soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
            }

            // Load sound
            welcomeSoundId = soundPool.load(this, R.raw.welcome, 1);

            // Setup listener for playback after loading
            soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    if (status == 0 && sampleId == welcomeSoundId) {
                        soundLoaded = true;
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkAudioSettings() {
        try {
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

            if (audioManager != null) {
                // Check volume level
                int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

                // Increase volume if too low
                if (currentVolume < maxVolume / 2) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume / 2, 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Public method to play welcome sound (can be called from fragments)
    public void playWelcomeSound() {
        try {
            if (soundPool != null && soundLoaded && welcomeSoundId != -1) {
                // Set volume level
                AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                float volume = 1.0f;

                if (audioManager != null) {
                    float currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    float maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    volume = currentVolume / maxVolume;
                }

                // Play sound
                soundPool.play(welcomeSoundId, volume, volume, 1, 0, 1.0f);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupBottomNavigation() {
        if (navView != null) {
            navView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    int itemId = item.getItemId();

                    try {
                        if (itemId == R.id.navigation_home) {
                            selectedFragment = new HomeFragment();
                        } else if (itemId == R.id.navigation_gallery) {
                            selectedFragment = new GalleryFragment();
                        } else if (itemId == R.id.navigation_cart) {
                            selectedFragment = new CartFragment();
                        } else if (itemId == R.id.navigation_about) {
                            selectedFragment = new AboutUsFragment();
                        } else if (itemId == R.id.navigation_contact) {
                            // Check if ContactUsFragment exists
                            try {
                                selectedFragment = new ContactUsFragment();
                            } catch (Exception e) {
                                e.printStackTrace();
                                // If ContactUsFragment creation fails, use fallback fragment
                                selectedFragment = createSimpleContactFragment();
                            }
                        } else {
                            // If no matching Fragment found, return to home page
                            selectedFragment = new HomeFragment();
                        }

                        if (selectedFragment != null) {
                            try {
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.nav_host_fragment_activity_main, selectedFragment)
                                        .commitAllowingStateLoss(); // Use commitAllowingStateLoss instead of commit
                                return true;
                            } catch (Exception e) {
                                e.printStackTrace();
                                // In case of error, return to home page
                                try {
                                    getSupportFragmentManager().beginTransaction()
                                            .replace(R.id.nav_host_fragment_activity_main, new HomeFragment())
                                            .commitAllowingStateLoss();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                return true;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // In case of general error, return to home page
                        try {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.nav_host_fragment_activity_main, new HomeFragment())
                                    .commitAllowingStateLoss();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        return true;
                    }

                    return false;
                }
            });
        }
    }

    // Basic navigation setup in case main setup fails
    private void setupBasicNavigation() {
        try {
            navView = findViewById(R.id.nav_view);
            if (navView != null) {
                navView.setOnItemSelectedListener(item -> {
                    try {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.nav_host_fragment_activity_main, new HomeFragment())
                                .commitAllowingStateLoss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Create simple contact fragment in case ContactUsFragment fails
    private Fragment createSimpleContactFragment() {
        return new Fragment() {
            @Override
            public android.view.View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
                try {
                    LinearLayout layout = new LinearLayout(MainActivity.this);
                    layout.setOrientation(LinearLayout.VERTICAL);
                    layout.setPadding(50, 50, 50, 50);
                    layout.setBackgroundColor(0xFFF5F5F5);

                    // Title
                    android.widget.TextView title = new android.widget.TextView(MainActivity.this);
                    title.setText("📬 Contact Us");
                    title.setTextSize(24);
                    title.setTextColor(0xFF220F84);
                    title.setPadding(0, 0, 0, 30);
                    title.setGravity(Gravity.CENTER);
                    layout.addView(title);

                    // Contact information
                    android.widget.TextView message = new android.widget.TextView(MainActivity.this);
                    message.setText("📧 Email: nutrago@example.com\n\n📞 Phone: +966 12 345 6789\n\n📍 Location: Riyadh, Saudi Arabia\n\n💬 We'd love to hear from you!");
                    message.setTextSize(16);
                    message.setTextColor(0xFF666666);
                    message.setPadding(20, 20, 20, 20);
                    message.setGravity(Gravity.CENTER);
                    message.setLineSpacing(8, 1.2f);
                    layout.addView(message);

                    return layout;
                } catch (Exception e) {
                    e.printStackTrace();
                    // In case of layout creation failure, return empty view
                    return new android.view.View(MainActivity.this);
                }
            }
        };
    }

    private void setupActionBarWithLogo() {
        try {
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }

            LinearLayout toolbarLayout = new LinearLayout(this);
            toolbarLayout.setOrientation(LinearLayout.HORIZONTAL);
            toolbarLayout.setBackgroundColor(0xFF6200EE);
            toolbarLayout.setGravity(Gravity.CENTER_VERTICAL);

            int toolbarHeight = (int) (100 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams toolbarParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    toolbarHeight
            );
            toolbarLayout.setLayoutParams(toolbarParams);

            ImageView logoView = new ImageView(this);
            logoView.setImageResource(R.drawable.logo);
            logoView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(
                    (int) (400 * getResources().getDisplayMetrics().density),
                    (int) (130 * getResources().getDisplayMetrics().density)
            );
            logoParams.setMargins(
                    (int) (12 * getResources().getDisplayMetrics().density),
                    (int) (22 * getResources().getDisplayMetrics().density),
                    0, 0
            );

            logoView.setLayoutParams(logoParams);
            toolbarLayout.addView(logoView);

            LinearLayout rootLayout = new LinearLayout(this);
            rootLayout.setOrientation(LinearLayout.VERTICAL);
            rootLayout.setPadding(0, 0, 0, 0);

            rootLayout.addView(toolbarLayout);

            android.view.View originalContent = ((android.view.ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
            ((android.view.ViewGroup) findViewById(android.R.id.content)).removeView(originalContent);

            LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0
            );
            contentParams.weight = 1;
            contentParams.setMargins(0, 0, 0, 0);

            if (originalContent instanceof android.view.ViewGroup) {
                ((android.view.ViewGroup) originalContent).setPadding(0, 0, 0, 0);
            }

            originalContent.setLayoutParams(contentParams);
            rootLayout.addView(originalContent);

            setContentView(rootLayout);

            navView = findViewById(R.id.nav_view);

            android.view.View fragmentContainer = findViewById(R.id.nav_host_fragment_activity_main);
            if (fragmentContainer != null) {
                fragmentContainer.setPadding(0, 0, 0, 0);
                if (fragmentContainer.getLayoutParams() instanceof android.view.ViewGroup.MarginLayoutParams) {
                    android.view.ViewGroup.MarginLayoutParams params =
                            (android.view.ViewGroup.MarginLayoutParams) fragmentContainer.getLayoutParams();
                    params.setMargins(0, 0, 0, 0);
                    fragmentContainer.setLayoutParams(params);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // In case of ActionBar setup failure, continue without it
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // No additional code needed here
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            // Clean up sound resources
            if (soundPool != null) {
                soundPool.release();
                soundPool = null;
            }

            // Clean up other resources
            navView = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        try {
            // Handle back button control
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);

            if (currentFragment instanceof HomeFragment) {
                // If we're on home page, exit app
                super.onBackPressed();
            } else {
                // If we're on another page, return to home page
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment_activity_main, new HomeFragment())
                        .commitAllowingStateLoss();

                // Update navigation bar
                if (navView != null) {
                    navView.setSelectedItemId(R.id.navigation_home);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            super.onBackPressed();
        }
    }
}