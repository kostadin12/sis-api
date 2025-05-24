package com.kostadin.sis.excel;

import com.kostadin.sis.absence.AbsenceRepository;
import com.kostadin.sis.absence.model.Absence;
import com.kostadin.sis.common.exception.ExcelReportBadRequestException;
import com.kostadin.sis.common.exception.ProjectNotFoundException;
import com.kostadin.sis.common.exception.UserNotFoundException;
import com.kostadin.sis.configurations.years.ReceiveNonWorkingDaysCommand;
import com.kostadin.sis.configurations.years.YearEntriesService;
import com.kostadin.sis.excel.request.ExportToExcelCommand;
import com.kostadin.sis.excel.request.ExportToExcelVariables;
import com.kostadin.sis.excel.request.UserExcelDTO;
import com.kostadin.sis.excel.request.UserExportCommand;
import com.kostadin.sis.exception.ErrorCode;
import com.kostadin.sis.exception.custom.CustomResponseStatusException;
import com.kostadin.sis.label.model.Label;
import com.kostadin.sis.label.model.response.LabelDTO;
import com.kostadin.sis.mapper.UserMapper;
import com.kostadin.sis.project.ProjectRepository;
import com.kostadin.sis.project.model.Project;
import com.kostadin.sis.user.UserRepository;
import com.kostadin.sis.user.model.User;
import com.kostadin.sis.userproject.model.UserProject;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ExcelGeneratorService {
    private static final String CONTENT_TYPE = "application/octet-stream";
    private static final String DATE_FORMATTER = "yyyy-MM-dd_HH";
    private static final String FILE_EXTENSION = ".xlsx";
    private static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AbsenceRepository absenceRepository;
    private final YearEntriesService yearEntriesService;


    public void exportProjectMembers(HttpServletResponse response, long projectId) throws IOException {
        var project = projectRepository.findByIdWithUsersAndLabels(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        if (isEmpty(project.getUsers())) {
            throw new ExcelReportBadRequestException("No users found in project " + project.getName());
        }

        response.setContentType(CONTENT_TYPE);
        DateFormat dateFormatter = new SimpleDateFormat(DATE_FORMATTER);
        String currentDateTime = dateFormatter.format(new Date());
        String headerValue = "attachment; filename=" + project.getName() + "-members_"+ currentDateTime + FILE_EXTENSION;
        response.setHeader(CONTENT_DISPOSITION_HEADER, headerValue);

        ExcelGenerator generator = new ExcelGenerator(new ExportToExcelVariables()
                .setProject(project));
        generator.generateMembersExcelFile(response);
    }

    public void exportProjectMembersAbsences(HttpServletResponse response, ExportToExcelCommand exportToExcelCommand) throws IOException{
        if (exportToExcelCommand.areDatesInvalid()) {
            throw new CustomResponseStatusException(BAD_REQUEST, ErrorCode.REPORT_BAD_REQUEST.getErrorCode(), ErrorCode.REPORT_BAD_REQUEST.getReason(), "Invalid dates - start date cannot be after end date.");
        }

        var project = projectRepository.findByIdWithUsersAndLabels(exportToExcelCommand.getProjectId())
                .orElseThrow(() -> new ProjectNotFoundException(exportToExcelCommand.getProjectId()));

        if (isEmpty(project.getUsers())) {
            throw new ExcelReportBadRequestException("No users found in project " + project.getName());
        }

        List<UserExcelDTO> users;
        if (project.isCapacityMode()){
            users = getUsersWithDaysOffAndCapacity(project, exportToExcelCommand.getStartDate(), exportToExcelCommand.getEndDate(), exportToExcelCommand.getPeriodCapacityCap());
        } else {
            users = getUsersWithDaysOff(project, exportToExcelCommand.getStartDate(), exportToExcelCommand.getEndDate());
        }

        if (isNotEmpty(exportToExcelCommand.getLabels())){
            users = users.stream().filter(
                    user ->
                            user.getLabels().stream().map(LabelDTO::getName).toList()
                                    .stream().anyMatch(exportToExcelCommand.getLabels()::contains)
            ).toList();
        }

        if (isNotEmpty(exportToExcelCommand.getEmployeeNumbers())){
            users = users.stream()
                    .filter(user ->
                            exportToExcelCommand.getEmployeeNumbers().contains(user.getEmployeeNumber()))
                    .map(user ->
                            user.setLabels(user.getLabels().stream().filter(label -> exportToExcelCommand.getLabels().contains(label.getName())).collect(Collectors.toSet()))).toList();
        }

        if (isEmpty(users)) {
            throw new ExcelReportBadRequestException("Zero users selected by filters.");
        }

        response.setContentType(CONTENT_TYPE);
        DateFormat dateFormatter = new SimpleDateFormat(DATE_FORMATTER);
        String currentDateTime = dateFormatter.format(new Date());
        String headerValue = "attachment; filename=" + project.getName() + "-absences-report_"+ currentDateTime + FILE_EXTENSION;
        response.setHeader(CONTENT_DISPOSITION_HEADER, headerValue);

        ExcelGenerator generator = new ExcelGenerator(new ExportToExcelVariables()
                .setProject(project)
                .setStartDate(exportToExcelCommand.getStartDate())
                .setEndDate(exportToExcelCommand.getEndDate())
                .setLabels(exportToExcelCommand.getLabels())
                .setUsers(users)
                .setPeriodCapacityCap(exportToExcelCommand.getPeriodCapacityCap())
        );
        generator.generateAbsencesExcelFile(response);
    }

    public void exportUserReport(HttpServletResponse response, UserExportCommand userExportCommand) throws IOException {
        List<User> users;
        if (isNotEmpty(userExportCommand.getEmployeeNumbers())) {
            users = userRepository.findUsersIn(userExportCommand.getEmployeeNumbers());
        } else {
            throw new CustomResponseStatusException(BAD_REQUEST, ErrorCode.URI_VARIABLE_BAD_REQUEST.getErrorCode(), ErrorCode.URI_VARIABLE_BAD_REQUEST.getReason(), "Only one of three filters must be passed - employeeNumbers, companies, systemLabels.");
        }
        if (isEmpty(users)) {
            throw new UserNotFoundException("No users match filter criteria.");
        }
        response.setContentType(CONTENT_TYPE);
        DateFormat dateFormatter = new SimpleDateFormat(DATE_FORMATTER);
        String currentDateTime = dateFormatter.format(new Date());
        String headerValue = "attachment; filename=SIS_combined_user_report_"+ currentDateTime + ".xlsx";
        response.setHeader(CONTENT_DISPOSITION_HEADER, headerValue);

        List<UserExcelDTO> userExcelDTOs = new ArrayList<>();

        users.forEach(u -> userExcelDTOs.add(getUserWithDaysOff(u, userExportCommand.getStartDate(), userExportCommand.getEndDate())));

        ExcelGenerator generator = new ExcelGenerator(new ExportToExcelVariables()
                .setUsers(userExcelDTOs)
                .setStartDate(userExportCommand.getStartDate())
                .setEndDate(userExportCommand.getEndDate())
                .setWorkingDaysInPeriod(extractWorkingDays(userExportCommand.getStartDate(), userExportCommand.getEndDate()).size())
        );
        generator.generateUserReportExcelFile(response);
    }

    private List<UserExcelDTO> getUsersWithDaysOff(Project project, LocalDate startDate, LocalDate endDate){
        List<UserExcelDTO> usersResponse = new ArrayList<>();
        var workingDays = extractWorkingDays(startDate, endDate);

        project.getUsers().stream().map(UserProject::getUser).map(userMapper::mapToExcelBody).forEach(user -> {
            var absences = absenceRepository.findAllByEmployeeNumberWithinTimeRage(user.getEmployeeNumber(), startDate, endDate);

            List<LocalDate> absentDays = extractDatesFromAbsences(absences)
                    .stream()
                    .filter(workingDays::contains)
                    .toList();

            user.setDaysOff(absentDays.size());
            user.setDaysAvailable(workingDays.size() - absentDays.size());

            usersResponse.add(user);
        });

        return usersResponse;
    }

    private List<UserExcelDTO> getUsersWithDaysOffAndCapacity(Project project, LocalDate startDate, LocalDate endDate, int periodCapacityCap) {
        List<UserExcelDTO> usersResponse = new ArrayList<>();

        for (UserProject userProject : project.getUsers()) {
            usersResponse.add(userMapper.mapToExcelBody(userProject.getUser())
                    .setProjects(List.of(new UserExcelDTO.ProjectExcelDTO()
                            .setName(userProject.getProject().getName())
                            .setCapacity(userProject.getCapacity()))));
        }

        var workingDays = extractWorkingDays(startDate, endDate);

        usersResponse.forEach(user -> {
            var absences = absenceRepository.findAllByEmployeeNumberWithinTimeRage(user.getEmployeeNumber(), startDate, endDate);

            List<LocalDate> absentDays = extractDatesFromAbsences(absences)
                    .stream()
                    .filter(workingDays::contains)
                    .toList();

            user.setDaysOff(absentDays.size());
            user.setDaysAvailable((workingDays.size() * ((double) periodCapacityCap / 100)) - absentDays.size());
        });

        return usersResponse;
    }

    private UserExcelDTO getUserWithDaysOff(User user, LocalDate startDate, LocalDate endDate){
        var usersResponse = userMapper.mapToExcelBody(user);

        var workingDays = extractWorkingDays(startDate, endDate);

        var absences = absenceRepository.findAllByEmployeeNumberWithinTimeRage(user.getEmployeeNumber(), startDate, endDate);

        List<LocalDate> absentDays = extractDatesFromAbsences(absences)
                .stream()
                .filter(workingDays::contains)
                .toList();

        usersResponse.setDaysOff(absentDays.size());
        usersResponse.setDaysAvailable(workingDays.size() - absentDays.size());
        usersResponse.setTotalWorkingDays(workingDays.size());

        List<UserExcelDTO.ProjectExcelDTO> projectExcelDTOs = new ArrayList<>();

        for (UserProject userProject : user.getProjects()) {
            projectExcelDTOs.add(new UserExcelDTO.ProjectExcelDTO()
                    .setName(userProject.getProject().getName())
                    .setCapacity(userProject.getCapacity())
                    .setLabels(user.getLabels().stream().filter(label -> userProject.getProject().getLabels().contains(label)).map(Label::getName).toList())
                    .setCapacityMode(userProject.getProject().isCapacityMode())
            );
        }

        usersResponse.setProjects(projectExcelDTOs);

        return usersResponse;
    }

    private List<LocalDate> extractDatesFromAbsences(List<Absence> absences){
        List<LocalDate> dates = new ArrayList<>();
        absences.forEach(absence -> dates.addAll(absence.getStartDate().datesUntil(absence.getEndDate().plusDays(1)).toList()));
        return dates;
    }

    private List<LocalDate> extractWorkingDays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            log.warn("Invalid date range for working days calculation: {} to {}", startDate, endDate);
            return new ArrayList<>();
        }

        List<LocalDate> allDays = startDate.datesUntil(endDate.plusDays(1))
                .collect(Collectors.toList());

        Set<LocalDate> nonWorkingDays = new HashSet<>();

        int startYear = startDate.getYear();
        int endYear = endDate.getYear();

        for (int year = startYear; year <= endYear; year++) {
            try {
                List<String> yearNonWorkingDays = yearEntriesService.getNonWorkingDays(
                        new ReceiveNonWorkingDaysCommand().setYear(String.valueOf(year)));

                for (String dateStr : yearNonWorkingDays) {
                    try {
                        LocalDate date = LocalDate.parse(dateStr);
                        nonWorkingDays.add(date);
                    } catch (Exception e) {
                        log.warn("Failed to parse non-working day: {}", dateStr, e);
                    }
                }
            } catch (Exception e) {
                log.error("Error retrieving non-working days for year {}", year, e);

                int finalYear = year;
                allDays.stream()
                        .filter(date -> date.getYear() == finalYear)
                        .filter(date -> date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY)
                        .forEach(nonWorkingDays::add);
            }
        }

        List<LocalDate> workingDays = allDays.stream()
                .filter(date -> !nonWorkingDays.contains(date))
                .collect(Collectors.toList());

        log.info("Calculated {} working days between {} and {}", workingDays.size(), startDate, endDate);

        return workingDays;
    }
}
