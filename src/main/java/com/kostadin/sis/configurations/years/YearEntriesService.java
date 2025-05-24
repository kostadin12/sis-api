package com.kostadin.sis.configurations.years;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.kostadin.sis.exception.ErrorCode;
import com.kostadin.sis.exception.custom.CustomResponseStatusException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class YearEntriesService {
    private static final String APPLICATION_NAME = "Scrum Information System";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String BULGARIAN_HOLIDAYS_CALENDAR_ID = "en.bulgarian#holiday@group.v.calendar.google.com";
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    @Value("${google.calendar.api.enabled:true}")
    private boolean googleCalendarApiEnabled;

    private final YearEntriesRepository yearEntriesRepository;

    public List<String> getNonWorkingDays(ReceiveNonWorkingDaysCommand command) {
        log.info("Receiving non-working days for year " + command.getYear());
        var yearEntry = yearEntriesRepository.findByYear(command.getYear())
                .orElseGet(() -> createNewYear(command.getYear()));

        return Arrays.stream(yearEntry.getNonWorkingDays().split(", ")).toList();
    }

    public void deleteYear(ReceiveNonWorkingDaysCommand command) {
        var yearEntry = yearEntriesRepository.findByYear(command.getYear())
                .orElseThrow(() -> new CustomResponseStatusException(HttpStatus.NOT_FOUND, ErrorCode.CONFIGURATION_NOT_FOUND.getErrorCode(), ErrorCode.CONFIGURATION_NOT_FOUND.getReason(), "Year not found in DB."));
        yearEntriesRepository.delete(yearEntry);
    }

    private YearEntry createNewYear(String year) {
        log.info("Creating new YearEntry entity - " + year);
        var yearEntry = new YearEntry()
                .setYear(year)
                .setNonWorkingDays(getNonWorkingDays(Year.parse(year)));

        return yearEntriesRepository.save(yearEntry);
    }

    private String getNonWorkingDays(Year year) {
        if (!googleCalendarApiEnabled) {
            log.warn("Google Calendar API is disabled. Using fallback method.");
            return getFallbackNonWorkingDays(year);
        }

        try {
            // Get all holidays for the specified year from Google Calendar API
            Set<String> nonWorkingDays = new HashSet<>(getHolidaysForYear(year));

            // Add all weekends for the year
            nonWorkingDays.addAll(getWeekendsForYear(year));

            // Sort dates and join them with comma
            List<String> sortedNonWorkingDays = nonWorkingDays.stream()
                    .sorted()
                    .collect(Collectors.toList());

            return String.join(", ", sortedNonWorkingDays);
        } catch (Exception e) {
            log.error("Error fetching non-working days from Google Calendar API", e);
            return getFallbackNonWorkingDays(year);
        }
    }

    private String getFallbackNonWorkingDays(Year year) {
        log.info("Using fallback method to calculate non-working days");
        try {
            // Get Bulgarian holidays using hardcoded values
            Set<String> nonWorkingDays = new HashSet<>(getHardcodedBulgarianHolidays(year));

            // Add all weekends for the year
            nonWorkingDays.addAll(getWeekendsForYear(year));

            // Sort dates and join them with comma
            List<String> sortedNonWorkingDays = nonWorkingDays.stream()
                    .sorted()
                    .collect(Collectors.toList());

            return String.join(", ", sortedNonWorkingDays);
        } catch (Exception e) {
            log.error("Error in fallback method for non-working days", e);

            // Last resort: just return weekends
            try {
                List<String> weekends = getWeekendsForYear(year);
                Collections.sort(weekends);
                return String.join(", ", weekends);
            } catch (Exception ex) {
                log.error("Error calculating weekends", ex);
                return "";
            }
        }
    }

    private List<String> getHardcodedBulgarianHolidays(Year year) {
        List<String> holidays = new ArrayList<>();
        int y = year.getValue();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        // Fixed Bulgarian holidays
        holidays.add(LocalDate.of(y, Month.JANUARY, 1).format(formatter));      // New Year's Day
        holidays.add(LocalDate.of(y, Month.MARCH, 3).format(formatter));        // Liberation Day
        holidays.add(LocalDate.of(y, Month.MAY, 1).format(formatter));          // Labor Day
        holidays.add(LocalDate.of(y, Month.MAY, 6).format(formatter));          // St. George's Day
        holidays.add(LocalDate.of(y, Month.MAY, 24).format(formatter));         // Bulgarian Culture Day
        holidays.add(LocalDate.of(y, Month.SEPTEMBER, 6).format(formatter));    // Unification Day
        holidays.add(LocalDate.of(y, Month.SEPTEMBER, 22).format(formatter));   // Independence Day
        holidays.add(LocalDate.of(y, Month.DECEMBER, 24).format(formatter));    // Christmas Eve
        holidays.add(LocalDate.of(y, Month.DECEMBER, 25).format(formatter));    // Christmas Day
        holidays.add(LocalDate.of(y, Month.DECEMBER, 26).format(formatter));    // Second Day of Christmas

        return holidays;
    }

    private List<String> getHolidaysForYear(Year year) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        // Set time range for the whole year
        DateTime timeMin = new DateTime(Date.from(year.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        DateTime timeMax = new DateTime(Date.from(year.atDay(year.length()).atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()));

        // Get Bulgarian holidays
        Events events = service.events().list(BULGARIAN_HOLIDAYS_CALENDAR_ID)
                .setTimeMin(timeMin)
                .setTimeMax(timeMax)
                .setSingleEvents(true)
                .setOrderBy("startTime")
                .execute();

        List<String> holidays = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        // Process events
        List<Event> items = events.getItems();
        for (Event event : items) {
            DateTime start = event.getStart().getDateTime();
            if (start == null) {
                // All-day events use date instead of dateTime
                start = event.getStart().getDate();
            }

            // Get the date string
            String dateString = start.toString();
            if (dateString.contains("T")) {
                dateString = dateString.split("T")[0];
            }

            try {
                LocalDate holidayDate = LocalDate.parse(dateString);
                holidays.add(holidayDate.format(formatter));
                log.debug("Added holiday: {} - {}", event.getSummary(), holidayDate);
            } catch (Exception e) {
                log.warn("Could not parse date: " + dateString, e);
            }
        }

        return holidays;
    }

    private List<String> getWeekendsForYear(Year year) {
        List<String> weekends = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        LocalDate date = year.atDay(1);
        LocalDate endDate = year.atDay(year.length());

        while (!date.isAfter(endDate)) {
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                weekends.add(date.format(formatter));
            }
            date = date.plusDays(1);
        }

        return weekends;
    }

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = YearEntriesService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public void updateYearEntries() {
        log.info("Updating year entries.");

        var now = LocalDate.now();
        var currentYear = String.valueOf(now.getYear());
        var nextYear = String.valueOf(now.plusYears(1).getYear());

        var currentYearEntry = yearEntriesRepository.findByYear(currentYear)
                .orElseGet(() -> createNewYear(currentYear));

        var nextYearEntry = yearEntriesRepository.findByYear(nextYear)
                .orElseGet(() -> createNewYear(nextYear));

        currentYearEntry.setNonWorkingDays(getNonWorkingDays(Year.parse(currentYearEntry.getYear())));
        nextYearEntry.setNonWorkingDays(getNonWorkingDays(Year.parse(nextYearEntry.getYear())));

        yearEntriesRepository.saveAll(List.of(currentYearEntry, nextYearEntry));
        log.info("Successfully updated year entries.");
    }

    public void deleteYearsOlderThan(int year) {
        log.info("Deleting year entries older than: {}", year);

        yearEntriesRepository.deleteAll(
                yearEntriesRepository.findAll().stream().filter(entry -> Integer.parseInt(entry.getYear()) < year).toList()
        );
    }
}