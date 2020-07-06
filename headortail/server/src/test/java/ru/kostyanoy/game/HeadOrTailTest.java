package ru.kostyanoy.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class HeadOrTailTest {

    @Test
    void changePlayerStateByGame() {
        Game game = new HeadOrTail();
        Player player1 = game.createNewPlayer();
        Player player2 = new Player(player1.getAccount());
        assertEquals(player1, player2);
        game.changePlayerStateByGame(player1.getAccount() >> 1, player1, game.getPossibleMovies().get(0));
        assertNotEquals(player1, player2);
    }
}