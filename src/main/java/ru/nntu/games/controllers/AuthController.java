package ru.nntu.games.controllers;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nntu.games.entities.Role;
import ru.nntu.games.entities.User;
import ru.nntu.games.models.enums.RoleEnum;
import ru.nntu.games.payload.requests.SignInRequest;
import ru.nntu.games.payload.requests.SignUpRequest;
import ru.nntu.games.payload.responses.JWTResponse;
import ru.nntu.games.payload.responses.MessageResponse;
import ru.nntu.games.repositories.RoleRepository;
import ru.nntu.games.repositories.UserRepository;
import ru.nntu.games.security.jwt.JWTUtils;
import ru.nntu.games.security.services.GamesUserDetails;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder encoder;

    private final JWTUtils jwtUtils;

    public AuthController(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder encoder,
            JWTUtils jwtUtils
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/sign_in")
    public ResponseEntity<?> authenticateUser(@RequestBody @NotNull SignInRequest signInRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        signInRequest.getUsername(),
                        signInRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtUtils.generateJWT(authentication);

        GamesUserDetails userDetails = (GamesUserDetails) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                new JWTResponse(
                        token,
                        userDetails.getId(),
                        userDetails.getUsername(),
                        userDetails.getEmail(),
                        roles
                )
        );
    }

    @PostMapping("/sign_up")
    public ResponseEntity<?> registerUser(@RequestBody @NotNull SignUpRequest signUpRequest) {
        String username = signUpRequest.getUsername();
        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        String email = signUpRequest.getEmail();
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        User user = new User(
                username,
                email,
                encoder.encode(signUpRequest.getPassword())
        );

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(RoleEnum.USER).orElseThrow(
                    () -> new RuntimeException("Error: Role is not found.")
            );

            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(RoleEnum.ADMIN).orElseThrow(
                                () -> new RuntimeException("Error: Role is not found.")
                        );

                        roles.add(adminRole);

                        break;
                    case "mod":
                        Role modRole = roleRepository.findByName(RoleEnum.MODERATOR).orElseThrow(
                                () -> new RuntimeException("Error: Role is not found.")
                        );

                        roles.add(modRole);

                        break;
                    default:
                        Role userRole = roleRepository.findByName(RoleEnum.USER).orElseThrow(
                                () -> new RuntimeException("Error: Role is not found.")
                        );

                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}
