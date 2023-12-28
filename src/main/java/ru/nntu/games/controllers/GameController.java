package ru.nntu.games.controllers;

import org.springframework.web.bind.annotation.*;
import ru.nntu.games.entities.Game;
import ru.nntu.games.repositories.GameRepository;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/games")
public class GameController {
    private final GameRepository gameRepository;

    public GameController(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @GetMapping
    public Iterable<Game> getGames() {
        return gameRepository.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Game> getGameById(@PathVariable UUID id) {
        return gameRepository.findById(id);
    }

    @GetMapping("/{name}")
    public Optional<Game> getGameByName(@PathVariable String name) {
        return gameRepository.findByName(name);
    }

    @PostMapping
    public Game postGame(@RequestBody Game game) {
        return gameRepository.save(game);
    }

    @DeleteMapping("/{id}")
    public void deleteGameById(@PathVariable UUID id) {
        gameRepository.deleteById(id);
    }
}
