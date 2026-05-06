package edu.zut.pbalab4;

import edu.zut.pbalab4.controller.UsersApiController;
import edu.zut.pbalab4.exception.UserAlreadyExistsException;
import edu.zut.pbalab4.exception.UserNotFoundException;
import edu.zut.pbalab4.model.CreateRequest;
import edu.zut.pbalab4.model.RequestHeader;
import edu.zut.pbalab4.model.User;
import edu.zut.pbalab4.model.UserResponse;
import edu.zut.pbalab4.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UsersIntegrationTest {

    @Autowired
    private UsersApiController usersApiController;

    @Autowired
    private UserService userService;

    private UUID userId1;
    private UUID userId2;
    private UUID nonExistingId;

    @BeforeEach
    void setUp() {
        userId1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        userId2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
        nonExistingId = UUID.fromString("33333333-3333-3333-3333-333333333333");

        try {
            userService.delete(userId1);
        } catch (Exception ignored) {
        }

        try {
            userService.delete(userId2);
        } catch (Exception ignored) {
        }

        try {
            userService.delete(nonExistingId);
        } catch (Exception ignored) {
        }
    }

    @Test
    void shouldAddUserToDatabase() {
        CreateRequest request = buildCreateRequest(
                userId1,
                "Anna",
                "Michalak",
                24,
                "92011165987",
                "PL",
                "a_mich@gmail.com"
        );

        ResponseEntity<UserResponse> response = usersApiController.createUser(request);

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getUser());

        User savedUser = userService.getOne(userId1);

        assertNotNull(savedUser);
        assertEquals("Anna", savedUser.getName());
        assertEquals("Michalak", savedUser.getSurname());
        assertEquals(24, savedUser.getAge());
        assertEquals("92011165987", savedUser.getPersonalId());
        assertEquals(User.CitizenshipEnum.PL, savedUser.getCitizenship());
        assertEquals("a_mich@gmail.com", savedUser.getEmail());
    }

    @Test
    void shouldRejectAddingUserWithExistingPersonalId() {
        CreateRequest firstRequest = buildCreateRequest(
                userId1,
                "Anna",
                "Michalak",
                24,
                "92011165987",
                "PL",
                "a_mich@gmail.com"
        );

        usersApiController.createUser(firstRequest);

        CreateRequest duplicateRequest = buildCreateRequest(
                userId2,
                "Hanna",
                "Nowak",
                30,
                "92011165987",
                "PL",
                "h_nowak@gmail.com"
        );

        assertThrows(UserAlreadyExistsException.class, () -> usersApiController.createUser(duplicateRequest));
    }

    @Test
    void shouldDeleteExistingUser() {
        CreateRequest request = buildCreateRequest(
                userId1,
                "Anna",
                "Michalak",
                24,
                "92011165987",
                "PL",
                "a_mich@gmail.com"
        );

        usersApiController.createUser(request);

        ResponseEntity<Void> response = usersApiController.deleteUser(userId1);

        assertEquals(204, response.getStatusCode().value());
        assertThrows(UserNotFoundException.class, () -> userService.getOne(userId1));
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingUser() {
        assertThrows(UserNotFoundException.class, () -> usersApiController.deleteUser(nonExistingId));
    }

    private CreateRequest buildCreateRequest(UUID id,
                                             String name,
                                             String surname,
                                             int age,
                                             String personalId,
                                             String citizenship,
                                             String email) {
        RequestHeader header = new RequestHeader();
        header.setRequestId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        header.setSendDate(OffsetDateTime.parse("2026-04-08T12:00:00Z"));

        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setSurname(surname);
        user.setAge(age);
        user.setPersonalId(personalId);
        user.setCitizenship(User.CitizenshipEnum.fromValue(citizenship));
        user.setEmail(email);

        CreateRequest request = new CreateRequest();
        request.setRequestHeader(header);
        request.setUser(user);

        return request;
    }
}