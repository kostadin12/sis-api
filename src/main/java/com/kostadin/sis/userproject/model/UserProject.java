package com.kostadin.sis.userproject.model;

import com.kostadin.sis.project.model.Project;
import com.kostadin.sis.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Objects;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "PROJECT_USER")
public class UserProject {
    @EmbeddedId
    private UserProjectId id;

    @ManyToOne(fetch = LAZY)
    @MapsId("userId")
    private User user;

    @ManyToOne(fetch = LAZY)
    @MapsId("projectId")
    private Project project;

    @Column(name = "capacity")
    private int capacity;

    public UserProject(User user, Project project){
        this.user = user;
        this.project = project;
        this.capacity = 100;
        this.id = new UserProjectId(user.getId(), project.getId());
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        return (obj instanceof UserProject other) && id == other.id;
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }

    @Transient
    public boolean isCapacityModeEnabled() {
        return Objects.requireNonNull(project).isCapacityMode();
    }
}
