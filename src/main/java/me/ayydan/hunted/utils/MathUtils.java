package me.ayydan.hunted.utils;

public class MathUtils
{
    public static int roundToNearestMultiple(int number, int multiple)
    {
        return number + (multiple - (number % multiple));
    }
}
