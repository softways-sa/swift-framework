package gr.softways.dev.util;

import java.sql.*;
import java.util.*;
import java.math.*;
import java.lang.*;

public class DateAssistant {

   String days[] = {"Sunday", "Monday", "Tuesday", "Wednesday",
                   "Thursday", "Friday", "Saturday"};

  String months[] = {"January", "February", "March", "April",
                     "May", "June", "July", "August", "September",
                     "October", "November", "December"};

  int DaysInMonth[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

  static String[] greekDays = {"", "Κυριακή", "Δευτέρα", "Τρίτη",
                               "Τετάρτη", "Πέμπτη", "Παρασκευή",
                               "Σάββατο"};
                               
  static String[] greekMonths = {"Ιανουαρίου", "Φεβρουαρίου", "Μαρτίου", 
                                 "Απριλίου", "Μαϊου", "Ιουνίου", "Ιουλίου", 
                                 "Αυγούστου", "Σεπτεμβρίου", "Οκτωβρίου", 
                                 "Νοεμβρίου", "Δεκεμβρίου"};
                                 
  public DateAssistant() {
  }

  /*
     USE:  Determines if given year is a leap year.
     IN:   year = given year after 1582 (start of the Gregorian calendar).
     OUT:  TRUE if given year is leap year, FALSE if not.
     NOTE: Formulas capture definition of leap years; cf CalcLeapYears().
  */
  public boolean IsLeapYear(int year) {

  /* If multiple of 100, leap year iff multiple of 400. */
  if ((year % 100) == 0) return((year % 400) == 0);

  /* Otherwise leap year iff multiple of 4. */
  return ((year % 4) == 0);
  } // IsLeapYear

  /**
   *  m1, m2  are the month number - 1
   *
   */
  public int calcDateDiff(int d1, int m1, int y1, int d2, int m2, int y2) {
	  boolean isl1 = IsLeapYear(y1);
	  boolean isl2 = IsLeapYear(y2);
	  int dayNumInYear1 = 0;
	  int dayNumInYear2 = 0;
	  int numOfDaysInYears = 0;
	  int remainingDays1 = 0;

	  dayNumInYear1 = calcDayNumInYear(d1,m1,isl1);
	  dayNumInYear2 = calcDayNumInYear(d2,m2,isl2);
	  if (y1==y2) {
	    return (dayNumInYear2 - dayNumInYear1);
    }
	  for (int i=y1+1; i<y2; i++) {
		  numOfDaysInYears += 365;
		  if (IsLeapYear(i) == true)
		     numOfDaysInYears++;
    }
    remainingDays1 = 365 - dayNumInYear1;
    if (isl1 == true) remainingDays1++;
      return (remainingDays1 + numOfDaysInYears + dayNumInYear2);
  }
  
  // m is month number - 1
  public int calcDayNumInYear(int d, int m, boolean isLeap) {
	  int sumDays = 0;
	  for (int i=0; i<m; i++) {
		  sumDays += DaysInMonth[i];
		  if (i==1 && isLeap == true)
		    sumDays++;
    }
    sumDays += d;
    return sumDays;
  }

  /**
   * Parse dateStr format must be in dd/mm/year and return timestamp.
   *
   */
  protected Timestamp parseDate(String dateStr, String delimiter) {
    StringTokenizer dateTokenizer = new StringTokenizer(dateStr, delimiter);
    String d = null, m = null, y = null;

    Timestamp date = null;

    Calendar calendar = Calendar.getInstance();

    if (dateTokenizer.countTokens() < 3) return null;

    d = dateTokenizer.nextToken();
    m = dateTokenizer.nextToken();
    y = dateTokenizer.nextToken();

    try {
      System.out.println(y + "-" + m + "-" + d + " "
              + calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE) + ":"
              + calendar.get(Calendar.SECOND) + ".00");

      date = Timestamp.valueOf(y + "-" + m + "-" + d + " "
              + calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE) + ":"
              + calendar.get(Calendar.SECOND) + ".00");
    }
    catch (IllegalArgumentException iae) {
      date = null;
      iae.printStackTrace();
    }

    return date;
  }
  
  public int daysInMonth(Timestamp date){
    int monthDays = 0, month = 0, year = 0;
    boolean leapYear = false;
    
    if (date == null)
      return -1;
    
    month = date.getMonth();
    year = date.getYear();
    
    leapYear = IsLeapYear(year);

    if (month == 2 && leapYear)
      monthDays = DaysInMonth[month] + 1;
    else
      monthDays = DaysInMonth[month];
    
    return monthDays;
  }
      
  public static String getGRDate(java.util.Date date, String field) {
    Calendar calendar = Calendar.getInstance();
    
    if (date != null)
      calendar.setTime(date);
    
    String str = "";
    
    try {
      if (field.equalsIgnoreCase("DAY_OF_WEEK")) {
        str = greekDays[ calendar.get(Calendar.DAY_OF_WEEK) ];
      }
      else if (field.equalsIgnoreCase("MONTH")) {
        str = greekMonths[ calendar.get(Calendar.MONTH) ];
      }
      else if (field.equalsIgnoreCase("FULL")) {
        str = greekDays[ calendar.get(Calendar.DAY_OF_WEEK) ] + " " 
            + calendar.get(Calendar.DAY_OF_MONTH) + " " 
            + greekMonths[ calendar.get(Calendar.MONTH) ] + " "
            + calendar.get(Calendar.YEAR);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
    return str;    
  }

  /**
   * Αφαιρει δύο timestamps και επιστρέφει τη διαφορά τους σε μέρες/ώρες/λεπτά.
   *
   * @param t1           Timestamp:   Το πρώτο όρισμα της αφαίρεσης
   * @param t2           Timestamp;   Το δεύτερο όρισμα της αφαίρεσης
   * @param returnType   int;         Τι θα επιστρέψει η αφαίρεση
   *                                  Πιθανές τιμές:
   *                                  days         = 0
   *                                  hours        = 1
   *                                  minutes      = 2
   *                                  seconds      = 3
   *                                  milliseconds = 4
   */

  public long negateTimestamps(Timestamp t1, Timestamp t2, int returnType) {
    // Initialize the calendar object
    Calendar calendar = Calendar.getInstance();

    int year1   = 0, year2   = 0,
        month1  = 0, month2  = 0,
        day1    = 0, day2    = 0,
        hour1   = 0, hour2   = 0,
        minute1 = 0, minute2 = 0,
        second1 = 0, second2 = 0,
        millis1 = 0, millis2 = 0;

    int dayDiff = 0;
    long diff = 0L;

    boolean sameDay = false;

    calendar.setTime(t1);
    year1   = calendar.get(calendar.YEAR);
    month1  = calendar.get(calendar.MONTH);
    day1    = calendar.get(calendar.DAY_OF_YEAR);
    hour1   = calendar.get(calendar.HOUR_OF_DAY);
    minute1 = calendar.get(calendar.MINUTE);
    second1 = calendar.get(calendar.SECOND);
    millis1 = calendar.get(calendar.MILLISECOND);

    calendar.setTime(t2);
    year2   = calendar.get(calendar.YEAR);
    day2    = calendar.get(calendar.DAY_OF_YEAR);
    month1  = calendar.get(calendar.MONTH);
    hour2   = calendar.get(calendar.HOUR_OF_DAY);
    minute2 = calendar.get(calendar.MINUTE);
    second2 = calendar.get(calendar.SECOND);
    millis2 = calendar.get(calendar.MILLISECOND);

    if ((year1 == year2) && (month1 == month2) && (day1 == day2)) {
      sameDay = true;
    }
    else {
      dayDiff = calcDateDiff(day1, month1, year1, day2, month2, year2);
    }

    if (!sameDay) {
      diff = (long)dayDiff;
      
      if (returnType == 1) {
        diff = (diff * 24) + (hour2 - hour1);
        //  Total Hours + Difference
      }
      else if (returnType == 2) {
        diff = (((diff * 24) + (hour2 - hour1)) * 60 ) + (minute2 - minute1);
        // Total Minutes + Difference
      }
      else if (returnType == 3) {
        diff = (((((diff * 24) + (hour2 - hour1)) * 60 ) + (minute2 - minute1)) * 60 ) + (second2 - second1);
      }
      else if (returnType == 4) {
        diff = (((((((diff * 24) + (hour2 - hour1)) * 60 ) + (minute2 - minute1)) * 60 ) + (second2 - second1)) * 1000) + (millis2 - millis1);
      }
    }
    else {
      if (returnType == 1 || returnType == 0) {
        diff = (hour2 - hour1);
      }
      else if (returnType == 2) {
        diff = ((hour2 - hour1) * 60 ) + (minute2 - minute1);
      }
      else if (returnType == 3) {
        diff = ((((hour2 - hour1) * 60 ) + (minute2 - minute1)) * 60 ) + (second2 - second1);
      }
      else if (returnType == 4) {
        diff = ((((((hour2 - hour1) * 60 ) + (minute2 - minute1)) * 60) + (second2 - second1)) * 1000 ) + (millis2 - millis1);
      }
    }
    return diff;
  }
}