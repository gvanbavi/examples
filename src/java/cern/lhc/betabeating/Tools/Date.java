/*
 * $Id $
 *
 * $Date$
 * $Revision$
 * $Author$
 *
 * Copyright CERN, All Rights Reserved.
 */
package cern.lhc.betabeating.Tools;

import java.util.Calendar;

/**
 * Date utils.
 * 
 *
 * @author tbach
 */
public class Date {
    
    /** Example: 23-12-2012 */
    public static String getCurrentDateAsString()
    {
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DATE);
        int month = cal.get(Calendar.MONTH) + 1; //january = 0, lets start with 1 instead of 0
        int year = cal.get(Calendar.YEAR);
        StringBuilder stringbBuilder = new StringBuilder();
        String delimiter = "-";
        stringbBuilder.append(day).append(delimiter).append(month).append(delimiter).append(year);
        return stringbBuilder.toString();
    }
}