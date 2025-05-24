package com.kostadin.sis.user.model;

import com.kostadin.sis.absence.model.Absence;
import com.kostadin.sis.common.exception.UserBadRequestException;
import com.kostadin.sis.label.model.Label;
import com.kostadin.sis.label.model.LabelScope;
import com.kostadin.sis.project.model.Project;
import com.kostadin.sis.userproject.model.UserProject;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@Entity
@Table(name = "USERS")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "FIRST_NAME")
    @NotBlank(message = "First name is mandatory.")
    @Size(min = 2, max = 255,message = "User first name max length is 255.")
    private String firstName;

    @Column(name = "LAST_NAME")
    @NotBlank(message = "Last name is mandatory.")
    @Size(min = 2, max = 255,message = "User last name max length is 255.")
    private String lastName;

    @Column(name = "EMPLOYEE_NUMBER")
    @NotBlank(message = "Employee number is mandatory.")
    @Pattern(regexp = "^EMP\\d{5}$", message = "Invalid employee number.")
    private String employeeNumber;

    @Column(name = "PHONE")
    private String phone;

    @Column(name = "SECONDARY_PHONE")
    private String secondaryPhone;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "SECONDARY_EMAIL")
    private String secondaryEmail;

    @Column(name = "COMPANY")
    private String company;

    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE")
    @NotNull(message = "User role is mandatory.")
    private UserRole role;

    @Column(name = "COLOR")
    private String color;

    @Column(name = "PASSWORD")
    @NotBlank(message = "Password is mandatory.")
    private String password;

    @ManyToMany(
            cascade = { CascadeType.MERGE, CascadeType.PERSIST,CascadeType.REFRESH},
            fetch = FetchType.LAZY
    )
    @JoinTable(name = "USER_LABEL",
            joinColumns = @JoinColumn(name = "USER_ID"),
            inverseJoinColumns = @JoinColumn(name = "LABEL_ID")
    )
    @Fetch(FetchMode.SUBSELECT)
    private Set<Label> labels = new HashSet<>();

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<UserProject> projects = new HashSet<>();

    @OneToMany(mappedBy = "projectOwner")
    private Set<Project> ownedProjects = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Absence> absences;

    @OneToMany(mappedBy = "substitute", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Absence> substitutedAbsences;

    @PreRemove
    private void removeUserFromSubscribersAndAbsencesBeforeDeleting(){
        getAbsences().forEach(a -> a.setUser(null));
        getSubstitutedAbsences().forEach(sa -> sa.setSubstitute(null));
    }

    public User assignSystemLabel(Label label){
        if (label.getScope() == LabelScope.PROJECT){
            throw new UserBadRequestException("Cannot add project scope labels.");
        }

        var isLabelNotAdded = !this.getLabels().add(label);

        if (isLabelNotAdded){
            throw new UserBadRequestException("User already has label " + label.getName());
        }
        return this;
    }

    public User assignProjectLabel(Label label){
        if (label.getScope() == LabelScope.SYSTEM){
            throw new UserBadRequestException("Label " + label.getName() + " must have a project scope.");
        }

        var isLabelNotAdded = !this.getLabels().add(label);

        if (isLabelNotAdded){
            throw new UserBadRequestException("User already has label " + label.getName());
        }
        return this;
    }

    public User removeLabel(Label label){
        var isLabelNotRemoved = !this.getLabels().remove(label);

        if (isLabelNotRemoved){
            throw new UserBadRequestException("User doesn't have label " + label.getName());
        }
        return this;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        return (obj instanceof User other) && id == other.id;
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}