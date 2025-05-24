package com.kostadin.sis.user.controller;

import com.kostadin.sis.absence.model.response.AbsenceDTO;
import com.kostadin.sis.user.UserService;
import com.kostadin.sis.user.model.request.AssignRemoveProjectLabelCommand;
import com.kostadin.sis.user.model.request.AssignRemoveSystemLabelCommand;
import com.kostadin.sis.user.model.request.FilterUsersForReportCommand;
import com.kostadin.sis.user.model.request.UpdateUserCommand;
import com.kostadin.sis.user.model.response.UserDTO;
import com.kostadin.sis.user.model.response.UserNameEmployeeNumber;
import com.kostadin.sis.user.model.response.UserWithSystemLabelsAndCompany;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sis/users/v1.0.0")
public class UserController implements UserOperations{
    private final UserService userService;

    @Override
    @GetMapping
    @Deprecated(forRemoval = true)
    public List<UserNameEmployeeNumber> getAll(){
        return userService.getUsers();
    }

    @Override
    @GetMapping("/{employeeId}")
    public UserDTO getUser(@PathVariable("employeeId") String employeeId) {
        return userService.getUser(employeeId);
    }

    @Override
    @GetMapping("/find")
    public List<UserNameEmployeeNumber> findUsers(@RequestParam String filter, @RequestParam(required = false) String absentUserEmployeeNumber) {
        return userService.findUsersLike(filter, absentUserEmployeeNumber);
    }

    @Override
    @GetMapping("/find-beneficiary-candidates")
    public List<UserNameEmployeeNumber> findBeneficiaryCandidates(String filter, long projectId) {
        return userService.findBeneficiaryCandidatesInProject(filter, projectId);
    }

    @Override
    @PatchMapping("/{employeeId}")
    @ResponseStatus(ACCEPTED)
    public void updateUser(@PathVariable String employeeId, @Valid @RequestBody UpdateUserCommand updateUserCommand) {
        userService.updateUser(employeeId, updateUserCommand);
    }

    @Override
    @ResponseStatus(NO_CONTENT)
    @DeleteMapping
    @Deprecated(forRemoval = true)
    public void deleteUser(@RequestParam String employeeId) {
        userService.deleteUser(employeeId);
    }

    @Override
    @GetMapping("/absences")
    public List<AbsenceDTO> getAbsencesByEmployeeId(@RequestParam String employeeId) {
        return userService.getAbsencesByUserBB(employeeId);
    }

    @Override
    @PostMapping("/project-labels/assign")
    public ResponseEntity<UserDTO> assignProjectLabel(@Valid @RequestBody AssignRemoveProjectLabelCommand requestBody) {
        return ResponseEntity.ok(userService.assignProjectLabel(requestBody));
    }

    @Override
    @PostMapping("/system-labels/assign")
    @Deprecated(forRemoval = true)
    public ResponseEntity<UserDTO> assignSystemLabel(@Valid @RequestBody AssignRemoveSystemLabelCommand requestBody) {
        return ResponseEntity.ok(userService.assignSystemLabel(requestBody));
    }

    @Override
    @PatchMapping("/project-labels/remove")
    @ResponseStatus(NO_CONTENT)
    public ResponseEntity<UserDTO> removeProjectLabel(@Valid @RequestBody AssignRemoveProjectLabelCommand requestBody) {
        return ResponseEntity.ok(userService.removeProjectLabel(requestBody));
    }

    @Override
    @PatchMapping("/system-labels/remove")
    @ResponseStatus(NO_CONTENT)
    @Deprecated(forRemoval = true)
    public ResponseEntity<UserDTO> removeSystemLabel(@RequestBody AssignRemoveSystemLabelCommand requestBody) {
        return ResponseEntity.ok(userService.removeSystemLabel(requestBody));
    }

    @Override
    @GetMapping("/companies")
    public Set<String> getAllCompanies() {
        return userService.findCompanies();
    }

    @Override
    @PostMapping("/filter/companies-system-labels")
    public Set<UserWithSystemLabelsAndCompany> filterUsersByCompaniesAndSystemLabels(@RequestBody FilterUsersForReportCommand filterUsersForReportCommand) {
        return userService.filterUsersByCompaniesAndSystemLabels(filterUsersForReportCommand);
    }
}
