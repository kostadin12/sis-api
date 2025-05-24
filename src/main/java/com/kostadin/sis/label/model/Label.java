package com.kostadin.sis.label.model;

import com.kostadin.sis.project.model.Project;
import com.kostadin.sis.user.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Set;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@Entity
@Table(name = "LABELS")
public class Label {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "Label name is mandatory.")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "SCOPE")
    @NotNull(message = "Label scope is mandatory.")
    private LabelScope scope;

    @ManyToMany(mappedBy = "labels", fetch = FetchType.LAZY)
    private Set<User> users;

    @ManyToMany(mappedBy = "labels", fetch = FetchType.LAZY)
    private Set<Project> projects;

    @PreRemove
    protected void removeLabelFromUsersAndProjects(){
        removeLabelFromUsers(this);
        removeLabelFromProjects(this);
    }

    public void removeLabelFromUsers(Label label){
        if (isNotEmpty(this.getUsers())){
            this.getUsers().forEach(u -> u.getLabels().remove(label));
        }
    }

    public void removeLabelFromProjects(Label label){
        if (isNotEmpty(this.getProjects())) {
            this.getProjects().forEach(p -> p.getLabels().remove(label));
        }
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        return (obj instanceof Label other) && id == other.id;
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}