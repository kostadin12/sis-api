package com.kostadin.sis.userproject.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "PROJECT_USER_ID")
public class UserProjectId implements Serializable {
    @Column(name = "USER_ID")
    private long userId;

    @Column(name = "PROJECT_ID")
    private long projectId;

    @Override
    public int hashCode() {
        return Objects.hash(userId, projectId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass())
            return false;

        UserProjectId that = (UserProjectId) o;
        return userId == that.userId &&
                projectId == that.projectId;
    }
}
