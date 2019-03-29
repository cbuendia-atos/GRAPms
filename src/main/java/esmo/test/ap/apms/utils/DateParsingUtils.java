package esmo.test.ap.apms.utils;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author nikos
 */
public class DateParsingUtils {

    public static Date parseAmkaDate(String amka) throws ParseException {
        String dd = amka.substring(0, 2);
        String mm = amka.substring(2, 4);
        String yy = amka.substring(4, 6);

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        return formatter.parse(dd + "-" + mm + "-" + yy);

    }
    
    public static Date parseEidasDate(String eidasDate) throws ParseException {
        //1980-01-01
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        return formatter.parse(eidasDate);
    }
    

}
