package com.example.bmicalculator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MainActivityTest {

    @Test
    public void checklistExampleOneRoundsToTwentyThree() {
        assertEquals(23.0, MainActivity.calculateBmi(65.0, 168.0), 0.0);
    }

    @Test
    public void checklistExampleTwoRoundsToTwentyFourPointSeven() {
        assertEquals(24.7, MainActivity.calculateBmi(80.0, 180.0), 0.0);
    }

    @Test
    public void repeatedCalculationUsesTheNewValues() {
        double firstResult = MainActivity.calculateBmi(65.0, 168.0);
        double secondResult = MainActivity.calculateBmi(90.0, 180.0);

        assertEquals(23.0, firstResult, 0.0);
        assertEquals(27.8, secondResult, 0.0);
    }

    @Test
    public void measurementsMustBeFiniteAndGreaterThanZero() {
        assertTrue(MainActivity.isValidMeasurement(65.0, 168.0));
        assertFalse(MainActivity.isValidMeasurement(0.0, 168.0));
        assertFalse(MainActivity.isValidMeasurement(65.0, 0.0));
        assertFalse(MainActivity.isValidMeasurement(-1.0, 168.0));
        assertFalse(MainActivity.isValidMeasurement(Double.NaN, 168.0));
        assertFalse(MainActivity.isValidMeasurement(65.0, Double.POSITIVE_INFINITY));
    }

    @Test
    public void decimalInputAllowsAtMostEightWholeAndTwoDecimalDigits() {
        assertTrue(MainActivity.isValidDecimalInput("65"));
        assertTrue(MainActivity.isValidDecimalInput("65.5"));
        assertTrue(MainActivity.isValidDecimalInput("65.55"));
        assertTrue(MainActivity.isValidDecimalInput("12345678.90"));
        assertFalse(MainActivity.isValidDecimalInput(""));
        assertFalse(MainActivity.isValidDecimalInput("."));
        assertFalse(MainActivity.isValidDecimalInput("65.555"));
        assertFalse(MainActivity.isValidDecimalInput("123456789"));
        assertFalse(MainActivity.isValidDecimalInput("-65"));
    }

    @Test
    public void riskBoundariesMatchAdultBmiRanges() {
        assertEquals(MainActivity.RISK_UNDERWEIGHT, MainActivity.classifyBmi(18.4));
        assertEquals(MainActivity.RISK_NORMAL, MainActivity.classifyBmi(18.5));
        assertEquals(MainActivity.RISK_NORMAL, MainActivity.classifyBmi(24.9));
        assertEquals(MainActivity.RISK_OVERWEIGHT, MainActivity.classifyBmi(25.0));
        assertEquals(MainActivity.RISK_OVERWEIGHT, MainActivity.classifyBmi(29.9));
        assertEquals(MainActivity.RISK_OBESE, MainActivity.classifyBmi(30.0));
        assertEquals(MainActivity.RISK_OBESE, MainActivity.classifyBmi(40.0));
    }
}
