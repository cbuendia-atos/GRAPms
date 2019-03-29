/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esmo.test.ap.apms.utils;

import net.gcardone.junidecode.Junidecode;

/**
 *
 * @author nikos
 */
public class TranslitarateUtils {

    public static String convertGreektoLatin(String greekText) {
        String res = Junidecode.unidecode(greekText);
        switch (res) {
            case "OKhI":
                return "NO";
            case "NAI":
                return "YES";
            default:
                return res;
        }
    }

}
