package com.kostadin.sis.label;

import com.kostadin.sis.label.model.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface LabelRepository extends JpaRepository<Label, Long> {
    Optional<Label> findLabelByNameIgnoreCase(String name);

    @Query("""
            FROM Label l
            WHERE l.id IN :labelIDs
            """)
    Set<Label> findLabelsIn(@Param("labelIDs") List<Long> labelIDs);

    @Query("""
            FROM Label l
            WHERE l.id IN :labelIDs
            AND l.scope = SYSTEM
            """)
    Set<Label> findSystemLabelsIn(@Param("labelIDs") List<Long> labelIDs);

    @Query("""
            FROM Label l
            WHERE UPPER(l.scope) = UPPER(:scope)
            """)
    List<Label> findAllWithScope(@Param("scope") String scope);

    @Query("""
            FROM Label l
            WHERE UPPER(l.scope) = UPPER(:labelScope)
            AND UPPER(l.name) LIKE '%' || UPPER(:filter) || '%' ESCAPE '/'
            """)
    List<Label> findAllByScopeAndName(@Param("labelScope") String labelScope, @Param("filter") String filter);

    @Query("""
            FROM Label l
            JOIN l.projects p
            WHERE p.id = :projectId
            AND l.name = :labelName
            AND l.scope = PROJECT
            """)
    Optional<Label> findProjectScopeLabelInProjectByName(@Param("labelName") String labelName, @Param("projectId") long projectId);

    @Query("""
            SELECT EXISTS (
            SELECT 1 FROM Label l
            WHERE l.name = :name
            )
            """)
    boolean existsLabelWithName(@Param("name") String name);

    @Query("""
            SELECT EXISTS (
            SELECT 1 FROM Label l
            WHERE l.name = :name
            AND l.scope = SYSTEM
            )
            """)
    boolean existsSystemLabelWithName(@Param("name") String name);

    @Query("""
            SELECT EXISTS (
            SELECT 1 FROM Project p
            JOIN p.labels l
            WHERE l.name = :name
            AND p.id = :projectId
            )
            """)
    boolean existsLabelWithNameInProject(@Param("name") String name, @Param("projectId") long projectId);

    @Query("""
            FROM Label l
            WHERE UPPER(l.name) = UPPER(:name)
            AND l.scope = SYSTEM
            """)
    Optional<Label> findSystemLabelByNameIgnoreCase(@Param("name") String name);

    @Query("""
            FROM Label l
            JOIN l.projects p
            WHERE p.id = :projectId
            AND l.name IN :labelNames
            """)
    List<Label> findProjectLabelsIn(List<String> labelNames, long projectId);

    @Query("""
        SELECT EXISTS (
        SELECT 1 FROM Label l
        JOIN l.projects p
        JOIN p.users up
        JOIN up.user u
        WHERE l.name = :labelName
        AND l.scope = SYSTEM
        AND p.id = :projectId
        AND EXISTS (
            SELECT 1
            FROM u.labels ul
            WHERE ul.id = l.id
            AND ul.scope = SYSTEM
            AND u.employeeNumber != :ignoredUserEmployeeNumber
            )
        )
        """)
    boolean isLabelFoundInAnyUsersInProject(long projectId, String labelName, String ignoredUserEmployeeNumber);

    @Query("""
            FROM Label l
            JOIN l.projects p
            WHERE p.id = :projectId
            AND l.name IN :labelNames
            AND l.scope = SYSTEM
            """)
    List<Label> findSystemLabelsInProject(List<String> labelNames, long projectId);
}
