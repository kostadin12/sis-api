package com.kostadin.sis.excel.request;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ExtractWorkingDaysCommand {
    String startDate;
    String numOfDays;
}
