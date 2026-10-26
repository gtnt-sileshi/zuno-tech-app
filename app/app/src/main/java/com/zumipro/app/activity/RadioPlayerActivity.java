package com.zumipro.app.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;

import com.squareup.picasso.Picasso;

import com.zumipro.app.R;
import com.zumipro.app.callback.Method;
import com.zumipro.app.dialog.FeedBackDialog;
import com.zumipro.app.dialog.SleepTimerDialog;
import com.zumipro.app.executor.LoadStatus;
import com.zumipro.app.interfaces.SuccessListener;
import com.zumipro.app.utils.IfSupported;
import com.zumipro.app.utils.helper.Helper;
import com.zumipro.app.utils.helper.SPHelper;

/**
 * In-activity radio player — no foreground service required.
 * Playback stops when the user leaves the screen.
 */
@OptIn(markerClass = UnstableApi.class)
public class RadioPlayerActivity extends AppCompatActivity {

    private ExoPlayer   player;
    private boolean     isPlaying = false;

    private ImageButton btnPlayPause;
    private ProgressBar pbBuffering;
    private TextView    tvStatus;
    private Helper      helper;
    private SPHelper    spHelper;
    private String      deviceId;
    private ImageView   fav;

    private String  streamUrl;
    private String  radioId;
    private String  radioName;
    private String  logo;
    private boolean isFav;

    private SleepTimerDialog sleepTimerDialog;

    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IfSupported.applyTheme(this);
        setContentView(R.layout.activity_radio_player);
        IfSupported.isRTL(this);
        IfSupported.isScreenshot(this);
        IfSupported.keepScreenOn(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { finish(); }
        });

        Intent intent = getIntent();
        radioId   = intent.getStringExtra("radio_id");
        streamUrl = intent.getStringExtra("stream_url");
        radioName = intent.getStringExtra("radio_name");
        logo      = intent.getStringExtra("logo");
        String genre       = intent.getStringExtra("genre");
        String description = intent.getStringExtra("description");

        setTitle(radioName != null ? radioName : getString(R.string.live_radio));

        helper   = new Helper(this);
        spHelper = new SPHelper(this);
        deviceId = android.provider.Settings.Secure.getString(
                getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        ImageView ivLogo        = findViewById(R.id.iv_radio_logo);
        TextView  tvName        = findViewById(R.id.tv_radio_name);
        TextView  tvGenre       = findViewById(R.id.tv_radio_genre);
        TextView  tvDescription = findViewById(R.id.tv_radio_description);
        btnPlayPause            = findViewById(R.id.btn_play_pause);
        pbBuffering             = findViewById(R.id.pb_radio_buffer);
        tvStatus                = findViewById(R.id.tv_radio_status);
        fav                     = findViewById(R.id.iv_fav);
        LinearLayout adView     = findViewById(R.id.ll_adView);

        if (logo != null && !logo.isEmpty() && !logo.equals("null")) {
            Picasso.get()
                    .load(logo)
                    .placeholder(R.drawable.material_design_default)
                    .error(R.drawable.material_design_default)
                    .into(ivLogo);
        }
        tvName.setText(radioName != null ? radioName : "");
        tvGenre.setText(genre != null && !genre.isEmpty() ? genre : "");
        tvDescription.setText(description != null && !description.isEmpty() ? description : "");

        helper.showBannerAd(adView, "");

        isFav = intent.getBooleanExtra("is_favourite", false);
        changeFav(isFav);
        fav.setOnClickListener(v -> loadFav());

        ImageView ivShare = findViewById(R.id.iv_share);
        ivShare.setOnClickListener(v -> shareStation());

        initPlayer();

        btnPlayPause.setOnClickListener(v -> {
            if (player == null) return;
            if (player.isPlaying()) {
                player.pause();
            } else {
                player.play();
            }
        });

        sleepTimerDialog = new SleepTimerDialog(this, () -> {
            Toast.makeText(this, R.string.sleep_timer_expired, Toast.LENGTH_SHORT).show();
            releasePlayer();
            finish();
        });
    }

    private void initPlayer() {
        if (streamUrl == null || streamUrl.isEmpty()) {
            Toast.makeText(this, getString(R.string.stream_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        player = new ExoPlayer.Builder(this).build();
        player.setMediaItem(MediaItem.fromUri(Uri.parse(streamUrl)));
        player.prepare();
        player.setPlayWhenReady(true);

        player.addListener(new Player.Listener() {

            @Override
            public void onPlaybackStateChanged(int state) {
                switch (state) {
                    case Player.STATE_BUFFERING:
                        pbBuffering.setVisibility(View.VISIBLE);
                        tvStatus.setText(getString(R.string.loading));
                        break;
                    case Player.STATE_READY:
                        pbBuffering.setVisibility(View.GONE);
                        tvStatus.setText(isPlaying
                                ? getString(R.string.radio_playing)
                                : getString(R.string.radio_paused));
                        break;
                    case Player.STATE_ENDED:
                    case Player.STATE_IDLE:
                        pbBuffering.setVisibility(View.GONE);
                        tvStatus.setText(getString(R.string.radio_stopped));
                        isPlaying = false;
                        btnPlayPause.setImageResource(R.drawable.ic_play);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onIsPlayingChanged(boolean playing) {
                isPlaying = playing;
                btnPlayPause.setImageResource(playing ? R.drawable.ic_pause : R.drawable.ic_play);
                tvStatus.setText(playing
                        ? getString(R.string.radio_playing)
                        : getString(R.string.radio_paused));
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                pbBuffering.setVisibility(View.GONE);
                tvStatus.setText(getString(R.string.stream_not_found));
                isPlaying = false;
                btnPlayPause.setImageResource(R.drawable.ic_play);
                Toast.makeText(RadioPlayerActivity.this,
                        getString(R.string.stream_not_found), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void releasePlayer() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) player.pause();
    }

    @Override
    protected void onDestroy() {
        if (sleepTimerDialog != null) sleepTimerDialog.cancelTimer();
        releasePlayer();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_radio_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_sleep_timer) {
            sleepTimerDialog.showDialog();
        } else if (id == R.id.menu_stop_playback) {
            releasePlayer();
            finish();
        } else if (id == R.id.menu_feedback) {
            new FeedBackDialog(this).showDialog(radioId, radioName);
        } else if (id == R.id.menu_share) {
            shareStation();
        }
        return true;
    }

    private void loadFav() {
        if (!helper.isNetworkAvailable()) {
            Toast.makeText(this, getString(R.string.err_internet_not_connected), Toast.LENGTH_SHORT).show();
            return;
        }
        new LoadStatus(new SuccessListener() {
            @Override public void onStart() { changeFav(!isFav); }
            @Override public void onEnd(String success, String favSuccess, String message) {
                boolean ok = success.equals("1") && (favSuccess.equals("1") || favSuccess.equals("2"));
                if (ok) {
                    Toast.makeText(RadioPlayerActivity.this, message, Toast.LENGTH_SHORT).show();
                } else {
                    changeFav(!isFav);
                    Toast.makeText(RadioPlayerActivity.this,
                            getString(R.string.err_server_not_connected), Toast.LENGTH_SHORT).show();
                }
            }
        }, helper.getAPIRequest(Method.METHOD_RADIO_FAV_TOG, 0, radioId, "", "",
                "", deviceId, "", "", "", "", "", "", "radio", null))
        .execute();
    }

    private void changeFav(boolean favourite) {
        isFav = favourite;
        fav.setImageResource(favourite ? R.drawable.ic_turned_in : R.drawable.ic_turned_in_not);
    }

    private void shareStation() {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_the_app));
        String shareText = (radioName != null ? radioName : getString(R.string.live_radio))
                + "\n\nvia " + getResources().getString(R.string.app_name)
                + " - http://play.google.com/store/apps/details?id=" + getPackageName();
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share)));
    }
}
