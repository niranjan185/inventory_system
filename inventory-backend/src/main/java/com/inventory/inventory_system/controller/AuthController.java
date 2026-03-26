package com.inventory.inventory_system.controller;

import com.inventory.inventory_system.config.AppConfig;
import com.inventory.inventory_system.dto.AuthResponse;
import com.inventory.inventory_system.dto.LoginRequest;
import com.inventory.inventory_system.entity.User;
import com.inventory.inventory_system.security.JwtUtils;
import com.inventory.inventory_system.service.UserService;
import com.inventory.inventory_system.service.AuthService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private AppConfig appConfig;

    // Register user
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        try {
            user.setRole("USER");
            User savedUser = userService.registerUser(user);
            
            String token = jwtUtils.generateJwtToken(savedUser.getEmail(), savedUser.getRole());
            
            return ResponseEntity.ok(new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // User login
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        
        User loggedUser = userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
        
        if (loggedUser != null) {
            String token = jwtUtils.generateJwtToken(loggedUser.getEmail(), loggedUser.getRole());
            
            return ResponseEntity.ok(new AuthResponse(
                token,
                loggedUser.getId(),
                loggedUser.getName(),
                loggedUser.getEmail(),
                loggedUser.getRole()
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid credentials"));
        }
    }

    // Admin login
    @PostMapping("/admin-login")
    public ResponseEntity<?> adminLogin(@Valid @RequestBody LoginRequest loginRequest) {

        if (loginRequest.getEmail().equals(appConfig.getAdmin().getEmail()) &&
            loginRequest.getPassword().equals(appConfig.getAdmin().getPassword())) {

            String token = jwtUtils.generateJwtToken(appConfig.getAdmin().getEmail(), "ADMIN");
            
            return ResponseEntity.ok(new AuthResponse(
                token,
                1L,
                "Admin",
                appConfig.getAdmin().getEmail(),
                "ADMIN"
            ));
        }

        return ResponseEntity.badRequest().body(Map.of("error", "Invalid admin credentials"));
    }

    // ==================== FORGOT PASSWORD ENDPOINTS ====================

    /**
     * Step 1: Send OTP to email
     * POST /api/auth/forgot-password
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }

            String result = authService.sendOtpToEmail(email);
            return ResponseEntity.ok(Map.of(
                "message", result,
                "email", email.trim().toLowerCase(),
                "success", true
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "success", false
            ));
        }
    }

    /**
     * Step 2: Verify OTP
     * POST /api/auth/verify-otp
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String otp = request.get("otp");
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }
            
            if (otp == null || otp.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "OTP is required"));
            }

            boolean isValid = authService.verifyOtp(email, otp);
            
            if (isValid) {
                return ResponseEntity.ok(Map.of(
                    "message", "OTP verified successfully",
                    "verified", true,
                    "success", true
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid or expired OTP",
                    "verified", false,
                    "success", false
                ));
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "verified", false,
                "success", false
            ));
        }
    }

    /**
     * Step 3: Reset password
     * POST /api/auth/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String otp = request.get("otp");
            String newPassword = request.get("newPassword");
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }
            
            if (otp == null || otp.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "OTP is required"));
            }
            
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "New password is required"));
            }

            String result = authService.resetPassword(email, otp, newPassword);
            
            return ResponseEntity.ok(Map.of(
                "message", result,
                "success", true
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "success", false
            ));
        }
    }

    /**
     * Check OTP status (optional endpoint for frontend validation)
     * GET /api/auth/otp-status?email=user@example.com
     */
    @GetMapping("/otp-status")
    public ResponseEntity<?> getOtpStatus(@RequestParam String email) {
        try {
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }

            boolean hasValidOtp = authService.hasValidOtp(email);
            long remainingMinutes = authService.getOtpRemainingMinutes(email);
            
            return ResponseEntity.ok(Map.of(
                "email", email.trim().toLowerCase(),
                "hasValidOtp", hasValidOtp,
                "remainingMinutes", remainingMinutes,
                "success", true
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "success", false
            ));
        }
    }
}