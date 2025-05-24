package com.kostadin.sis.userproject;

import com.kostadin.sis.userproject.model.UserProject;
import com.kostadin.sis.userproject.model.UserProjectId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProjectRepository extends JpaRepository<UserProject, UserProjectId> {
    @Query("""
            FROM UserProject up
            WHERE up.user.id = :userId
            AND up.project.id = :projectId
            """)
    @EntityGraph(attributePaths = {"user", "project"})
    Optional<UserProject> findByUserAndProjectId(long userId, long projectId);

    @EntityGraph(attributePaths = {"project", "user", "user.labels", "project.labels"})
    List<UserProject> findAllById_ProjectId(long projectId);

    @Query("""
            FROM UserProject up
            WHERE up.user.employeeNumber = :employeeId
            AND up.project.id = :projectId
            """)
    @EntityGraph(attributePaths = {"user", "project"})
    Optional<UserProject> findByUserEmployeeNumberAndProjectId(String employeeId, long projectId);

    @Query("""
            FROM UserProject up
            JOIN up.user u
            WHERE LOWER(u.employeeNumber) = LOWER(:employeeNumber)
            """)
    @EntityGraph(attributePaths = {"user", "project"})
    List<UserProject> findAllByUserEmployeeNumber(String employeeNumber);

    @Query("""
            SELECT DISTINCT up.user.color
            FROM UserProject up
            WHERE up.id.projectId IN (
                SELECT DISTINCT up_1.id.projectId
                FROM UserProject up_1
                WHERE LOWER(up_1.user.employeeNumber) = LOWER(:employeeNumber)
            )
            """)
    List<String> findAllColorsWithinProjectsByEmployeeNumber(String employeeNumber);
}
