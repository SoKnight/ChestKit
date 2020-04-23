package ru.soknight.chestkit.date;

import lombok.Getter;

@Getter
public class DateUnit {

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
