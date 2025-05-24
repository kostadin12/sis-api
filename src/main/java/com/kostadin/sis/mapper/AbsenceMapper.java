package com.kostadin.sis.mapper;

import com.kostadin.sis.absence.model.Absence;
import com.kostadin.sis.absence.model.request.SaveAbsenceCommand;
import com.kostadin.sis.absence.model.response.AbsenceDTO;
import com.kostadin.sis.user.model.response.UserDTO;
import com.kostadin.sis.user.model.response.UserNameEmployeeNumber;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(uses = UserMapper.class)
public interface AbsenceMapper {

    @Mapping(target = "substitute", source = "substitute", qualifiedByName = "mapSubstituteToUserNameEmployeeNumber")
    AbsenceDTO toDto(Absence absence);

    List<AbsenceDTO> toDto(List<Absence> absenceList);

    default Absence toEntity(SaveAbsenceCommand saveAbsenceCommand){
        if ( saveAbsenceCommand == null ) {
            return null;
        }

        Absence absence = new Absence();

        absence.setStartDate( saveAbsenceCommand.getStartDate() );
        absence.setEndDate( saveAbsenceCommand.getEndDate() );
        absence.setAbsenceType( saveAbsenceCommand.getAbsenceType() );

        return absence;
    }

    @Named("mapSubstituteToUserNameEmployeeNumber")
    default UserNameEmployeeNumber mapSubstituteToEmployeeNumber(UserDTO userDTO){
        if (userDTO != null && userDTO.getEmployeeNumber() != null){
            return new UserNameEmployeeNumber(userDTO.getFirstName(), userDTO.getLastName(), userDTO.getEmployeeNumber());
        }
        return null;
    }

}
