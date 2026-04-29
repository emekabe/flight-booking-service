package com.emekachukwulobe.flightbookingservice.mapper;

import com.emekachukwulobe.flightbookingservice.domain.User;
import com.emekachukwulobe.flightbookingservice.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "tenant.code", target = "tenantCode")
    UserResponse toResponse(User user);
}
