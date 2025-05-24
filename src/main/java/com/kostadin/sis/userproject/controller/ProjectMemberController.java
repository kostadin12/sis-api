package com.kostadin.sis.userproject.controller;

import com.kostadin.sis.project.model.request.GetUsersAbsencesByLabelsCommand;
import com.kostadin.sis.project.model.request.RemoveUserFromProjectCommand;
import com.kostadin.sis.project.model.response.ProjectName;
import com.kostadin.sis.userproject.ProjectMemberService;
import com.kostadin.sis.userproject.model.ProjectMember;
import com.kostadin.sis.userproject.model.ProjectMemberWithAbsences;
import com.kostadin.sis.userproject.model.ProjectMemberWithCapacities;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sis/project-members/v1.0.0")
public class ProjectMemberController implements ProjectMemberOperations{

    private final ProjectMemberService projectMemberService;

    @Override
    @GetMapping("/{id}/users")
    public List<ProjectMember> getUsers(@PathVariable("id") long id) {
        return projectMemberService.loadProjectMembers(id);
    }

    @Override
    @PostMapping("/{id}/users/assign")
    @ResponseStatus(CREATED)
    public void addUsers(@PathVariable("id") long id, @RequestBody List<String> employeeNumbers) {
        projectMemberService.addUsers(id, employeeNumbers);
    }

    @Override
    @PostMapping("/{id}/users/remove")
    @ResponseStatus(ACCEPTED)
    public void removeUser(@PathVariable("id") long projectId, @Valid @RequestBody RemoveUserFromProjectCommand removeUserFromProjectCommand) {
        projectMemberService.removeUser(projectId, removeUserFromProjectCommand);
    }

    @Override
    @PostMapping("/users-absences")
    public List<ProjectMemberWithAbsences> getUsersAbsencesByLabels(@Valid @RequestBody GetUsersAbsencesByLabelsCommand getUsersAbsencesByLabelsCommand) {
        return projectMemberService.getUsersAbsencesByLabels(getUsersAbsencesByLabelsCommand);
    }

    @Override
    @GetMapping("/users/capacities")
    public ProjectMemberWithCapacities getProjectMemberWithCapacities(@RequestParam String employeeNumber) {
        return projectMemberService.getProjectMemberWithCapacities(employeeNumber);
    }

    @Override
    @GetMapping("/users/{employeeId}")
    public List<ProjectName> getProjectsByEmployeeId(@PathVariable("employeeId") String employeeId) {
        return projectMemberService.getProjectsByUserBB(employeeId);
    }
}
