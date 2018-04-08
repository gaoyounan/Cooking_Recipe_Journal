package com.example.gaoyounan.cooking_recipe.util;

public class DataShareInvertory {

    private static int visible_position = 0;

    public static int getVisible_position() {
        return visible_position;
    }

    public static void setVisible_position(int visible_position) {
        DataShareInvertory.visible_position = visible_position;
    }
}
