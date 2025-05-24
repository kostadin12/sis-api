package com.kostadin.sis.mapper;

import com.kostadin.sis.absence.model.Absence;
import com.kostadin.sis.absence.model.response.AbsenceDTO;
import com.kostadin.sis.project.model.response.ProjectNameWithCapacity;
import com.kostadin.sis.userproject.model.ProjectMember;
import com.kostadin.sis.userproject.model.ProjectMemberWithAbsences;
import com.kostadin.sis.userproject.model.ProjectMemberWithCapacities;
import com.kostadin.sis.userproject.model.UserProject;
import com.kostadin.sis.label.model.Label;
import com.kostadin.sis.label.model.response.LabelDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Mapper
public interface ProjectMemberMapper {

    List<ProjectMember> toProjectMember(List<UserProject> source);

    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.employeeNumber", target = "employeeNumber")
    @Mapping(source = "user.phone", target = "phone")
    @Mapping(source = "user.secondaryPhone", target = "secondaryPhone")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.secondaryEmail", target = "secondaryEmail")
    @Mapping(source = "user.company", target = "company")
    @Mapping(source = "user.color", target = "color")
    @Mapping(source = "user.role", target = "role")
    @Mapping(target = "labels", expression = "java(toProjectMemberLabels(source))")
    @Mapping(target = "capacity", expression = "java(toProjectMemberCapacity(source))")
    ProjectMember toProjectMember(UserProject source);

    default Set<LabelDTO> toProjectMemberLabels(UserProject source) {
        var userLabels =
                source.getUser().getLabels().stream().map(Label::getName).collect(toSet());

        return source.getProject().getLabels().stream()
                .filter(label -> userLabels.contains(label.getName()))
                .map(LabelDTO::of)
                .collect(toSet());
    }

    default List<AbsenceDTO> toProjectMemberAbsences(UserProject source, List<Absence> absences) {
        return absences.stream()
                .filter(a -> a.getUser().getId() == source.getUser().getId())
                .map(AbsenceDTO::of)
                .toList();
    }

    default int toProjectMemberCapacity(UserProject source) {
        return source.getCapacity();
    }

    @Mapping(source = "source.user.firstName", target = "firstName")
    @Mapping(source = "source.user.lastName", target = "lastName")
    @Mapping(source = "source.user.employeeNumber", target = "employeeNumber")
    @Mapping(source = "source.user.color", target = "color")
    @Mapping(target = "labels", expression = "java(toProjectMemberLabels(source))")
    @Mapping(target = "absences", expression = "java(toProjectMemberAbsences(source, absences))")
    ProjectMemberWithAbsences toProjectMemberWithAbsences(UserProject source, List<Absence> absences);

    @Mapping(target = "employeeNumber", source = "employeeNumber")
    @Mapping(target = "capacities", expression = "java(toCapacitiesForUserInProjects(source))")
    ProjectMemberWithCapacities toProjectMemberWithCapacities(List<UserProject> source, String employeeNumber);

    default String toProjectMemberEmployeeNumber(List<UserProject> source) {
        return source.get(0).getUser().getEmployeeNumber();
    }
    default List<ProjectNameWithCapacity> toCapacitiesForUserInProjects(List<UserProject> source) {
        List<ProjectNameWithCapacity> result = new ArrayList<>();

        for (UserProject userProject : source) {
            result.add(new ProjectNameWithCapacity(
                    userProject.getProject().getId(),
                    userProject.getProject().getName(),
                    userProject.getProject().isCapacityMode(),
                    userProject.getCapacity()));
        }
        return result;
    }
}
