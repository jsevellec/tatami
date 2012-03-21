package fr.ippon.tatami.service;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.inject.Inject;

import org.junit.Test;

import fr.ippon.tatami.AbstractCassandraTatamiTest;
import fr.ippon.tatami.domain.User;
import fr.ippon.tatami.security.AuthenticationService;

public class UserServiceTest extends AbstractCassandraTatamiTest {

	@Inject
	public UserService userService;

	@Test
	public void shouldGetAUserServiceInjected() {
		assertThat(userService, notNullValue());
	}

	@Test
	public void shouldGetAUserByLogin() {
		User user = userService.getUserByLogin("jdubois");
		assertThat(user, notNullValue());
		assertThat(user.getEmail(), is("jdubois@ippon.fr"));
		assertThat(user.getGravatar(), is("gravatar"));
		assertThat(user.getFirstName(), is("Julien"));
		assertThat(user.getLastName(), is("Dubois"));
	}

	@Test
	public void shouldNotGetAUserByLogin() {
		User user = userService.getUserByLogin("unknownUserLogin");
		assertThat(user, nullValue());
	}

	@Test
	public void shouldGetAUserProfileByLogin() {
		User user = userService.getUserProfileByLogin("jdubois");
		assertThat(user.getEmail(), is("jdubois@ippon.fr"));
		assertThat(user.getTweetCount(), is(2L));
		assertThat(user.getFollowersCount(), is(3L));
		assertThat(user.getFriendsCount(), is(4L));
	}

	@Test
	public void shouldNotGetAUserProfileByLogin() {
		User user = userService.getUserProfileByLogin("unknownUserLogin");
		assertThat(user, nullValue());
	}

	@Test
	public void shouldUpdateUser() {
		String login = "uuser";
		String firstName = "UpdatedFirstName";
		String lastName = "UpdatedLastName";
		User userToUpdate = constructAUser(login, firstName, lastName);

		UserService userService = mockAuthenticationOnUserServiceWithACurrentUser(userToUpdate);

		User updatedUser = userService.getUserByLogin(login);

		assertThat(updatedUser.getFirstName(), is(firstName));
		assertThat(updatedUser.getLastName(), is(lastName));

	}

	@Test
	public void shouldCreateAUser() {
		String login = "nuser";
		String firstName = "New";
		String lastName = "User";
		String email = "nuser@ippon.fr";
		String gravatar = "newGravatar";

		User user = new User();
		user.setLogin(login);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setEmail(email);
		user.setGravatar(gravatar);

		userService.createUser(user);

		/* verify */
		User userToBeTheSame = userService.getUserProfileByLogin(login);
		assertThat(userToBeTheSame.getLogin(), is(user.getLogin()));
		assertThat(userToBeTheSame.getFirstName(), is(user.getFirstName()));
		assertThat(userToBeTheSame.getLastName(), is(user.getLastName()));
		assertThat(userToBeTheSame.getGravatar(), is(user.getGravatar()));
		assertThat(userToBeTheSame.getTweetCount(), is(0L));
		assertThat(userToBeTheSame.getFollowersCount(), is(0L));
		assertThat(userToBeTheSame.getFriendsCount(), is(0L));
	}

	@Test
	public void shouldFollowUser() {

		User userWhoWantToFollow = constructAUser("userWhoWantToFollow", "User", "WhoWantToFollow");
		UserService userService = mockAuthenticationOnUserServiceWithACurrentUser(userWhoWantToFollow);

		userService.followUser("userWhoWillBeFollowed");

		/* verify */
		User userWhoFollow = userService.getUserProfileByLogin("userWhoWantToFollow");
		assertThat(userWhoFollow.getFriendsCount(), is(1L));

		User userWhoIsFollowed = userService.getUserProfileByLogin("userWhoWillBeFollowed");
		assertThat(userWhoIsFollowed.getFollowersCount(), is(1L));
	}

	@Test
	public void shouldFollowUserBecauseUserNotExist() {

		User userWhoWantToFollow = constructAUser("userWhoWantToFollow", "User", "WhoWantToFollow");
		UserService userService = mockAuthenticationOnUserServiceWithACurrentUser(userWhoWantToFollow);

		userService.followUser("unknownUser");

		/* verify */
		User userWhoFollow = userService.getUserProfileByLogin("userWhoWantToFollow");
		assertThat(userWhoFollow.getFriendsCount(), is(1L));
	}

	@Test
	public void shouldNotFollowUserBecauseUserAlreadyFollowed() throws Exception {

		User currentUser = constructAUser("userWhoFollow", "User", "WhoFollow");
		UserService userService = mockAuthenticationOnUserServiceWithACurrentUser(currentUser);

		userService.followUser("userWhoIsFollowed");

		/* verify */
		User userWhoFollow = userService.getUserProfileByLogin("userWhoFollow");
		assertThat(userWhoFollow.getFriendsCount(), is(1L));
		assertThat(userWhoFollow.getFollowersCount(), is(0L));

		User userWhoIsFollowed = userService.getUserProfileByLogin("userWhoIsFollowed");
		assertThat(userWhoIsFollowed.getFriendsCount(), is(0L));
		assertThat(userWhoIsFollowed.getFollowersCount(), is(1L));
	}

	@Test
	public void shouldForgetUser() {
		User currentUser = constructAUser("userWhoWantToForget", "User", "WhoWantToforget");
		UserService userService = mockAuthenticationOnUserServiceWithACurrentUser(currentUser);

		userService.forgetUser("userToForget");

		/* verify */
		User userWhoWantToForget = userService.getUserProfileByLogin("userWhoWantToForget");
		assertThat(userWhoWantToForget.getFriendsCount(), is(0L));

		User userToForget = userService.getUserProfileByLogin("userToForget");
		assertThat(userToForget.getFollowersCount(), is(0L));
	}

	private UserService mockAuthenticationOnUserServiceWithACurrentUser(User authenticateUser) {
		AuthenticationService mockAuthenticationService = mock(AuthenticationService.class);
		when(mockAuthenticationService.getCurrentUser()).thenReturn(authenticateUser);
		userService.setAuthenticationService(mockAuthenticationService);

		userService.updateUser(authenticateUser);
		return userService;
	}

	private User constructAUser(String login, String firstName, String lastName) {
		User userToUpdate = new User();
		userToUpdate.setLogin(login);
		userToUpdate.setEmail("uuser@ippon.fr");
		userToUpdate.setFirstName(firstName);
		userToUpdate.setLastName(lastName);
		return userToUpdate;
	}
}