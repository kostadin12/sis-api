package com.kostadin.sis.excel.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkingDay {
    String calendarDate;
    char weekdayFlag;
    char holidayFlag;
}
