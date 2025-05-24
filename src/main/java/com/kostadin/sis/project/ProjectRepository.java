package com.kostadin.sis.project;

import com.kostadin.sis.project.model.Project;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project,Long> {
    @Query("""
            SELECT p FROM Project p
            JOIN p.users up
            JOIN up.user u
            WHERE UPPER(u.employeeNumber) = UPPER(:employeeId)
            """)
    List<Project> findProjectsByUserBB(String employeeId);

    @Query("""
            SELECT p FROM Project p
            WHERE UPPER(p.name) LIKE '%' || UPPER(:input) || '%' ESCAPE '/'
            """)
    List<Project> findAllByNameLike(String input);

    @Query("""
            FROM Project p
            WHERE p.id = :projectId
            """)
    @EntityGraph(attributePaths = {"users", "labels"})
    Optional<Project> findByIdWithUsersAndLabels(@Param("projectId") long projectId);

    @Query("""
            FROM Project p
            WHERE p.id = :projectId
            """)
    @EntityGraph(attributePaths = {"labels"})
    Optional<Project> findByIdWithLabels(@Param("projectId") long projectId);

    @Query("""
            FROM Project p
            WHERE p.id = :projectId
            AND p.projectOwner.employeeNumber = :projectOwnerEmployeeNumber
            """)
    @EntityGraph(attributePaths = {"labels"})
    Optional<Project> findByIdAndProjectOwnerEmployeeNumberWithLabels(@Param("projectId") long projectId, @Param("projectOwnerEmployeeNumber") String projectOwnerEmployeeNumber);

    @Query("""
            FROM Project p
            WHERE p.id = :projectId
            """)
    @EntityGraph(attributePaths = {"users"})
    Optional<Project> findByIdWithUsers(long projectId);
}
