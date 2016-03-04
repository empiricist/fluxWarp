package com.empiricist.teleflux.utility;

public class ParseHelper {


    public static int safeReadInt(String string, int defaultVal){
        int res = defaultVal;
        //LogHelper.info("Trying to read string: " + string);
        try{
            res = (int)Double.parseDouble(string);//computercraft returns all numbers as decimals, b/c lua
        }catch(NumberFormatException e){
            res = defaultVal;
        }
        return res;
    }

    public static String safeReadString(Object obj){
        //if (obj instanceof String){
        return obj.toString();
        //}else{
        //    return "";
        //}
    }
}


