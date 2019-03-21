package fr.rochet.playgroundobjects;

import fr.rochet.enemies.Werewolf;
import fr.rochet.items.Item;
import fr.rochet.items.ItemType;
import fr.rochet.items.Torch;
import fr.rochet.levels.Level;
import fr.rochet.enemies.Enemy;
import fr.rochet.objects.Exit;
import fr.rochet.objects.Obstacle;
import fr.rochet.objects.Player;
import fr.rochet.utils.RunGameException;

import java.util.ArrayList;
import java.util.List;

public class Playground {

    private int width;
    private int height;

    private List<Player> players;
    private List<Enemy> enemies;
    private List<Obstacle> obstacles;
    private List<Exit> exits;
    private List<Torch> torches;
    private List<Item> items;

    //<editor-fold desc="Fonctions d'initialisation">

    public Playground() {
        this.players = new ArrayList<>();
        this.enemies = new ArrayList<>();
        this.obstacles = new ArrayList<>();
        this.exits = new ArrayList<>();
        this.torches = new ArrayList<>();
        this.items = new ArrayList<>();
    }

    /**
     * On initialise le niveau avec l'objet level passé
     */
    public void initialize(Level level) throws RunGameException {

        this.height = level.getMap().length;
        this.width = level.getMap()[0].length;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                switch (level.getMap()[y][x]) {
                    case "#":
                        obstacles.add(new Obstacle(x, y));
                        break;
                    case "P":
                        Player player = new Player(x, y, level.getVisionDistance());
                        player.getInventory().addItem(ItemType.TORCH);
                        players.add(player);
                        break;
                    case "E": // TODO : différencier les types d'ennemis sur la carte
                        enemies.add(new Werewolf(x, y));
                        break;
                    case "X":
                        exits.add(new Exit(x, y));
                        break;
                    case "T":
                        torches.add(new Torch(x, y));
                        break;
                }
            }
        }
    }

    //</editor-fold>

    //<editor-fold desc="Fonctions principales de jeu">

    /**
     * Fonction principale déroulant le jeu sur le terrain
     */
    public void play() throws RunGameException {
        int gameResult;
        while ((gameResult = isGameOver()) == 0) {
            players.forEach(player -> {
                printConsole();
                player.getInventory().printConsole();
                player.move(obstacles, exits, torches);
            });
            for (Enemy enemy : enemies) {
                printConsole();
                enemy.move(players, obstacles, enemies);
            }
        }

        // Fin du jeu
        printConsole();
        if (gameResult < 0) {
            System.out.println("Vous vous êtes fait dévoré tout cru");
        } else {
            System.out.println("Bravo ! Vous avez réussi à vous échapper");
        }
    }

    /**
     * Vérifie si le jeu est terminé (gagné ou perdu)
     *
     * @return -1 si perdu, 1 si gagné, 0 sinon
     */
    private int isGameOver() {
        if (players.stream().allMatch(Player::isOut)) {
            return 1;
        }

        // Si au moins un joueur est sur la meme position que au moins un ennemi
        if (players.stream().anyMatch(player -> enemies.stream().anyMatch(enemy -> enemy.isAtSamePosition(player)))) {
            return -1;
        }

        return 0;
    }

    //</editor-fold>

    //<editor-fold desc="Fonctions d'affichage">

    /**
     * Permet d'afficher provisoirement le terrain de jeu dans la console
     * Peut etre amélioré (bordures (obstacles -> évite de sortir du terrain))
     */
    private void printConsole() {
        clearConsole();
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int finalX = i;
                int finalY = j;

                // On vérifie si 1 joueur / torche à la vision sur le point
                // Si un joueur possède une torche, on prend la distance d'éclairage de la torche autour de lui, car celle ci est plus grande que sa vision
                if (players.stream().anyMatch(player -> Math.pow(Math.abs(player.getX() - finalX), 2) + Math.pow(Math.abs(player.getY() - finalY), 2) < Math.pow(player.carryTorch() ? Torch.LIGHT_RANGE : player.getVisionRange(), 2))
                        || torches.stream().anyMatch(torch -> Math.pow(Math.abs(torch.getX() - finalX), 2) + Math.pow(Math.abs(torch.getY() - finalY), 2) < Math.pow(Torch.LIGHT_RANGE, 2))) {
                    if (enemies.stream().anyMatch(x -> x.getX() == finalX && x.getY() == finalY)) {
                        System.out.print('E');
                    } else if (players.stream().anyMatch(x -> x.getX() == finalX && x.getY() == finalY)) {
                        System.out.print('P');
                    } else if (exits.stream().anyMatch(x -> x.getX() == finalX && x.getY() == finalY)) {
                        System.out.print('X');
                    } else if (torches.stream().anyMatch(x -> x.getX() == finalX && x.getY() == finalY)) {
                        System.out.print('T');
                    } else if (obstacles.stream().anyMatch(x -> x.getX() == finalX && x.getY() == finalY)) {
                        System.out.print('#');
                    } else {
                        System.out.print(' ');
                    }
                } else {
                    System.out.print('o');
                }
            }
            System.out.println();
        }
    }

    /**
     * Vide la console
     */
    private void clearConsole() {
        try {
            if (System.getProperty("os.name").startsWith("Windows")) {
                Runtime.getRuntime().exec("cls");
            } else {
                Runtime.getRuntime().exec("clear");
            }
        } catch (Exception excpt) {
            for (int i = 0; i < 20; i++) {
                System.out.println();
            }
        }
    }

    //</editor-fold>
}
