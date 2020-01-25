package ru.soknight.chestkit.utils;

import lombok.Getter;
import ru.soknight.chestkit.files.Config;

public class DateFormatter {

	public static String getFormatedTime(long minutes) {
        String formated = "", separator = Config.getString("date-formating.separator");
        long hours, days, weeks, months, years;
        
        DateUnit dc = new DateUnit(minutes);
        minutes = dc.getMinutes();  hours = dc.getHours();
        days    = dc.getDays();     weeks = dc.getWeeks();
        months  = dc.getMonths();   years = dc.getYears();
        
        // String formating:
        if(years > 0)
            formated = Config.getString("date-formating.years").replace("%y%", String.valueOf(years));
        if(months > 0) {
            if(!formated.equals("")) formated += separator;
            formated += Config.getString("date-formating.months").replace("%M%", String.valueOf(months)); }
        if(weeks > 0) {
            if(!formated.equals("")) formated += separator;
            formated += Config.getString("date-formating.weeks").replace("%w%", String.valueOf(weeks)); }
        if(days > 0) {
            if(!formated.equals("")) formated += separator;
            formated += Config.getString("date-formating.days").replace("%d%", String.valueOf(days)); }
        if(hours > 0) {
            if(!formated.equals("")) formated += separator;
            formated += Config.getString("date-formating.hours").replace("%H%", String.valueOf(hours)); }
        if(minutes > 0) {
            if(!formated.equals("")) formated += separator;
            formated += Config.getString("date-formating.minutes").replace("%m%", String.valueOf(minutes)); }
        
        return formated;
    }
	
	
	@Getter
	private static class DateUnit {
		private long minutes;
	    private long hours;
	    private long days;
	    private long weeks;
	    private long months;
	    private long years;
	    
	    public DateUnit(long minutes) {
	        this.minutes = minutes;
	        convert();
	    }
	    
	    private void convert() {
	        // Minutes -> hours:
	        if(minutes >= 60) {
	            hours = minutes / 60;
	            minutes %= 60;
	        }
	        
	        // Hours -> days:
	        if(hours >= 24) {
	            days = hours / 24;
	            hours %= 24;
	        }
	        
	        // Days -> years:
	        if(days >= 365) {
	            years = days / 365;
	            days %= 365;
	        }
	        
	        // Days -> months:
	        if(days >= 30) {
	            months = days / 30;
	            days %= 30;
	        }
	        
	        // Days -> weeks:
	        if(days >= 7) {
	            weeks = days / 7;
	            days %= 7;
	        }
	    }
	}
	
}
