package com.example.WeConnect_BE.service;

import com.example.WeConnect_BE.dto.response.ContactResponse;
import com.example.WeConnect_BE.entity.Contact;
import com.example.WeConnect_BE.entity.User;
import com.example.WeConnect_BE.repository.ContactRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.logging.log4j.util.Strings;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContactService {
    ContactRepository contactRepository;

    public List<ContactResponse> getContacts() {
        JwtAuthenticationToken authentication =
                (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        Jwt jwt = authentication.getToken();
        String id = jwt.getSubject(); // sub trong JWT
        List<Contact> contacts = contactRepository
                .findAllByUserId(id);

        // Map sang ContactResponse
        return contacts.stream()
                .map(contact -> {
                    User otherUser;
                    if (contact.getRequesterUser().getUserId().equals(id)) {
                        otherUser = contact.getAddresseeUser();
                    } else {
                        otherUser = contact.getRequesterUser();
                    }

                    return ContactResponse.builder()
                            .id(otherUser.getUserId())                // id của đối phương
                            .name(otherUser.getUsername())        // tên đối phương
                            .email(otherUser.getEmail())          // email đối phương
                            .avatarUrl(otherUser.getAvatarUrl())  // avatar đối phương
                            .build();
                }).toList();
    }

}
