package com.example.WeConnect_BE.mapper;

import com.example.WeConnect_BE.dto.request.RegisterRequest;
import com.example.WeConnect_BE.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;




@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "password",target = "passwordHash")
    User toUser(RegisterRequest registerRequest);
}
