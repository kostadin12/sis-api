package com.kostadin.sis.mapper;

import com.kostadin.sis.project.model.Project;
import com.kostadin.sis.project.model.request.SaveProjectCommand;
import com.kostadin.sis.project.model.response.ProjectDTO;
import com.kostadin.sis.project.model.response.ProjectName;
import com.kostadin.sis.user.model.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface ProjectMapper {

    ProjectDTO toDto(Project project);

    List<ProjectDTO> toDto(List<Project> projectList);

    Project toEntity(SaveProjectCommand saveProjectCommand);

    ProjectName toProjectName(Project project);

    default String mapProjectOwner(User user){
        if (user != null){
            return user.getEmployeeNumber();
        }
        return null;
    }
}
