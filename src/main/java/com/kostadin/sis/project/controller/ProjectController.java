package com.kostadin.sis.project.controller;

import com.kostadin.sis.project.model.request.SaveProjectCommand;
import com.kostadin.sis.project.model.request.UpdateProjectCommand;
import com.kostadin.sis.project.model.response.ProjectDTO;
import com.kostadin.sis.project.model.response.ProjectName;
import com.kostadin.sis.project.service.ProjectService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sis/projects/v1.0.0")
public class ProjectController implements ProjectOperations {

    private final ProjectService projectService;

    @Override
    @ResponseStatus(CREATED)
    @PostMapping
    public ProjectDTO saveProject(@Valid @RequestBody SaveProjectCommand saveProjectCommand){
        return projectService.saveProject(saveProjectCommand);
    }

    @Override
    @GetMapping
    public List<ProjectName> getProjects(){
        return projectService.getProjects();
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ProjectDTO> getProjectById(@PathVariable("id") long id){
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @Override
    @GetMapping("/find")
    @Deprecated(forRemoval = true)
    public List<ProjectName> findProjects(@NotEmpty @RequestParam String filter) {
        return projectService.findProjects(filter);
    }

    @Override
    @PatchMapping("/{id}")
    @ResponseStatus(ACCEPTED)
    public ProjectDTO updateProject(@PathVariable("id") long id, @RequestParam("projectOwnerId") String projectOwnerEmployeeNumber, @Valid @RequestBody UpdateProjectCommand updateProjectCommand){
        return projectService.updateProject(id, projectOwnerEmployeeNumber, updateProjectCommand);
    }

    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public void deleteProject(@PathVariable("id") long id, @RequestParam("projectOwnerId") String projectOwnerEmployeeNumber) {
        projectService.deleteProject(id, projectOwnerEmployeeNumber);
    }
}
