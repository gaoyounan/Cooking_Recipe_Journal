package com.example.gaoyounan.cooking_recipe.util;

public class LogCount {

    private static int i = 0;
    private static LogCount instance = null;

    private LogCount(){
        i = 0;
    }

    public static LogCount getInstance()
    {
        if(instance == null)
        {
            instance = new LogCount();
        }

        return instance;
    }

    public void setIncrease()
    {
        i++;
    }


    public void checkReqCount()
    {
        System.out.println("!!!!!!!!!!!!!!!!!!!Request："+i+"， cache_size:" + RecipeImageUtils.getInstance().getCacheSize());
    }
}
