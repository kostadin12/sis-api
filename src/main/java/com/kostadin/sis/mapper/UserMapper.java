package com.kostadin.sis.mapper;

import com.kostadin.sis.subscriptions.model.Subscription;
import com.kostadin.sis.userproject.model.UserProject;
import com.kostadin.sis.excel.request.UserExcelDTO;
import com.kostadin.sis.label.model.Label;
import com.kostadin.sis.user.model.User;
import com.kostadin.sis.user.model.request.SaveUserCommand;
import com.kostadin.sis.user.model.response.UserAccount;
import com.kostadin.sis.user.model.response.UserDTO;
import com.kostadin.sis.user.model.response.UserNameEmployeeNumber;
import com.kostadin.sis.user.model.response.UserWithSystemLabelsAndCompany;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

import static com.kostadin.sis.label.model.LabelScope.SYSTEM;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Mapper
public interface UserMapper {
    @Mapping(target = "subscribedToEmployeeNumbers", expression = "java(mapSubscriptions(userSubscriptions))")
    UserAccount toAccount(User user, List<Subscription> userSubscriptions);

    default List<String> mapSubscriptions(List<Subscription> userSubscriptions) {
        return isNotEmpty(userSubscriptions) ? userSubscriptions.stream().map(Subscription::getUser).map(User::getEmployeeNumber).toList() : List.of();
    }

    UserDTO toDto(User user);

    default UserDTO map(UserProject userProject){
        return toDto(userProject.getUser());
    }

    List<UserDTO> toDto(List<User> users);

    @Mapping(target = "labels", ignore = true)
    User toEntity(SaveUserCommand saveUserCommand);

    UserNameEmployeeNumber toResponseBody(User user);

    List<UserNameEmployeeNumber> toResponseBody(List<User> users);

    @Mapping(target = "daysAvailable", ignore = true)
    @Mapping(target = "daysOff", ignore = true)
    @Mapping(target = "projects", ignore = true)
    UserExcelDTO mapToExcelBody(User user);

    List<UserExcelDTO> mapToExcelBody(Set<User> users);

    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "employeeNumber", target = "employeeNumber")
    @Mapping(source = "company", target = "company")
    @Mapping(target = "systemLabels", expression = "java(toSystemLabelNames(user))")
    UserWithSystemLabelsAndCompany toUserWithSystemLabelsAndCompany(User user);

    Set<UserWithSystemLabelsAndCompany> toUserWithSystemLabelsAndCompany(Set<User> users);

    default List<String> toSystemLabelNames(User user) {
        return user.getLabels().stream().filter(label -> label.getScope() == SYSTEM).map(Label::getName).toList();
    }
}
