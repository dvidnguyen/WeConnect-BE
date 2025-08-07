package com.example.WeConnect_BE.mapper;

import com.example.WeConnect_BE.entity.Contact;
import com.example.WeConnect_BE.entity.Friend;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FriendMapper {
    @Mapping(source = "requester", target = "requesterUser")
    @Mapping(source = "addressee", target = "addresseeUser")
    @Mapping(source = "createdAt", target = "createdAt")
    Contact toContact(Friend friend);
}
