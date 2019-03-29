/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esmo.test.ap.apms.utils;

/**
 *
 * @author nikos
 */
public class EidasNamesUtils {

    public static String getLatin(String eidasValue) {
        if (eidasValue.contains(",")) {
            return eidasValue.split(",")[1];
        }
        return eidasValue;
    }
}
