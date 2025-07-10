package com.chatapp.synk.util;

import com.chatapp.synk.dto.UserDTO;
import com.chatapp.synk.entity.User;

public class Mapper {

    // Utility mapper method
    public static UserDTO mapToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setName(user.getName());
        dto.setProfilePictureUrl(user.getProfilePictureUrl());
        dto.setAbout(user.getAbout());
        return dto;
    }
}
