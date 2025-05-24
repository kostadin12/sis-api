package com.kostadin.sis.excel;

import com.kostadin.sis.excel.request.ExportToExcelVariables;
import com.kostadin.sis.label.model.Label;
import com.kostadin.sis.label.model.response.LabelDTO;
import com.kostadin.sis.userproject.model.UserProject;
import com.kostadin.sis.excel.request.UserExcelDTO;
import com.kostadin.sis.user.model.User;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.time.LocalDate;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.poi.ss.usermodel.BorderStyle.THIN;

@RequiredArgsConstructor
public class ExcelGenerator {
    private ExportToExcelVariables variables;
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;

    public ExcelGenerator(ExportToExcelVariables variables) {
        this.variables = variables;
        this.workbook = new XSSFWorkbook();
    }
    public void generateMembersExcelFile(HttpServletResponse response) throws IOException {
        writeProjectDetailsHeader();
        writeProjectDetails();
        writeReportingDate();
        writeMembersHeader();
        writeMembers();
        ServletOutputStream outputStream = response.getOutputStream();
        sheet.setAutoFilter(new CellRangeAddress(11,11, 0, 8));
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }

    public void generateAbsencesExcelFile(HttpServletResponse response) throws IOException {
        writeProjectDetailsHeader();
        writeProjectDetails();
        if (variables.getProject().isCapacityMode()){
            writeSelectedPeriod();
        } else writeSelectedPeriodWithoutPeriodCapacityCap();
        writeSelectedMembersHeader();
        writeSelectedMembers();
        if (variables.getProject().isCapacityMode()){
            writeDaysOffWithCapacity();
        } else writeDaysOff();
        writeFilteredLabelsIfAny();
        ServletOutputStream outputStream = response.getOutputStream();
        sheet.setAutoFilter(variables.getProject().isCapacityMode() ? new CellRangeAddress(12, 12, 0, 12) : new CellRangeAddress(12, 12, 0, 11));
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }

    public void generateUserReportExcelFile(HttpServletResponse response) throws IOException {
        writeReportingDateAndPeriod();
        writeUserHeader();
        writeUser();
        writeDaysAvailableWithCapacity();
        ServletOutputStream outputStream = response.getOutputStream();
        sheet.setAutoFilter(new CellRangeAddress(7,7, 0, 13));
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }

    public CellStyle getDefaultCellStyle(){
        CellStyle style = workbook.createCellStyle();

        XSSFFont font = workbook.createFont();

        font.setFontHeight(14);

        style.setFont(font);

        style.setBorderTop(THIN);
        style.setBorderBottom(THIN);
        style.setBorderLeft(THIN);
        style.setBorderRight(THIN);

        return style;
    }

    public CellStyle getDefaultCellStyleCentered(){
        CellStyle style = workbook.createCellStyle();

        XSSFFont font = workbook.createFont();

        font.setFontHeight(14);

        style.setFont(font);

        style.setBorderTop(THIN);
        style.setBorderBottom(THIN);
        style.setBorderLeft(THIN);
        style.setBorderRight(THIN);

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    public CellStyle getBoldCellStyle(){
        CellStyle styleBold = workbook.createCellStyle();

        XSSFFont fontBold = workbook.createFont();

        fontBold.setFontHeight(14);
        fontBold.setBold(true);

        styleBold.setFont(fontBold);

        styleBold.setBorderTop(THIN);
        styleBold.setBorderBottom(THIN);
        styleBold.setBorderLeft(THIN);
        styleBold.setBorderRight(THIN);

        styleBold.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleBold.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());

        return styleBold;
    }

    public CellStyle getHeaderCellStyleBold(){
        CellStyle headerStyle = workbook.createCellStyle();

        XSSFFont fontBold = workbook.createFont();

        fontBold.setFontHeight(14);
        fontBold.setBold(true);

        headerStyle.setFont(fontBold);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setBorderTop(THIN);
        headerStyle.setBorderBottom(THIN);
        headerStyle.setBorderLeft(THIN);
        headerStyle.setBorderRight(THIN);

        return headerStyle;
    }

    public CellStyle getHeaderCellStyle(){
        CellStyle headerStyle = workbook.createCellStyle();

        XSSFFont fontBold = workbook.createFont();

        fontBold.setFontHeight(14);

        headerStyle.setFont(fontBold);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        return headerStyle;
    }

    private void createCell(Row row, int columnCount, Object valueOfCell, CellStyle style) {
        Cell cell = row.createCell(columnCount);
        if (valueOfCell instanceof Integer integerValue) {
            cell.setCellValue(integerValue);
        } else if (valueOfCell instanceof Long longValue) {
            cell.setCellValue(longValue);
        } else if (valueOfCell instanceof String stringValue) {
            cell.setCellValue(stringValue);
        } else if (valueOfCell instanceof Double doubleValue) {
            cell.setCellValue(doubleValue);
        } else if (valueOfCell instanceof Float floatValue) {
            cell.setCellValue(floatValue);
        } else {
            cell.setCellValue("");
        }
        cell.setCellStyle(style);
        sheet.autoSizeColumn(columnCount);
    }

    private void writeProjectDetailsHeader() {
        sheet = workbook.createSheet("Project");
        Row projectHeaderRow = sheet.createRow(0);
        Row projectNameRow = sheet.createRow(1);
        Row projectStartDateRow = sheet.createRow(2);
        Row projectEndDateRow = sheet.createRow(3);
        Row projectStatusRow = sheet.createRow(4);
        Row projectMembersCountRow = sheet.createRow(5);

        CellRangeAddress cellAddresses = new CellRangeAddress(0, 0, 0, 1);
        createCell(projectHeaderRow, 0, "Project", getHeaderCellStyleBold());
        sheet.addMergedRegion(cellAddresses);

        CellStyle style = getBoldCellStyle();
        createCell(projectNameRow, 0, "Name", style);
        createCell(projectStartDateRow, 0, "Start date", style);
        createCell(projectEndDateRow, 0, "End date", style);
        createCell(projectStatusRow, 0, "Status", style);
        createCell(projectMembersCountRow, 0, "Member count", style);
    }
    private void writeProjectDetails() {
        sheet = workbook.getSheet("Project");
        Row projectNameRow = sheet.getRow(1);
        Row projectStartDateRow = sheet.getRow(2);
        Row projectEndDateRow = sheet.getRow(3);
        Row projectStatusRow = sheet.getRow(4);
        Row projectMembersCountRow = sheet.getRow(5);

        String endDate;
        if (variables.getProject().getEndDate() != null) {
            endDate = variables.getProject().getEndDate().toString();
        } else endDate = "";

        CellStyle style = getDefaultCellStyle();
        createCell(projectNameRow, 1, variables.getProject().getName(), style);
        createCell(projectStartDateRow, 1, variables.getProject().getStartDate().toString(), style);
        createCell(projectEndDateRow, 1, endDate, style);
        createCell(projectStatusRow, 1, variables.getProject().getProjectStatus().toString(), style);
        createCell(projectMembersCountRow, 1, variables.getProject().getUsers().size(), style);
    }

    private void writeMembersHeader() {
        sheet = workbook.getSheet("Project");
        Row headerRow = sheet.createRow(10);
        Row row = sheet.createRow(11);

        CellRangeAddress cellAddresses = new CellRangeAddress(10, 10, 0, 9);
        createCell(headerRow, 0, "Project members", getHeaderCellStyleBold());
        sheet.addMergedRegion(cellAddresses);

        CellStyle style = getBoldCellStyle();
        createCell(row, 0, "First name", style);
        createCell(row, 1, "Last name", style);
        createCell(row, 2, "Employee number", style);
        createCell(row, 3, "Role", style);
        createCell(row, 4, "Phone", style);
        createCell(row, 5, "Alternative phone", style);
        createCell(row, 6, "Email", style);
        createCell(row, 7, "Alternative email", style);
        createCell(row, 8, "Company", style);
        createCell(row, 9, "Labels", style);
    }

    private void writeMembers() {
        int rowCount = 12;
        CellStyle style = getDefaultCellStyle();

        var users = variables.getProject().getUsers().stream().map(UserProject::getUser).toList();

        for (User user : users) {
            var userLabels = user.getLabels().stream().filter(label -> variables.getProject().getLabels().contains(label)).map(Label::getName).toList();

            int columnCount = 0;
            Row row = sheet.createRow(rowCount++);
            createCell(row, columnCount++, user.getFirstName(), style);
            createCell(row, columnCount++, user.getLastName(), style);
            createCell(row, columnCount++, user.getEmployeeNumber(), style);
            createCell(row, columnCount++, user.getRole().toString().substring(5), style);
            createCell(row, columnCount++, user.getPhone(), style);
            createCell(row, columnCount++, user.getSecondaryPhone(), style);
            createCell(row, columnCount++, user.getEmail(), style);
            createCell(row, columnCount++, user.getSecondaryEmail(), style);
            createCell(row, columnCount++, user.getCompany(), style);
            createCell(row, columnCount++, userLabels.toString(), style);
        }
    }

    private void writeReportingDate() {
        sheet = workbook.getSheet("Project");
        Row reportHeaderRow = sheet.createRow(7);
        Row reportDateRow = sheet.createRow(8);

        CellRangeAddress reportDateHeaderCellAddresses = new CellRangeAddress(7, 7, 0, 1);
        createCell(reportHeaderRow, 0, "Date of report:", getHeaderCellStyleBold());
        sheet.addMergedRegion(reportDateHeaderCellAddresses);

        CellRangeAddress reportDateCellAddresses = new CellRangeAddress(8, 8, 0, 1);
        createCell(reportDateRow, 0, LocalDate.now().toString(), getHeaderCellStyle());
        sheet.addMergedRegion(reportDateCellAddresses);
    }

    private void writeSelectedPeriod() {
        sheet = workbook.getSheet("Project");
        Row reportHeaderRow = sheet.createRow(7);
        Row startDateRow = sheet.createRow(8);
        Row endDateRow = sheet.createRow(9);

        CellStyle percentageStyle = getDefaultCellStyle();
        percentageStyle.setDataFormat(workbook.createDataFormat().getFormat(BuiltinFormats.getBuiltinFormat(9)));

        CellRangeAddress cellAddresses = new CellRangeAddress(7, 7, 0, 1);
        createCell(reportHeaderRow, 0, "Report for period:", getHeaderCellStyleBold());
        sheet.addMergedRegion(cellAddresses);

        CellStyle styleBold = getBoldCellStyle();
        CellStyle style = getDefaultCellStyle();
        createCell(startDateRow, 0, "From", styleBold);
        createCell(endDateRow, 0, "To", styleBold);
        createCell(startDateRow, 1, variables.getStartDate().toString(), style);
        createCell(endDateRow, 1, variables.getEndDate().toString(), style);

        createCell(endDateRow, 6, "Period capacity cap", styleBold);
        createCell(endDateRow, 7, variables.getPeriodCapacityCap() + "%", style);
    }

    private void writeSelectedPeriodWithoutPeriodCapacityCap() {
        sheet = workbook.getSheet("Project");
        Row reportHeaderRow = sheet.createRow(7);
        Row startDateRow = sheet.createRow(8);
        Row endDateRow = sheet.createRow(9);

        CellStyle percentageStyle = getDefaultCellStyle();
        percentageStyle.setDataFormat(workbook.createDataFormat().getFormat(BuiltinFormats.getBuiltinFormat(9)));

        CellRangeAddress cellAddresses = new CellRangeAddress(7, 7, 0, 1);
        createCell(reportHeaderRow, 0, "Report for period:", getHeaderCellStyleBold());
        sheet.addMergedRegion(cellAddresses);

        CellStyle styleBold = getBoldCellStyle();
        CellStyle style = getDefaultCellStyle();
        createCell(startDateRow, 0, "From", styleBold);
        createCell(endDateRow, 0, "To", styleBold);
        createCell(startDateRow, 1, variables.getStartDate().toString(), style);
        createCell(endDateRow, 1, variables.getEndDate().toString(), style);
    }

    private void writeSelectedMembersHeader() {
        sheet = workbook.getSheet("Project");
        Row headerRow = sheet.createRow(11);
        Row row = sheet.createRow(12);

        CellRangeAddress cellAddresses = new CellRangeAddress(11, 11, 0, 11);
        createCell(headerRow, 0, "Project members", getHeaderCellStyleBold());
        sheet.addMergedRegion(cellAddresses);

        CellStyle style = getBoldCellStyle();
        createCell(row, 0, "First name", style);
        createCell(row, 1, "Last name", style);
        createCell(row, 2, "Employee number", style);
        createCell(row, 3, "Role", style);
        createCell(row, 4, "Phone", style);
        createCell(row, 5, "Alternative phone", style);
        createCell(row, 6, "Email", style);
        createCell(row, 7, "Alternative email", style);
        createCell(row, 8, "Company", style);
        createCell(row, 9, "Labels", style);
        createCell(row, 10, "Days absent", style);
    }

    private void writeFilteredLabelsIfAny() {
        if (isNotEmpty(variables.getLabels())){
            sheet = workbook.getSheet("Project");
            Row headerRow = sheet.getRow(8);
            Row row = sheet.getRow(9);

            CellRangeAddress labelsHeaderCell = new CellRangeAddress(8, 8, 3, 7);
            createCell(headerRow, 3, "Filtered by labels:", getHeaderCellStyleBold());
            sheet.addMergedRegion(labelsHeaderCell);

            CellRangeAddress labelsBodyCell = new CellRangeAddress(9, 9, 3, 7);
            createCell(row, 3, variables.getLabels().toString(), getHeaderCellStyle());
            sheet.addMergedRegion(labelsBodyCell);
        }
    }

    private void writeSelectedMembers() {
        int rowCount = 13;
        CellStyle style = getDefaultCellStyle();
        CellStyle labelsStyle = getHeaderCellStyle();
        labelsStyle.setBorderTop(THIN);
        labelsStyle.setBorderBottom(THIN);
        labelsStyle.setBorderLeft(THIN);
        labelsStyle.setBorderRight(THIN);

        for (UserExcelDTO user: variables.getUsers()) {
            int columnCount = 0;
            Row row = sheet.createRow(rowCount++);
            createCell(row, columnCount++, user.getFirstName(), style);
            createCell(row, columnCount++, user.getLastName(), style);
            createCell(row, columnCount++, user.getEmployeeNumber(), style);
            createCell(row, columnCount++, user.getRole().toString().substring(5), style);
            createCell(row, columnCount++, user.getPhone(), style);
            createCell(row, columnCount++, user.getSecondaryPhone(), style);
            createCell(row, columnCount++, user.getEmail(), style);
            createCell(row, columnCount++, user.getSecondaryEmail(), style);
            createCell(row, columnCount++, user.getCompany(), style);
            createCell(row, columnCount++, user.getLabels().stream().map(LabelDTO::getName).toList().toString(), labelsStyle);
            createCell(row, columnCount++, user.getDaysOff(), style);
        }
    }

    private void writeDaysOffWithCapacity() {
        int rowCount = 13;
        CellStyle style = getDefaultCellStyle();
        CellStyle percentageStyle = getDefaultCellStyle();
        percentageStyle.setDataFormat(workbook.createDataFormat().getFormat(BuiltinFormats.getBuiltinFormat(9)));
        CellStyle boldCellStyle = getBoldCellStyle();

        Row headerRow = sheet.getRow(12);
        createCell(headerRow, 11, "Capacity", boldCellStyle);
        createCell(headerRow, 12, "Days available", boldCellStyle);

        for (UserExcelDTO user: variables.getUsers()) {
            int columnCount = 11;
            Row row = sheet.getRow(rowCount++);
            var userCapacity = user.getProjects().get(0).getCapacity();
            createCell(row, columnCount++, userCapacity + "%", percentageStyle);
            var daysAvailable = user.getDaysAvailable() * ((double) userCapacity / 100);
            createCell(row, columnCount++, Math.round(daysAvailable * 10.0) / 10.0, style);
        }
    }

    private void writeDaysOff() {
        int rowCount = 13;
        CellStyle style = getDefaultCellStyle();
        CellStyle labelsStyle = getHeaderCellStyle();
        labelsStyle.setBorderTop(THIN);
        labelsStyle.setBorderBottom(THIN);
        labelsStyle.setBorderLeft(THIN);
        labelsStyle.setBorderRight(THIN);

        Row headerRow = sheet.getRow(12);
        CellStyle boldCellStyle = getBoldCellStyle();
        createCell(headerRow, 11, "Days available", boldCellStyle);

        for (UserExcelDTO user: variables.getUsers()) {
            int columnCount = 11;
            Row row = sheet.getRow(rowCount++);
            createCell(row, columnCount++, user.getDaysAvailable(), style);
        }
    }

    private void writeReportingDateAndPeriod() {
        sheet = workbook.createSheet("User");
        Row reportHeaderRow = sheet.createRow(0);
        CellStyle styleBold = getBoldCellStyle();
        CellStyle style = getDefaultCellStyle();

        createCell(reportHeaderRow, 0, "Date of report:", styleBold);
        createCell(reportHeaderRow, 1, LocalDate.now().toString(), style);

        Row reportPeriodHeaderRow = sheet.createRow(2);
        Row startDateRow = sheet.createRow(3);
        Row endDateRow = sheet.createRow(4);

        CellRangeAddress cellAddresses = new CellRangeAddress(2, 2, 0, 1);
        createCell(reportPeriodHeaderRow, 0, "Report for period:", getHeaderCellStyleBold());
        sheet.addMergedRegion(cellAddresses);

        createCell(startDateRow, 0, "From", styleBold);
        createCell(endDateRow, 0, "To", styleBold);
        createCell(startDateRow, 1, variables.getStartDate().toString(), style);
        createCell(endDateRow, 1, variables.getEndDate().toString(), style);
    }

    private void writeUserHeader() {
        sheet = workbook.getSheet("User");
        Row headerRow = sheet.createRow(6);
        Row userHeaderRow = sheet.createRow(7);

        CellRangeAddress cellAddresses = new CellRangeAddress(6, 6, 0, 12);
        createCell(headerRow, 0, "Users", getHeaderCellStyleBold());
        sheet.addMergedRegion(cellAddresses);

        CellStyle style = getBoldCellStyle();
        createCell(userHeaderRow, 0, "First name", style);
        createCell(userHeaderRow, 1, "Last name", style);
        createCell(userHeaderRow, 2, "Employee number", style);
        createCell(userHeaderRow, 3, "Role", style);
        createCell(userHeaderRow, 4, "Phone", style);
        createCell(userHeaderRow, 5, "Secondary phone", style);
        createCell(userHeaderRow, 6, "Email", style);
        createCell(userHeaderRow, 7, "Secondary email", style);
        createCell(userHeaderRow, 8, "Company", style);
        createCell(userHeaderRow, 9, "Project", style);
        createCell(userHeaderRow, 10, "Labels", style);
        createCell(userHeaderRow, 11, "Days absent", style);
    }

    private void writeUser() {
        sheet = workbook.getSheet("User");
        var users = variables.getUsers();
        int rowCount = 8;
        int columnCount = 0;

        CellStyle style = getDefaultCellStyle();
        CellStyle styleCentered = getDefaultCellStyleCentered();
        for (UserExcelDTO user : users) {
            if (isNotEmpty(user.getProjects())) {
                int daysAbsentStartRowCount = rowCount;
                for (UserExcelDTO.ProjectExcelDTO project : user.getProjects()) {
                    Row row = sheet.createRow(rowCount++);
                    createCell(row, columnCount++, user.getFirstName(), style);
                    createCell(row, columnCount++, user.getLastName(), style);
                    createCell(row, columnCount++, user.getEmployeeNumber(), style);
                    createCell(row, columnCount++, user.getRole().toString(), style);
                    createCell(row, columnCount++, user.getPhone(), style);
                    createCell(row, columnCount++, user.getSecondaryPhone(), style);
                    createCell(row, columnCount++, user.getEmail(), style);
                    createCell(row, columnCount++, user.getSecondaryEmail(), style);
                    createCell(row, columnCount++, user.getCompany(), style);
                    createCell(row, columnCount++, project.getName(), style);
                    createCell(row, columnCount++, project.getLabels().toString(), style);
                    createCell(row, columnCount++, user.getDaysOff(), style);
                    columnCount = 0;
                }

                if (daysAbsentStartRowCount == rowCount-1) {
                    createCell(sheet.getRow(rowCount-1), 11, user.getDaysOff(), styleCentered);
                } else {
                    CellRangeAddress daysAbsentCell = new CellRangeAddress(daysAbsentStartRowCount, rowCount-1, 11, 11);
                    createCell(sheet.getRow(daysAbsentStartRowCount), 11, user.getDaysOff(), styleCentered);
                    sheet.addMergedRegion(daysAbsentCell);
                }
            } else {
                printUserIfEmptyProjects(rowCount++, user, style);
            }
        }
    }

    private void printUserIfEmptyProjects(int rowCount, UserExcelDTO user, CellStyle style) {
        CellStyle noProjectsStyle = getDefaultCellStyle();
        CellStyle centeredStyle = getDefaultCellStyleCentered();
        noProjectsStyle.setFillForegroundColor(IndexedColors.YELLOW1.getIndex());
        noProjectsStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        int columnCount = 0;
        Row row = sheet.createRow(rowCount);
        createCell(row, columnCount++, user.getFirstName(), style);
        createCell(row, columnCount++, user.getLastName(), style);
        createCell(row, columnCount++, user.getEmployeeNumber(), style);
        createCell(row, columnCount++, user.getRole().toString(), style);
        createCell(row, columnCount++, user.getPhone(), style);
        createCell(row, columnCount++, user.getSecondaryPhone(), style);
        createCell(row, columnCount++, user.getEmail(), style);
        createCell(row, columnCount++, user.getSecondaryEmail(), style);
        createCell(row, columnCount++, user.getCompany(), style);
        createCell(row, columnCount++, "User does not participate in any projects.", noProjectsStyle);
        createCell(row, columnCount++, "-", centeredStyle);
        createCell(row, columnCount++, user.getDaysOff(), centeredStyle);
        createCell(row, columnCount++, "-", centeredStyle);
        createCell(row, columnCount++, "-", centeredStyle);
    }

    private void writeDaysAvailableWithCapacity() {
        sheet = workbook.getSheet("User");
        Row userHeaderRow = sheet.getRow(7);

        CellStyle boldCellStyle = getBoldCellStyle();
        createCell(userHeaderRow, 12, "Capacity", boldCellStyle);
        createCell(userHeaderRow, 13, "Project utilization (days)", boldCellStyle);
        createCell(userHeaderRow, 14, "Out of (working days for period)", boldCellStyle);

        var users = variables.getUsers();
        int rowCount = 8;

        CellStyle style = getDefaultCellStyle();

        CellStyle exceededCapacityStyle = getDefaultCellStyle();
        exceededCapacityStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
        exceededCapacityStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle normalCapacityStyle = getDefaultCellStyle();
        normalCapacityStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        normalCapacityStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        normalCapacityStyle.setDataFormat(workbook.createDataFormat().getFormat("0%"));
        exceededCapacityStyle.setDataFormat(workbook.createDataFormat().getFormat("0%"));

        for (UserExcelDTO user : users) {
            if (isNotEmpty(user.getProjects())) {
                int totalCapacity = user.getProjects().stream().filter(UserExcelDTO.ProjectExcelDTO::isCapacityMode).mapToInt(UserExcelDTO.ProjectExcelDTO::getCapacity).sum();
                for (UserExcelDTO.ProjectExcelDTO project : user.getProjects()) {
                    Row row = sheet.getRow(rowCount++);
                    if (totalCapacity > 100 && project.isCapacityMode()) {
                        createCell(row, 12, (float) project.getCapacity() / 100, exceededCapacityStyle);
                    } else if (!project.isCapacityMode()){
                        createCell(row, 12, "NOT PROVIDED", style);
                    } else if (totalCapacity <= 100){
                        createCell(row, 12, (float) project.getCapacity() / 100, normalCapacityStyle);
                    }
                    var daysAvailable = user.getDaysAvailable() * ((double) project.getCapacity() / 100);
                    createCell(row, 13, Math.round(daysAvailable * 10.0) / 10.0, style);
                }
            }
            else {
                rowCount++;
            }
        }

        if (rowCount-1 == 8) {
            createCell(sheet.getRow(8), 14, variables.getWorkingDaysInPeriod(), getHeaderCellStyleBold());
        } else {
            CellRangeAddress labelsHeaderCell = new CellRangeAddress(8, rowCount-1, 14, 14);
            createCell(sheet.getRow(8), 14, variables.getWorkingDaysInPeriod(), getHeaderCellStyleBold());
            sheet.addMergedRegion(labelsHeaderCell);
        }
    }
}
