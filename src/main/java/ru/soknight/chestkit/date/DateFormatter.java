package ru.soknight.chestkit.date;

import ru.soknight.chestkit.configuration.Config;

public class DateFormatter {
	
	public static String getFormatedTime(Config config, long minutes) {
        String formated = "", separator = config.getColoredString("date-formating.separator");
        long hours, days, weeks, months, years;
        
        DateUnit dc = new DateUnit(minutes);
        minutes = dc.getMinutes();  hours = dc.getHours();
        days    = dc.getDays();     weeks = dc.getWeeks();
        months  = dc.getMonths();   years = dc.getYears();
        
        // String formating:
        if(years > 0)
            formated = config.getColoredString("date-formating.years").replace("%y%", String.valueOf(years));
        if(months > 0) {
            if(!formated.equals("")) formated += separator;
            formated += config.getColoredString("date-formating.months").replace("%M%", String.valueOf(months));
        }
        if(weeks > 0) {
            if(!formated.equals("")) formated += separator;
            formated += config.getColoredString("date-formating.weeks").replace("%w%", String.valueOf(weeks));
        }
        if(days > 0) {
            if(!formated.equals("")) formated += separator;
            formated += config.getColoredString("date-formating.days").replace("%d%", String.valueOf(days));
        }
        if(hours > 0) {
            if(!formated.equals("")) formated += separator;
            formated += config.getColoredString("date-formating.hours").replace("%H%", String.valueOf(hours));
        }
        if(minutes > 0) {
            if(!formated.equals("")) formated += separator;
            formated += config.getColoredString("date-formating.minutes").replace("%m%", String.valueOf(minutes));
        }
        
        return formated;
    }
}
