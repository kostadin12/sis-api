package com.kostadin.sis.absence;

import com.kostadin.sis.absence.model.Absence;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AbsenceRepository extends JpaRepository<Absence,Long> {
    List<Absence> findAllByUserId(long userId);

    @Query("""
            SELECT EXISTS (
            SELECT 1 FROM Absence a
            WHERE a.user.id = :userId
            AND (:startDate BETWEEN a.startDate AND a.endDate
            OR :endDate BETWEEN a.startDate AND a.endDate)
            )
            """)
    boolean checkInvalidAbsenceDates(long userId, LocalDate startDate, LocalDate endDate);

    @Query("""
            SELECT EXISTS (
            SELECT 1 FROM Absence a
            WHERE a.user.id = :userId
            AND (:startDate BETWEEN a.startDate AND a.endDate
            OR :endDate BETWEEN a.startDate AND a.endDate)
            AND a.id != :ignored
            )
            """)
    boolean existsAnotherUserAbsenceWithinTimeRangeIgnoringId(@Param("userId") long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("ignored") Long ignoredAbscenseId);

    @Query("""
            FROM Absence a
            WHERE UPPER(a.user.employeeNumber) = UPPER(:employeeNumber)
            """)
    @EntityGraph(attributePaths = "user")
    List<Absence> findAllByUserEmployeeNumber(@Param("employeeNumber") String employeeNumber);

    @Query("""
            FROM Absence a
            WHERE ( a.startDate BETWEEN :startDate AND :endDate
            OR a.endDate BETWEEN :startDate AND :endDate )
            """)
    List<Absence> findAbsencesBetween(@Param("startDate")LocalDate startDate, @Param("endDate")LocalDate endDate);

    @Query("""
            FROM Absence a
            WHERE a.id = :id
            AND a.user.employeeNumber = :employeeNumber
            """)
    Optional<Absence> findByIdAndEmployeeNumber(long id, String employeeNumber);

    @Query("""
            SELECT EXISTS (
            SELECT 1 FROM Absence a
            WHERE LOWER(a.user.email) = LOWER(:email)
            AND
            ((:startDate BETWEEN a.startDate AND a.endDate)
            OR (:endDate BETWEEN a.startDate AND a.endDate))
            )
            """)
    boolean isAbsenceExistent(String email, LocalDate startDate, LocalDate endDate);

    @Query("""
            FROM Absence a
            WHERE a.user.employeeNumber = :employeeNumber
            AND ( a.startDate BETWEEN :startDate AND :endDate
            OR a.endDate BETWEEN :startDate AND :endDate )
            """)
    List<Absence> findAllByEmployeeNumberWithinTimeRage(String employeeNumber, LocalDate startDate, LocalDate endDate);
}
