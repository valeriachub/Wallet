package app.wallet;

import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    private Unbinder unbinder;
    private SharedPreferences preferences;
    private Handler handler;
    private Runnable runnable;
    private int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_main);
        unbinder = ButterKnife.bind(this);
        initUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        handler.removeCallbacks(runnable);
    }

    private void initUI() {
        initProgress();
    }

    private void initProgress() {

        handler = new Handler();
        runnable = () -> {
            if (i < 5) {
                i++;
                runOnUiThread(() -> progressBar.setProgress(i));
                handler.postDelayed(runnable, 200);
            } else {
                i++;
                runOnUiThread(() -> progressBar.setProgress(i));
                checkWallet();
            }
        };
        handler.post(runnable);
    }

    private void checkWallet() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences != null && preferences.contains(Const.PREF_WALLET_ADDRESS) && !preferences.getString(Const.PREF_WALLET_ADDRESS, "").isEmpty()) {
            HomeActivity.start(this);
            finish();
        } else {
            CreateWalletActivity.start(this);
            finish();
        }
    }
}
