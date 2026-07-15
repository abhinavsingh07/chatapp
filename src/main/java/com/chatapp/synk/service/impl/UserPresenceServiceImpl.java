package com.chatapp.synk.service.impl;

import com.chatapp.synk.chat.redis.RedisSessionStore;
import com.chatapp.synk.dto.UserDTO;
import com.chatapp.synk.dto.UserStatusDTO;
import com.chatapp.synk.entity.User;
import com.chatapp.synk.exceptionHandler.ServiceException;
import com.chatapp.synk.repository.UserRepository;
import com.chatapp.synk.security_validator.InputSecurityUtils;
import com.chatapp.synk.service.UserPresenceService;
import com.chatapp.synk.util.Mapper;
import com.chatapp.synk.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserPresenceServiceImpl implements UserPresenceService {

    private static final Logger logger = LoggerFactory.getLogger(UserPresenceServiceImpl.class);

    private final UserRepository userRepository;
    private final RedisSessionStore redisSessionStore;

    public UserPresenceServiceImpl(UserRepository userRepository,
            RedisSessionStore redisSessionStore) {
        this.userRepository = userRepository;
        this.redisSessionStore = redisSessionStore;
    }

    @Override
    public UserDTO updateLastSeen(String userId) {
        logger.debug("Updating last seen for user ID: {}", userId);
        String validId = InputSecurityUtils.secureId(userId);

        User user = userRepository
                .findById(Long.parseLong(validId))
                .orElseThrow(() -> new ServiceException("User not found with ID", HttpStatus.NOT_FOUND));

        String lastActive = redisSessionStore.getLastActiveTimeStampUser(validId);
        if (StringUtil.isBlank(lastActive)) {
            throw new ServiceException("Last active timestamp not found", HttpStatus.NOT_FOUND);
        }

        user.setUserlastSeen(lastActive);
        User updatedUser = userRepository.save(user);
        return Mapper.mapToUserDTO(updatedUser);
    }

    @Override
    public List<UserStatusDTO> getLastActiveUserStatus(String userId) {
        logger.debug("Fetching last active timestamp for user(s): {}", userId);
        long now = Instant.now().toEpochMilli();
        List<UserStatusDTO> result = new ArrayList<>();

        String[] userIds = Arrays.stream(userId.split(","))
                .map(InputSecurityUtils::secureId)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        List<Long> missedIds = new ArrayList<>();

        for (String uid : userIds) {
            String lastActive = redisSessionStore.getLastActiveTimeStampUser(uid);
            if (lastActive != null) {
                boolean online = (now - Long.parseLong(lastActive)) <= 4000;
                result.add(new UserStatusDTO(uid, online, lastActive));
            } else {
                missedIds.add(Long.parseLong(uid));
            }
        }

        if (!missedIds.isEmpty()) {
            Map<Long, String> lastSeenByUserId = userRepository.findAllById(missedIds).stream()
                    .collect(Collectors.toMap(User::getId, u -> u.getUserlastSeen() != null ? u.getUserlastSeen() : ""));
            for (Long uid : missedIds) {
                String lastActiveDB = lastSeenByUserId.getOrDefault(uid, "");
                result.add(new UserStatusDTO(String.valueOf(uid), false, lastActiveDB));
            }
        }

        return result;
    }
}
