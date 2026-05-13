package com.example.bijlibachat.service;

import com.example.bijlibachat.dao.UserDAO;
import com.example.bijlibachat.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;

public class UserService {
    private static final int MAX_LOGIN_ATTEMPTS = 5;

    private final UserDAO userDAO = new UserDAO();
    private final Map<String, Integer> failedAttemptsByEmail = new HashMap<>();

    public User authenticateUser(String email, String password) {
        return authenticateLogin(email, password).getUser();
    }

    public AuthenticationResult authenticateLogin(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        if (isLockedOut(normalizedEmail)) {
            return AuthenticationResult.locked(0);
        }

        User user = userDAO.findByEmail(normalizedEmail);
        if (user == null) {
            return recordFailedAttempt(normalizedEmail);
        }

        String storedPassword = user.getPassword();
        if (storedPassword != null && isBcryptHash(storedPassword) && BCrypt.checkpw(password, storedPassword)) {
            failedAttemptsByEmail.remove(normalizedEmail);
            return AuthenticationResult.success(user);
        }

        if (storedPassword != null && storedPassword.equals(password)) {
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            userDAO.updatePassword(user.getUserID(), hashedPassword);
            user.setPassword(hashedPassword);
            failedAttemptsByEmail.remove(normalizedEmail);
            return AuthenticationResult.success(user);
        }

        return recordFailedAttempt(normalizedEmail);
    }

    public boolean registerUser(String name, String email, String password, String phone, String accountType) {
        String normalizedEmail = normalizeEmail(email);
        if (userDAO.findByEmail(normalizedEmail) != null) return false;

        User user = new User();
        user.setName(name);
        user.setMail(normalizedEmail);
        user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        user.setPhone(phone);
        user.setAccountType(accountType);
        userDAO.createUser(user);
        return true;
    }

    public boolean updateProfile(User u) {
        if (u.getName() == null || u.getName().isEmpty()) return false;
        userDAO.updateUser(u);
        return true;
    }

    public User getUserByEmail(String email) {
        return userDAO.findByEmail(normalizeEmail(email));
    }

    private AuthenticationResult recordFailedAttempt(String email) {
        int attempts = failedAttemptsByEmail.getOrDefault(email, 0) + 1;
        failedAttemptsByEmail.put(email, attempts);
        int remainingAttempts = Math.max(0, MAX_LOGIN_ATTEMPTS - attempts);
        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            return AuthenticationResult.locked(remainingAttempts);
        }
        return AuthenticationResult.failure(remainingAttempts);
    }

    private boolean isLockedOut(String email) {
        return failedAttemptsByEmail.getOrDefault(email, 0) >= MAX_LOGIN_ATTEMPTS;
    }

    private boolean isBcryptHash(String password) {
        return password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$");
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    public static final class AuthenticationResult {
        private final User user;
        private final boolean lockedOut;
        private final int remainingAttempts;

        private AuthenticationResult(User user, boolean lockedOut, int remainingAttempts) {
            this.user = user;
            this.lockedOut = lockedOut;
            this.remainingAttempts = remainingAttempts;
        }

        public static AuthenticationResult success(User user) {
            return new AuthenticationResult(user, false, MAX_LOGIN_ATTEMPTS);
        }

        public static AuthenticationResult failure(int remainingAttempts) {
            return new AuthenticationResult(null, false, remainingAttempts);
        }

        public static AuthenticationResult locked(int remainingAttempts) {
            return new AuthenticationResult(null, true, remainingAttempts);
        }

        public User getUser() {
            return user;
        }

        public boolean isAuthenticated() {
            return user != null;
        }

        public boolean isLockedOut() {
            return lockedOut;
        }

        public int getRemainingAttempts() {
            return remainingAttempts;
        }
    }
}
