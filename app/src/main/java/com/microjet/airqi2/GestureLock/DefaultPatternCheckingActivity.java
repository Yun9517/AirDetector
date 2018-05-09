package com.microjet.airqi2.GestureLock;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.github.ihsg.patternlocker.OnPatternChangeListener;
import com.github.ihsg.patternlocker.PatternIndicatorView;
import com.github.ihsg.patternlocker.PatternLockerView;
import com.microjet.airqi2.AirMapActivity;
import com.microjet.airqi2.Definition.SavePreferences;
import com.microjet.airqi2.R;
import com.microjet.airqi2.SettingActivity;

import java.util.List;


public class DefaultPatternCheckingActivity extends AppCompatActivity {
    private PatternIndicatorView patternIndicatorView;
    private TextView textMsg;
    private PatternHelper patternHelper;

    private static int actionMode = 0;

    public static final int START_ACTION_MODE_NORMAL = 0;
    public static final int START_ACTION_MODE_DISABLE = 1;
    public static final int START_ACTION_MODE_CHANGE_PASSWOPRD = 2;

    public static void startAction(Context context, int mode) {
        Intent intent = new Intent(context, DefaultPatternCheckingActivity.class);
        context.startActivity(intent);

        actionMode = mode;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_pattern_checking);

        this.patternIndicatorView = findViewById(R.id.pattern_indicator_view);
        PatternLockerView patternLockerView = findViewById(R.id.pattern_lock_view);
        this.textMsg = findViewById(R.id.text_msg);

        patternLockerView.setOnPatternChangedListener(new OnPatternChangeListener() {
            @Override
            public void onStart(PatternLockerView view) {
            }

            @Override
            public void onChange(PatternLockerView view, List<Integer> hitList) {
            }

            @Override
            public void onComplete(PatternLockerView view, List<Integer> hitList) {
                boolean isError = !isPatternOk(hitList);
                view.updateStatus(isError);
                patternIndicatorView.updateState(hitList, isError);
                updateMsg();

                if(!isError) {
                    switch (actionMode) {
                        case START_ACTION_MODE_DISABLE:
                            SharedPreferences share = getSharedPreferences(SavePreferences.SETTING_KEY, Context.MODE_PRIVATE);
                            share.edit().putBoolean(SavePreferences.SETTING_MAP_PRIVACY, false).apply();
                            break;

                        case START_ACTION_MODE_NORMAL:
                            callCompletePage();
                            break;

                        case START_ACTION_MODE_CHANGE_PASSWOPRD:
                            DefaultPatternSettingActivity.startAction(DefaultPatternCheckingActivity.this);
                            break;
                    }
                }
                Log.e("Pattern", "Error: " + isError);
            }

            @Override
            public void onClear(PatternLockerView view) {
                finishIfNeeded();
            }
        });

        this.textMsg.setText(getResources().getText(R.string.text_draw_pattern));
        this.patternHelper = new PatternHelper();
    }

    private boolean isPatternOk(List<Integer> hitList) {
        this.patternHelper.validateForChecking(hitList);
        return this.patternHelper.isOk();
    }

    private void updateMsg() {
        this.textMsg.setText(this.patternHelper.getMessage());
        this.textMsg.setTextColor(this.patternHelper.isOk() ?
                getResources().getColor(R.color.colorPrimary) :
                getResources().getColor(R.color.colorAccent));
    }

    private void finishIfNeeded() {
        if (this.patternHelper.isFinish()) {
            finish();
        }
    }

    private void callCompletePage() {
        Intent i = new Intent(this, AirMapActivity.class);
        startActivity(i);
    }
}
