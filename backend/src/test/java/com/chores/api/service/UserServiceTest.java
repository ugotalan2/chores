package com.chores.api.service;

import com.chores.api.entity.User;
import com.chores.api.enums.Role;
import com.chores.api.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User kidUser;
    private User parentUser;

    @BeforeEach
    void setup() {
        kidUser = User.builder()
                .displayName("Kid1")
                .role(Role.CHILD)
                .isActive(true)
                .build();

        parentUser = User.builder()
                .displayName("Parent")
                .role(Role.PARENT)
                .email("parent@example.com")
                .clerkUserId("clerk_123")
                .isActive(true)
                .build();
    }

    @Test
    void getActiveKids_returnsOnlyActiveKids() {
        when(userRepository.findByRoleAndIsActiveTrue(Role.CHILD))
                .thenReturn(List.of(kidUser));

        List<User> result = userService.getActiveKids();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRole()).isEqualTo(Role.CHILD);
        verify(userRepository).findByRoleAndIsActiveTrue(Role.CHILD);
    }

    @Test
    void getUserById_returnsUserWhenFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id))
                .thenReturn(Optional.of(kidUser));

        Optional<User> result = userService.getUserById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getDisplayName()).isEqualTo("Kid1");
    }

    @Test
    void getUserById_returnsEmptyWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        Optional<User> result = userService.getUserById(id);

        assertThat(result).isEmpty();
    }

    @Test
    void updateUser_throwsWhenUserNotFound() {
        UUID id = UUID.randomUUID();
        User user = User.builder()
                .displayName("Ghost")
                .role(Role.CHILD)
                .build();

        // Simulate no ID set - existsById returns false
        when(userRepository.existsById(any())).thenReturn(false);

        assertThatThrownBy(() -> userService.updateUser(id, user))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateUser_throwsWhenIdsDontMatch() {
        UUID id = UUID.randomUUID();
        User user = User.builder()
                .displayName("Ghost")
                .role(Role.CHILD)
                .build();
        user.setId(UUID.randomUUID());

        when(userRepository.existsById(any())).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(id, user))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deactivateUser_setIsActiveFalse() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id))
                .thenReturn(Optional.of(kidUser));
        when(userRepository.save(any(User.class)))
                .thenReturn(kidUser);

        userService.deactivateUser(id);

        assertThat(kidUser.getIsActive()).isFalse();
        verify(userRepository).save(kidUser);
    }

    @Test
    void deactivateUser_throwsWhenUserNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deactivateUser(id))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
