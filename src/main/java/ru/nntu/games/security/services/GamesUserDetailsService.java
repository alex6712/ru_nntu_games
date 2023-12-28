package ru.nntu.games.security.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nntu.games.entities.User;
import ru.nntu.games.repositories.UserRepository;

@Service
public class GamesUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public GamesUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с таким именем не найден: " + username));

        return GamesUserDetails.build(user);
    }
}
