package com.kostadin.sis.user;

import com.kostadin.sis.user.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = {"labels"})
    Optional<User> findByEmployeeNumberIgnoreCase(String employeeNumber);

    @EntityGraph(attributePaths = {"labels"})
    Optional<User> findByEmailIgnoreCase(String email);

    @Query("""
            FROM User u
            WHERE UPPER(u.employeeNumber) = UPPER(:employeeNumber)
            """)
    @EntityGraph(attributePaths = {"labels", "projects"})
    Optional<User> findByEmployeeNumberIgnoreCaseWithProjects(@Param("employeeNumber") String employeeNumber);

    @Query("""
            FROM User u
            WHERE UPPER(u.employeeNumber) IN :employeeIDs
            """)
    @EntityGraph(attributePaths = {"labels"})
    List<User> findUsersIn(List<String> employeeIDs);

    @Query("""
            SELECT u FROM User u
            WHERE LOWER(u.employeeNumber) LIKE '%' || LOWER(:input) || '%' ESCAPE '/'
            OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE '%' || LOWER(:input) || '%' ESCAPE '/'
            OR LOWER(CONCAT(u.lastName, ' ', u.firstName)) LIKE '%' || LOWER(:input) || '%' ESCAPE '/'
    """)
    List<User> findUsersWithNameOrEmployeeNumberLike(String input);

    @Query("""
            SELECT substitute
            FROM Project p
            JOIN p.users upEmp
            JOIN p.users upSub
            JOIN upEmp.user AS employee
            JOIN upSub.user AS substitute
            WHERE employee.employeeNumber = :absentUserEmployeeNumber
            AND
            (LOWER(substitute.employeeNumber) LIKE '%' || LOWER(:input) || '%' ESCAPE '/'
            OR LOWER(CONCAT(substitute.firstName, ' ', substitute.lastName)) LIKE '%' || LOWER(:input) || '%' ESCAPE '/'
            OR LOWER(CONCAT(substitute.lastName, ' ', substitute.firstName)) LIKE '%' || LOWER(:input) || '%' ESCAPE '/')
            AND substitute.employeeNumber != employee.employeeNumber
    """)
    List<User> findUsersInProjectWithNameOrEmployeeNumberLike(String absentUserEmployeeNumber, String input);

    @Query("""
            SELECT substitute
            FROM Project project
            JOIN project.users up_e
            JOIN project.users up_s
            JOIN up_e.user AS employee
            JOIN up_s.user AS substitute
            WHERE employee.employeeNumber = :employeeNumber
            AND
            substitute.employeeNumber = :substituteEmployeeNumber
            """)
    Optional<User> findSubstituteIfAvailable(@Param("employeeNumber") String employeeNumber, @Param("substituteEmployeeNumber") String substituteEmployeeNumber);

    @Procedure("utl.WS_EXCHANGE.book_in_cal_vend")
    void sendAppointment(String subject, String body, String startDate, String endDate, String sendTo, Integer duration);

    @Query("""
            SELECT u
            FROM Project p
            JOIN p.users up
            JOIN up.user u
            WHERE UPPER(u.employeeNumber) = :beneficiaryManagerEmployeeNumber
            AND (u.role = ROLE_MANAGER OR u.role = ROLE_REPORT OR  u.role = ROLE_ADMIN)
            AND p.id = :projectId
            """)
    Optional<User> findBeneficiaryByEmployeeNumberIgnoreCase(String beneficiaryManagerEmployeeNumber, long projectId);

    @Query("""
            FROM User u
            WHERE UPPER (u.employeeNumber) = UPPER (:employeeId)
            """)
    @EntityGraph(attributePaths = {"ownedProjects", "projects", "projects.project", "labels"})
    Optional<User> findByEmployeeNumberIgnoreCaseWithOwnedProjects(String employeeId);

    @Query("""
            SELECT u
            FROM Project p
            JOIN p.users up
            JOIN up.user u
            WHERE p.id = :projectId
            AND (u.role = ROLE_MANAGER OR u.role = ROLE_ADMIN OR u.role = ROLE_REPORT)
            AND u.employeeNumber != p.projectOwner.employeeNumber
            AND
            (LOWER(u.employeeNumber) LIKE '%' || LOWER(:filter) || '%' ESCAPE '/'
            OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE '%' || LOWER(:filter) || '%' ESCAPE '/'
            OR LOWER(CONCAT(u.lastName, ' ', u.firstName)) LIKE '%' || LOWER(:filter) || '%' ESCAPE '/')
            """)
    List<User> findBeneficiaryCandidatesInProject(String filter, long projectId);

    @Query("""
            SELECT EXISTS (
            SELECT 1 FROM User u
            WHERE u.employeeNumber = :employeeNumber
            )
            """)
    boolean existsUserWithEmployeeNumber(String employeeNumber);

    @Query("""
            SELECT u.employeeNumber
            FROM User u
            WHERE LOWER(u.employeeNumber) LIKE '%' || LOWER(:personKey) || '%' ESCAPE '/'
            """)
    String getExistentEmployeeNumberLike(String personKey);

    @Query("""
            FROM User u
            WHERE u.company = :company
            """)
    @EntityGraph(attributePaths = {"labels"})
    Set<User> findUsersWithCompany(String company);

    @Query("""
           FROM User u
           JOIN u.labels l
           WHERE l.name = :systemLabel
           AND l.scope = SYSTEM
           """)
    @EntityGraph(attributePaths = {"labels"})
    Set<User> findUsersWithSystemLabel(String systemLabel);

    @Query("""
            SELECT u.company
            FROM User u
            """)
    Set<String> getAllCompanies();

    @Query("""
            FROM User u
            WHERE UPPER(u.employeeNumber) IN :employeeNumbers
            """)
    List<User> findByEmployeeNumberIgnoreCaseIn(List<String> employeeNumbers);

    @Query("""
        SELECT EXISTS (
        SELECT 1 FROM User u
        WHERE LOWER(u.email) = LOWER(:email)
        )
        """)
    boolean existsByEmailIgnoreCase(String email);

    @Query("""
        SELECT EXISTS (
        SELECT 1 FROM User u
        WHERE u.employeeNumber = :employeeNumber
        )
        """)
    boolean existsByEmployeeNumber(String employeeNumber);
}
