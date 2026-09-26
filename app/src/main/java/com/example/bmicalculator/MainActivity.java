package com.example.bmicalculator;

import android.content.res.Configuration;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    static final int RISK_UNDERWEIGHT = 0;
    static final int RISK_NORMAL = 1;
    static final int RISK_OVERWEIGHT = 2;
    static final int RISK_OBESE = 3;

    private static final String KEY_BMI_TEXT = "bmi_text";
    private static final String KEY_RISK_TEXT = "risk_text";
    private static final String KEY_RISK_COLOR = "risk_color";
    private static final String KEY_APPLIED_FONT_SCALE = "applied_font_scale";
    private static final Pattern DECIMAL_INPUT_PATTERN =
            Pattern.compile("[0-9]{1,8}(\\.[0-9]{0,2})?");

    private final DecimalFormat bmiFormatter = new DecimalFormat("0.00");
    private final SparseArray<Float> originalTextSizes = new SparseArray<>();

    private EditText weightInput;
    private EditText heightInput;
    private TextView bmiOutput;
    private TextView riskOutput;

    private float baselineResourceFontScale;
    private float currentSystemFontScale;
    private float appliedFontScale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        weightInput = findViewById(R.id.weight_input);
        heightInput = findViewById(R.id.height_input);
        bmiOutput = findViewById(R.id.bmi_output);
        riskOutput = findViewById(R.id.risk_output);
        final Button calculateBtn = findViewById(R.id.calculate_btn);

        weightInput.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8, 2)});
        heightInput.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8, 2)});

        baselineResourceFontScale = getResources().getConfiguration().fontScale;
        currentSystemFontScale = baselineResourceFontScale;
        appliedFontScale = baselineResourceFontScale;
        captureOriginalTextSizes();

        if (savedInstanceState != null) {
            bmiOutput.setText(savedInstanceState.getCharSequence(
                    KEY_BMI_TEXT, getText(R.string.bmi_default)));
            riskOutput.setText(savedInstanceState.getCharSequence(
                    KEY_RISK_TEXT, getText(R.string.risk_default)));
            riskOutput.setTextColor(savedInstanceState.getInt(
                    KEY_RISK_COLOR,
                    ContextCompat.getColor(this, R.color.risk_default)));
            appliedFontScale = savedInstanceState.getFloat(
                    KEY_APPLIED_FONT_SCALE, baselineResourceFontScale);
            applyFontScale(appliedFontScale);
        }

        calculateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calculateAndDisplayBmi();
            }
        });
    }

    private void calculateAndDisplayBmi() {
        String weightText = weightInput.getText().toString().trim();
        String heightText = heightInput.getText().toString().trim();

        weightInput.setError(null);
        heightInput.setError(null);

        if (weightText.isEmpty() || heightText.isEmpty()) {
            if (weightText.isEmpty()) {
                weightInput.setError(getText(R.string.empty_input_warning));
            }
            if (heightText.isEmpty()) {
                heightInput.setError(getText(R.string.empty_input_warning));
            }
            showValidationMessage(R.string.empty_input_warning);
            return;
        }

        if (!isValidDecimalInput(weightText) || !isValidDecimalInput(heightText)) {
            weightInput.setError(getText(R.string.invalid_input_warning));
            heightInput.setError(getText(R.string.invalid_input_warning));
            showValidationMessage(R.string.invalid_input_warning);
            return;
        }

        try {
            double weightKg = Double.parseDouble(weightText);
            double heightCm = Double.parseDouble(heightText);

            if (!isValidMeasurement(weightKg, heightCm)) {
                weightInput.setError(getText(R.string.invalid_input_warning));
                heightInput.setError(getText(R.string.invalid_input_warning));
                showValidationMessage(R.string.invalid_input_warning);
                return;
            }

            double bmi = calculateBmi(weightKg, heightCm);
            bmiOutput.setText(bmiFormatter.format(bmi));
            showRisk(classifyBmi(bmi));
        } catch (NumberFormatException exception) {
            weightInput.setError(getText(R.string.invalid_input_warning));
            heightInput.setError(getText(R.string.invalid_input_warning));
            showValidationMessage(R.string.invalid_input_warning);
        }
    }

    static boolean isValidMeasurement(double weightKg, double heightCm) {
        return Double.isFinite(weightKg)
                && Double.isFinite(heightCm)
                && weightKg > 0.0
                && heightCm > 0.0;
    }

    static boolean isValidDecimalInput(String input) {
        return DECIMAL_INPUT_PATTERN.matcher(input).matches();
    }

    static double calculateBmi(double weightKg, double heightCm) {
        double heightMeters = heightCm / 100.0;
        double rawBmi = weightKg / (heightMeters * heightMeters);

        // The checklist examples use one-decimal rounding and display two decimals.
        return Math.round(rawBmi * 10.0) / 10.0;
    }

    static int classifyBmi(double bmi) {
        if (bmi < 18.5) {
            return RISK_UNDERWEIGHT;
        } else if (bmi < 25.0) {
            return RISK_NORMAL;
        } else if (bmi < 30.0) {
            return RISK_OVERWEIGHT;
        } else {
            return RISK_OBESE;
        }
    }

    private void showRisk(int riskLevel) {
        if (riskLevel == RISK_UNDERWEIGHT) {
            riskOutput.setText(R.string.risk_underweight);
            riskOutput.setTextColor(ContextCompat.getColor(this, R.color.risk_underweight));
        } else if (riskLevel == RISK_NORMAL) {
            riskOutput.setText(R.string.risk_normal);
            riskOutput.setTextColor(ContextCompat.getColor(this, R.color.risk_normal));
        } else if (riskLevel == RISK_OVERWEIGHT) {
            riskOutput.setText(R.string.risk_overweight);
            riskOutput.setTextColor(ContextCompat.getColor(this, R.color.risk_overweight));
        } else {
            riskOutput.setText(R.string.risk_obese);
            riskOutput.setTextColor(ContextCompat.getColor(this, R.color.risk_obese));
        }
    }

    private void showValidationMessage(int messageResource) {
        bmiOutput.setText(R.string.bmi_default);
        riskOutput.setText(messageResource);
        riskOutput.setTextColor(ContextCompat.getColor(this, R.color.error_color));
    }

    private void captureOriginalTextSizes() {
        int[] textViewIds = {
                R.id.title_text,
                R.id.weight_label,
                R.id.weight_input,
                R.id.height_label,
                R.id.height_input,
                R.id.bmi_label,
                R.id.bmi_output,
                R.id.risk_label,
                R.id.risk_output,
                R.id.calculate_btn
        };

        for (int viewId : textViewIds) {
            TextView textView = findViewById(viewId);
            originalTextSizes.put(viewId, textView.getTextSize());
        }
    }

    private void applyFontScale(float selectedFontScale) {
        float scaleRatio = selectedFontScale / baselineResourceFontScale;

        for (int index = 0; index < originalTextSizes.size(); index++) {
            int viewId = originalTextSizes.keyAt(index);
            TextView textView = findViewById(viewId);
            float originalSizePixels = originalTextSizes.valueAt(index);
            textView.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    originalSizePixels * scaleRatio);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (Math.abs(newConfig.fontScale - currentSystemFontScale) > 0.01f) {
            final float newSystemFontScale = newConfig.fontScale;
            currentSystemFontScale = newSystemFontScale;

            new AlertDialog.Builder(this)
                    .setTitle(R.string.font_scale_title)
                    .setMessage(R.string.font_scale_message)
                    .setPositiveButton(R.string.use_system_font, (dialog, which) -> {
                        appliedFontScale = newSystemFontScale;
                        applyFontScale(appliedFontScale);
                    })
                    .setNegativeButton(R.string.keep_current_font, (dialog, which) -> {
                        applyFontScale(appliedFontScale);
                    })
                    .show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putCharSequence(KEY_BMI_TEXT, bmiOutput.getText());
        outState.putCharSequence(KEY_RISK_TEXT, riskOutput.getText());
        outState.putInt(KEY_RISK_COLOR, riskOutput.getCurrentTextColor());
        outState.putFloat(KEY_APPLIED_FONT_SCALE, appliedFontScale);
        super.onSaveInstanceState(outState);
    }
}

class DecimalDigitsInputFilter implements InputFilter {
    private final Pattern mPattern;

    DecimalDigitsInputFilter(int digits, int digitsAfterZero) {
        mPattern = Pattern.compile(
                "[0-9]{0," + (digits - 1) + "}+((\\.[0-9]{0,"
                        + (digitsAfterZero - 1) + "})?)||(\\.)?");
    }

    @Override
    public CharSequence filter(
            CharSequence source,
            int start,
            int end,
            Spanned dest,
            int dstart,
            int dend) {
        Matcher matcher = mPattern.matcher(dest);
        if (!matcher.matches()) {
            return "";
        }
        return null;
    }
}
