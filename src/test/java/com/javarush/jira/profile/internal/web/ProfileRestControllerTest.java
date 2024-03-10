package com.javarush.jira.profile.internal.web;

import com.javarush.jira.profile.ProfileTo;
import com.javarush.jira.common.util.JsonUtil;
import com.javarush.jira.AbstractControllerTest;
import com.javarush.jira.profile.internal.model.Profile;
import com.javarush.jira.profile.internal.ProfileMapper;
import com.javarush.jira.profile.internal.ProfileRepository;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static com.javarush.jira.login.internal.web.UserTestData.*;
import static com.javarush.jira.profile.internal.web.ProfileTestData.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

class ProfileRestControllerTest extends AbstractControllerTest {

    @Autowired
    private ProfileRepository repository;
    @Autowired
    ProfileMapper mapper;

    /**
     * Проверяет, что неавторизованный доступ к профилю возвращает статус "Unauthorized".
     */
    @Test
    void verifyUnauthorizedAccessToGetProfile() throws Exception {
        perform(MockMvcRequestBuilders.get(ProfileRestController.REST_URL))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Проверяет, что авторизованный пользователь получает свой профиль.
     */
    @Test
    @WithUserDetails(value = USER_MAIL)
    public void verifyUserProfileReturnedForAuthorizedUser() throws Exception {
        perform(MockMvcRequestBuilders.get(ProfileRestController.REST_URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(PROFILE_TO_MATCHER.contentJson(USER_PROFILE_TO));
    }

    /**
     * Проверяет, что авторизованный администратор получает профиль администратора.
     */
    @Test
    @WithUserDetails(value = ADMIN_MAIL)
    public void verifyAdminProfileReturnedForAuthorizedAdmin() throws Exception {
        perform(MockMvcRequestBuilders.get(ProfileRestController.REST_URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(PROFILE_TO_MATCHER.contentJson(ADMIN_PROFILE_TO));
    }

    /**
     * Проверяет, что гость получает пустой профиль.
     */
    @Test
    @WithUserDetails(value = GUEST_MAIL)
    public void verifyEmptyProfileReturnedForGuest() throws Exception {
        perform(MockMvcRequestBuilders.get(ProfileRestController.REST_URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(PROFILE_TO_MATCHER.contentJson(GUEST_PROFILE_EMPTY_TO));
    }

    /**
     * Проверяет, что неавторизованный доступ к обновлению профиля возвращает статус "Unauthorized".
     */
    @Test
    void verifyUnauthorizedAccessToUpdateProfile() throws Exception {
        perform(MockMvcRequestBuilders.put(ProfileRestController.REST_URL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Проверяет, что изменения сохраняются в базе данных при обновлении действительного существующего профиля.
     */
    @Test
    @WithUserDetails(value = USER_MAIL)
    public void verifyChangesSavedForValidExistingProfileUpdate() throws Exception {
        ProfileTo updateProfileTo = getUpdatedTo(USER_ID);

        perform(MockMvcRequestBuilders.put(ProfileRestController.REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(updateProfileTo)))
                .andDo(print())
                .andExpect(status().isNoContent());

        Profile afterUpdateProfile = repository.getExisted(USER_ID);
        ProfileTo afterUpdateProfileTo = mapper.toTo(afterUpdateProfile);

        assertAll(
                () -> assertEquals(afterUpdateProfileTo.id(), updateProfileTo.id()),
                () -> assertThat(afterUpdateProfileTo.getContacts()).hasSameElementsAs(updateProfileTo.getContacts()),
                () -> assertThat(afterUpdateProfileTo.getMailNotifications()).hasSameElementsAs((updateProfileTo.getMailNotifications()))
        );
    }

    /**
     * Проверяет, что при попытке обновления профиля с неправильным ID возвращается статус "Unprocessable Entity".
     */
    @Test
    @WithUserDetails(value = USER_MAIL)
    public void verifyUnprocessableEntityReturnedForInconsistentIdUpdate() throws Exception {
        ProfileTo updateProfileTo = getUpdatedTo(ADMIN_ID);
        perform(MockMvcRequestBuilders.put(ProfileRestController.REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(updateProfileTo)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    /**
     * Проверяет, что изменения сохраняются для авторизованного пользователя при обновлении профиля с нулевым ID.
     */
    @Test
    @WithUserDetails(value = USER_MAIL)
    public void verifyChangesSavedForNullIdUpdateForAuthorizedUserProfile() throws Exception {
        ProfileTo updateProfileTo = getUpdatedTo(null);
        perform(MockMvcRequestBuilders.put(ProfileRestController.REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(updateProfileTo)))
                .andDo(print())
                .andExpect(status().isNoContent());

        Profile afterUpdateProfile = repository.getExisted(USER_ID);
        ProfileTo afterUpdateProfileTo = mapper.toTo(afterUpdateProfile);
        updateProfileTo.setId(USER_ID);

        assertAll(
                () -> assertEquals(afterUpdateProfileTo.id(), updateProfileTo.id()),
                () -> assertThat(afterUpdateProfileTo.getContacts()).hasSameElementsAs(updateProfileTo.getContacts()),
                () -> assertThat(afterUpdateProfileTo.getMailNotifications()).hasSameElementsAs((updateProfileTo.getMailNotifications()))
        );
    }

    /**
     * Проверяет, что при попытке обновления профиля с неизвестным контактом возвращается статус "Unprocessable Entity".
     */
    @Test
    @WithUserDetails(value = USER_MAIL)
    public void verifyUnprocessableEntityReturnedForUnknownContactUpdate() throws Exception {
        ProfileTo updatedProfileTo = getWithUnknownContactTo(USER_ID);

        perform(MockMvcRequestBuilders.put(ProfileRestController.REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(updatedProfileTo)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    /**
     * Проверяет, что при попытке обновления недопустимого профиля возвращается статус "Unprocessable Entity".
     */
    @Test
    @WithUserDetails(value = USER_MAIL)
    public void verifyUnprocessableEntityReturnedForInvalidProfileUpdate() throws Exception {
        ProfileTo updatedProfileTo = getInvalidTo(USER_ID);

        perform(MockMvcRequestBuilders.put(ProfileRestController.REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(updatedProfileTo)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    /**
     * Проверяет, что при попытке обновления профиля с неизвестными уведомлениями возвращается статус "Unprocessable Entity".
     */
    @Test
    @WithUserDetails(value = USER_MAIL)
    public void verifyUnprocessableEntityReturnedForUnknownNotificationsUpdate() throws Exception {
        ProfileTo updatedProfileTo = getWithUnknownNotificationTo(USER_ID);

        perform(MockMvcRequestBuilders.put(ProfileRestController.REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(updatedProfileTo)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    /**
     * Проверяет, что при попытке обновления профиля с контактом, содержащим небезопасный HTML, возвращается статус "Unprocessable Entity".
     */
    @Test
    @WithUserDetails(value = USER_MAIL)
    public void verifyUnprocessableEntityReturnedForHtmlUnsafeContactUpdate() throws Exception {
        ProfileTo updatedProfileTo = getWithContactHtmlUnsafeTo(USER_ID);

        perform(MockMvcRequestBuilders.put(ProfileRestController.REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(updatedProfileTo)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }
}