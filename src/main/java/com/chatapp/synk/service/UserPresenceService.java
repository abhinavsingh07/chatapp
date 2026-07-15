package com.chatapp.synk.service;

import com.chatapp.synk.dto.UserDTO;
import com.chatapp.synk.dto.UserStatusDTO;

import java.util.List;

/**
 * Manages user online presence — last-seen timestamps and active status.
 * Separated from {@link UserService} to keep profile management and
 * presence tracking as distinct concerns (SRP).
 */
public interface UserPresenceService {

    /**
     * Update the last-seen timestamp for a user from Redis into the database.
     *
     * @param userId the user ID
     * @return updated UserDTO with the new lastSeen value
     */
    UserDTO updateLastSeen(String userId);

    /**
     * Get the last active status for one or more comma-separated user IDs.
     * Checks Redis first; falls back to the database for users not in Redis.
     *
     * @param userId single user ID or comma-separated list
     * @return list of UserStatusDTO with online flag and last-active timestamp
     */
    List<UserStatusDTO> getLastActiveUserStatus(String userId);
}
