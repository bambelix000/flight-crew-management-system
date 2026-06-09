package com.tab.flight_crew_manager.user;

import com.tab.flight_crew_manager.crew_assignment.AssignmentStatus;
import com.tab.flight_crew_manager.crew_assignment.CrewAssignment;
import com.tab.flight_crew_manager.duty.Duty;
import com.tab.flight_crew_manager.flight.Flight;
import com.tab.flight_crew_manager.user.dto.StatsDataDto;
import com.tab.flight_crew_manager.user.dto.UserUpdateDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final ZoneId REPORT_ZONE = ZoneId.of("Europe/Warsaw");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(User user) {
        if (userRepository.existsByLogin(user.getLogin())) {
            throw new IllegalStateException("Login jest już zajęty");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public StatsDataDto getStats(Principal principal) {
        String login = principal.getName();
        User user = userRepository.findByLogin(login).orElseThrow();
        LocalDateTime now = LocalDateTime.now();

        StatsDataDto dto = new StatsDataDto();
        dto.setId(user.getId());
        dto.setLogin(user.getLogin());
        dto.setName(user.getName());
        dto.setSurname(user.getSurname());
        dto.setPhoneNumber(user.getPhoneNumber());

        int rolling20Days = user.getAssignments().stream()
                .filter(a -> a.getStatus() != AssignmentStatus.REJECTED)
                .filter(a -> a.getDuty().getDutyStartTime() != null && a.getDuty().getDutyStartTime().isAfter(now.minusDays(20)))
                .mapToInt(this::getTrackedAirTimeMinutes).sum();

        int rolling365Days = user.getAssignments().stream()
                .filter(a -> a.getStatus() != AssignmentStatus.REJECTED)
                .filter(a -> a.getDuty().getDutyStartTime() != null && a.getDuty().getDutyStartTime().isAfter(now.minusDays(365)))
                .mapToInt(this::getTrackedAirTimeMinutes).sum();

        dto.setTwentyDaysAirTime(rolling20Days);
        dto.setAnnualAirTime(rolling365Days);

        dto.setTotalAirBorneTimeMinutes(getTotalTrackedAirTimeMinutes(user));
        dto.setTotalWorkTimeMinutes(getTotalTrackedWorkTimeMinutes(user));
        dto.setTotalDutyTimeMinutes(getTotalTrackedDutyTimeMinutes(user));
        dto.setIncapacityCounter(user.getIncapacityCounter());
        dto.setTotalDutiesCount(user.getAssignments().size());

        String topRole = user.getAssignments().stream()
                .filter(a -> a.getStatus() != AssignmentStatus.REJECTED)
                .map(a -> a.getRoleOnDuty().name())
                .reduce(java.util.function.BinaryOperator.maxBy((role1, role2) -> 1))
                .orElse("Brak lotów");
        dto.setMostFrequentRole(topRole);

        return dto;
    }

    public void updateUser(Long id, UserUpdateDto updatedData) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Użytkownik nie istnieje"));

        user.setName(updatedData.getName());
        user.setSurname(updatedData.getSurname());
        user.setLogin(updatedData.getLogin());
        user.setPhoneNumber(updatedData.getPhoneNumber());
        user.setUserRole(updatedData.getUserRole());
        userRepository.save(user);
    }

    public void updatePhone(Principal principal, String newPhone) {
        User user = userRepository.findByLogin(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Użytkownik nie istnieje"));
        user.setPhoneNumber(newPhone);
        userRepository.save(user);
    }

    public User login(String login, String password) {
        return userRepository.findByLogin(login)
                .filter(u -> u.getPassword().equals(password))
                .orElseThrow(() -> new IllegalStateException("Błędny login lub hasło"));
    }
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Nie znaleziono użytkownika"));
    }

    public byte[] generateMyReport(Principal principal) {
        User user = userRepository.findByLogin(principal.getName()).orElseThrow();
        LocalDateTime generatedAt = LocalDateTime.now(REPORT_ZONE);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        int rolling20Days = user.getAssignments().stream()
                .filter(a -> a.getStatus() != AssignmentStatus.REJECTED)
                .filter(a -> a.getDuty().getDutyStartTime() != null && a.getDuty().getDutyStartTime().isAfter(generatedAt.minusDays(20)))
                .mapToInt(this::getTrackedAirTimeMinutes).sum();

        int rolling365Days = user.getAssignments().stream()
                .filter(a -> a.getStatus() != AssignmentStatus.REJECTED)
                .filter(a -> a.getDuty().getDutyStartTime() != null && a.getDuty().getDutyStartTime().isAfter(generatedAt.minusDays(365)))
                .mapToInt(this::getTrackedAirTimeMinutes).sum();

        List<CrewAssignment> assignments = user.getAssignments().stream()
                .filter(a -> a.getStatus() != AssignmentStatus.REJECTED)
                .sorted(Comparator.comparing(
                        a -> a.getDuty().getDutyStartTime(),
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        List<String> lines = new ArrayList<>();
        lines.add("Flight Crew Manager - raport uzytkownika");
        lines.add("Data i godzina generowania: " + generatedAt.format(dateTimeFormatter));
        lines.add("Statystyki na dzien: " + generatedAt.toLocalDate());
        lines.add("");
        lines.add("Uzytkownik: " + valueOrDash(user.getName()) + " " + valueOrDash(user.getSurname()) + " (" + user.getLogin() + ")");
        lines.add("Telefon: " + valueOrDash(user.getPhoneNumber()));
        lines.add("Rola konta: " + user.getUserRole());
        lines.add("");
        lines.add("Statystyki");
        lines.add("- Czas sluzb: " + formatMinutes(getTotalTrackedDutyTimeMinutes(user)));
        lines.add("- Czas pracy: " + formatMinutes(getTotalTrackedWorkTimeMinutes(user)));
        lines.add("- Czas w powietrzu: " + formatMinutes(getTotalTrackedAirTimeMinutes(user)));
        lines.add("- Czas lotu FTL z ostatnich 20 dni: " + formatMinutes(rolling20Days) + " / 90h");
        lines.add("- Czas lotu FTL w roku kalendarzowym: " + formatMinutes(rolling365Days) + " / 900h");
        lines.add("- Zgloszone incapacity: " + user.getIncapacityCounter());
        lines.add("- Liczba sluzb w raporcie: " + assignments.size());
        lines.add("");
        lines.add("Sluzby uzytkownika");

        if (assignments.isEmpty()) {
            lines.add("Brak sluzb do wyswietlenia.");
        }

        for (CrewAssignment assignment : assignments) {
            Duty duty = assignment.getDuty();
            lines.add("");
            lines.add("Sluzba #" + duty.getId());
            lines.add("- Rola w sluzbie: " + assignment.getRoleOnDuty());
            lines.add("- Status: " + assignment.getStatus());
            lines.add("- Planowany start: " + formatDateTime(duty.getDutyStartTime(), dateTimeFormatter));
            lines.add("- Planowany koniec: " + formatDateTime(duty.getDutyEndTime(), dateTimeFormatter));
            lines.add("- Rzeczywisty start: " + formatDateTime(assignment.getActualStartTime(), dateTimeFormatter));
            lines.add("- Rzeczywisty stop: " + formatDateTime(assignment.getActualEndTime(), dateTimeFormatter));
            lines.add("- Czas sluzby zaliczony: " + formatMinutes(getTrackedDutyDurationMinutes(assignment)));
            lines.add("- Czas pracy zaliczony: " + formatMinutes(getTrackedWorkTimeMinutes(assignment)));
            lines.add("- Czas w powietrzu zaliczony: " + formatMinutes(getTrackedAirTimeMinutes(assignment)));
            lines.add("- Loty w sluzbie:");

            if (duty.getFlights().isEmpty()) {
                lines.add("  Brak lotow.");
            } else {
                duty.getFlights().stream()
                        .sorted(Comparator.comparing(Flight::getDepartureTime, Comparator.nullsLast(Comparator.naturalOrder())))
                        .forEach(flight -> lines.add("  " + valueOrDash(flight.getFlightNumber())
                                + " | " + airportCode(flight.getDepartureAirport()) + " - " + airportCode(flight.getArrivalAirport())
                                + " | " + formatDateTime(flight.getDepartureTime(), dateTimeFormatter)
                                + " -> " + formatDateTime(flight.getArrivalTime(), dateTimeFormatter)
                                + " | " + formatMinutes(flight.getDurationMinutes())));
            }
        }

        return buildPdf(lines);
    }

    public List<StatsDataDto> getAllCrewStats() {
        LocalDateTime now = LocalDateTime.now();

        return userRepository.findAll().stream()
                .filter(u -> u.getUserRole() == UserRole.CREWMEMBER)
                .map(user -> {
                    StatsDataDto dto = new StatsDataDto();
                    dto.setId(user.getId());
                    dto.setLogin(user.getLogin());
                    dto.setName(user.getName());
                    dto.setSurname(user.getSurname());
                    dto.setPhoneNumber(user.getPhoneNumber());

                    int rolling20Days = user.getAssignments().stream()
                            .filter(a -> a.getStatus() != AssignmentStatus.REJECTED)
                            .filter(a -> a.getDuty().getDutyStartTime() != null && a.getDuty().getDutyStartTime().isAfter(now.minusDays(20)))
                            .mapToInt(this::getTrackedAirTimeMinutes).sum();

                    int rolling365Days = user.getAssignments().stream()
                            .filter(a -> a.getStatus() != AssignmentStatus.REJECTED)
                            .filter(a -> a.getDuty().getDutyStartTime() != null && a.getDuty().getDutyStartTime().isAfter(now.minusDays(365)))
                            .mapToInt(this::getTrackedAirTimeMinutes).sum();

                    dto.setTwentyDaysAirTime(rolling20Days);
                    dto.setAnnualAirTime(rolling365Days);

                    dto.setTotalAirBorneTimeMinutes(getTotalTrackedAirTimeMinutes(user));
                    dto.setTotalWorkTimeMinutes(getTotalTrackedWorkTimeMinutes(user));
                    dto.setTotalDutyTimeMinutes(getTotalTrackedDutyTimeMinutes(user));
                    dto.setIncapacityCounter(user.getIncapacityCounter());
                    dto.setTotalDutiesCount(user.getAssignments().size());

                    return dto;
                })
                .collect(Collectors.toList());
    }

    private int getTotalTrackedAirTimeMinutes(User user) {
        return user.getAssignments().stream()
                .filter(a -> a.getStatus() != AssignmentStatus.REJECTED)
                .mapToInt(this::getTrackedAirTimeMinutes)
                .sum();
    }

    private int getTotalTrackedWorkTimeMinutes(User user) {
        return user.getAssignments().stream()
                .filter(a -> a.getStatus() != AssignmentStatus.REJECTED)
                .mapToInt(this::getTrackedWorkTimeMinutes)
                .sum();
    }

    private int getTotalTrackedDutyTimeMinutes(User user) {
        return user.getAssignments().stream()
                .filter(a -> a.getStatus() != AssignmentStatus.REJECTED)
                .mapToInt(this::getTrackedDutyDurationMinutes)
                .sum();
    }

    private int getTrackedAirTimeMinutes(CrewAssignment assignment) {
        if (assignment.getActualStartTime() != null && assignment.getActualEndTime() != null) {
            return Math.min(getPlannedAirTimeMinutes(assignment.getDuty()), getTrackedWorkTimeMinutes(assignment));
        }
        return getPlannedAirTimeMinutes(assignment.getDuty());
    }

    private int getTrackedDutyDurationMinutes(CrewAssignment assignment) {
        if (assignment.getActualStartTime() != null && assignment.getActualEndTime() != null) {
            return (int) Duration.between(assignment.getActualStartTime(), assignment.getActualEndTime()).toMinutes();
        }
        Duty duty = assignment.getDuty();
        if (duty.getDutyStartTime() == null || duty.getDutyEndTime() == null) {
            return 0;
        }
        return (int) Duration.between(duty.getDutyStartTime(), duty.getDutyEndTime()).toMinutes();
    }

    private int getTrackedWorkTimeMinutes(CrewAssignment assignment) {
        if (assignment.getActualStartTime() != null && assignment.getActualEndTime() != null) {
            int actualDutyMinutes = (int) Duration.between(assignment.getActualStartTime(), assignment.getActualEndTime()).toMinutes();
            return Math.min(getPlannedWorkTimeMinutes(assignment.getDuty()), Math.max(0, actualDutyMinutes - 60));
        }
        return getPlannedWorkTimeMinutes(assignment.getDuty());
    }

    private int getPlannedAirTimeMinutes(Duty duty) {
        return duty.getAirTimeMinutes() != null ? duty.getAirTimeMinutes() : 0;
    }

    private int getPlannedWorkTimeMinutes(Duty duty) {
        return duty.getWorkTimeMinutes() != null ? duty.getWorkTimeMinutes() : 0;
    }

    private String formatMinutes(Integer minutes) {
        int safeMinutes = minutes != null ? Math.max(0, minutes) : 0;
        return (safeMinutes / 60) + "h " + (safeMinutes % 60) + "m";
    }

    private String formatDateTime(LocalDateTime dateTime, DateTimeFormatter formatter) {
        return dateTime != null ? dateTime.format(formatter) : "brak";
    }

    private String airportCode(com.tab.flight_crew_manager.airport.Airport airport) {
        return airport != null ? airport.getAirportCode() : "?";
    }

    private String valueOrDash(String value) {
        return value != null && !value.isBlank() ? value : "-";
    }

    private byte[] buildPdf(List<String> sourceLines) {
        List<String> wrappedLines = new ArrayList<>();
        for (String line : sourceLines) {
            wrappedLines.addAll(wrapLine(normalizeText(line), 95));
        }

        List<List<String>> pages = new ArrayList<>();
        int linesPerPage = 50;
        for (int i = 0; i < wrappedLines.size(); i += linesPerPage) {
            pages.add(wrappedLines.subList(i, Math.min(i + linesPerPage, wrappedLines.size())));
        }
        if (pages.isEmpty()) {
            pages.add(List.of(""));
        }

        int objectCount = 3 + pages.size() * 2;
        String[] objects = new String[objectCount + 1];
        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < pages.size(); i++) {
            kids.append(4 + i * 2).append(" 0 R ");
        }

        objects[1] = "<< /Type /Catalog /Pages 2 0 R >>";
        objects[2] = "<< /Type /Pages /Kids [" + kids + "] /Count " + pages.size() + " >>";
        objects[3] = "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>";

        for (int i = 0; i < pages.size(); i++) {
            int pageObjectId = 4 + i * 2;
            int contentObjectId = pageObjectId + 1;
            String content = buildPdfPageContent(pages.get(i));
            int length = content.getBytes(StandardCharsets.ISO_8859_1).length;

            objects[pageObjectId] = "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                    + "/Resources << /Font << /F1 3 0 R >> >> /Contents " + contentObjectId + " 0 R >>";
            objects[contentObjectId] = "<< /Length " + length + " >>\nstream\n" + content + "endstream";
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        writePdf(output, "%PDF-1.4\n");

        int[] offsets = new int[objectCount + 1];
        for (int i = 1; i <= objectCount; i++) {
            offsets[i] = output.size();
            writePdf(output, i + " 0 obj\n" + objects[i] + "\nendobj\n");
        }

        int xrefOffset = output.size();
        writePdf(output, "xref\n0 " + (objectCount + 1) + "\n");
        writePdf(output, "0000000000 65535 f \n");
        for (int i = 1; i <= objectCount; i++) {
            writePdf(output, String.format("%010d 00000 n \n", offsets[i]));
        }
        writePdf(output, "trailer\n<< /Size " + (objectCount + 1) + " /Root 1 0 R >>\n");
        writePdf(output, "startxref\n" + xrefOffset + "\n%%EOF");

        return output.toByteArray();
    }

    private String buildPdfPageContent(List<String> lines) {
        StringBuilder content = new StringBuilder();
        content.append("BT\n/F1 10 Tf\n14 TL\n50 800 Td\n");
        for (String line : lines) {
            content.append("(").append(escapePdfText(line)).append(") Tj\nT*\n");
        }
        content.append("ET\n");
        return content.toString();
    }

    private List<String> wrapLine(String line, int maxLength) {
        List<String> lines = new ArrayList<>();
        if (line.length() <= maxLength) {
            lines.add(line);
            return lines;
        }

        StringBuilder current = new StringBuilder();
        for (String word : line.split(" ")) {
            if (word.length() > maxLength) {
                if (!current.isEmpty()) {
                    lines.add(current.toString());
                    current = new StringBuilder();
                }
                for (int i = 0; i < word.length(); i += maxLength) {
                    lines.add(word.substring(i, Math.min(i + maxLength, word.length())));
                }
                continue;
            }

            if (!current.isEmpty() && current.length() + word.length() + 1 > maxLength) {
                lines.add(current.toString());
                current = new StringBuilder();
            }
            if (!current.isEmpty()) {
                current.append(" ");
            }
            current.append(word);
        }

        if (!current.isEmpty()) {
            lines.add(current.toString());
        }
        return lines;
    }

    private String normalizeText(String text) {
        String normalized = text
                .replace("ą", "a").replace("ć", "c").replace("ę", "e").replace("ł", "l")
                .replace("ń", "n").replace("ó", "o").replace("ś", "s").replace("ź", "z")
                .replace("ż", "z").replace("Ą", "A").replace("Ć", "C").replace("Ę", "E")
                .replace("Ł", "L").replace("Ń", "N").replace("Ó", "O").replace("Ś", "S")
                .replace("Ź", "Z").replace("Ż", "Z");
        StringBuilder ascii = new StringBuilder();
        for (int i = 0; i < normalized.length(); i++) {
            char character = normalized.charAt(i);
            ascii.append(character >= 32 && character <= 126 ? character : "?");
        }
        return ascii.toString();
    }

    private String escapePdfText(String text) {
        return text.replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }

    private void writePdf(ByteArrayOutputStream output, String value) {
        output.writeBytes(value.getBytes(StandardCharsets.ISO_8859_1));
    }
}
