package com.kostadin.sis.project.model;

import com.kostadin.sis.userproject.model.UserProject;
import com.kostadin.sis.common.exception.ProjectBadRequestException;
import com.kostadin.sis.label.model.Label;
import com.kostadin.sis.label.model.LabelScope;
import com.kostadin.sis.user.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Slf4j
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@Entity
@Table(name = "PROJECTS")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "Project name is mandatory.")
    private String name;
    private String description;
    @NotNull(message = "Project start date is mandatory.")
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    @NotNull(message = "Project status is mandatory.")
    private ProjectStatus projectStatus;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PROJECT_OWNER_ID")
    private User projectOwner;

    @Column(name = "CAPACITY_MODE")
    private boolean capacityMode;

    @OneToMany(
            mappedBy = "project",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<UserProject> users = new HashSet<>();

    @ManyToMany(
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.LAZY
    )
    @JoinTable(name = "PROJECT_LABEL",
            joinColumns = @JoinColumn(name = "PROJECT_ID"),
            inverseJoinColumns = @JoinColumn(name = "LABEL_ID")
    )
    private Set<Label> labels = new HashSet<>();

    public Project removeUser(User user){
        var isUserNotRemoved = !getUsers().removeIf(userProject -> Objects.equals(userProject.getUser().getEmployeeNumber(), user.getEmployeeNumber()));

        if (isUserNotRemoved) {
            throw new ProjectBadRequestException("User not found in project.");
        }

        return this;
    }

    public Project addUsers(List<User> users) {
        var projectUsers = this.getUsers().stream().map(UserProject::getUser).collect(Collectors.toSet());
        for (User user : users) {
            if (projectUsers.contains(user)) {
                throw new ProjectBadRequestException("User " + user.getEmployeeNumber() + " already included on project.");
            }
            this.getUsers().add(new UserProject(user, this));
        }
        return this;
    }

    public Project addLabel(Label label) {
        var hasLabelProjectScopeAndNotAddedToOtherProject = label.getScope() == LabelScope.PROJECT && isNotEmpty(label.getProjects());

        if (hasLabelProjectScopeAndNotAddedToOtherProject){
            throw new ProjectBadRequestException("Label is assigned to another project.");
        }

        var isNotLabelAdded = !this.getLabels().add(label);
        if(isNotLabelAdded){
            throw new ProjectBadRequestException("Project already has label " + label.getName() + ".");
        }

        log.info("Added label {} to project", label);
        return this;
    }

    public Project addProjectLabels(List<Label> labels){
        labels.forEach(this::addLabel);
        return this;
    }

    public Project removeLabel(Label label) {

        if (getLabels().remove(label)) {
            log.info("Removed label {} from project", label);
        }
        return this;
    }

    public Project removeLabels(List<Label> labels) {
        var labelNames = labels.stream().map(Label::getName).toList();
        getLabels().removeIf(label -> labelNames.contains(label.getName()));
        return this;
    }
}