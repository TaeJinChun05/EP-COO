package core;

import lib.GameLib;
import core.CollisionManager;
import java.util.ArrayList;
import utils.*;
import java.awt.Color;

import entities.Entity;
import entities.spaceships.player.Player;
import entities.spaceships.enemies.*;
import entities.projectiles.Projectile;
import entities.powerups.*;
import strategies.shooting.*;
import strategies.movement.*;

// Para compilar e rodar o programa, utilize os seguintes comandos:
// 1°: javac -d bin -sourcepath src $(find src -name "*.java")
// 2°: java -cp bin core.Main

public class Main {

	// ------------------- CONSTANTES ------------------- //
	public static final int INACTIVE = 0;
	public static final int ACTIVE = 1;
	public static final int EXPLODING = 2;

	// -------------- ATRIBUTOS PRINCIPAIS -------------- //
	private Player player; // Instância do jogador
	private Background background1; // Primeira camada do fundo
	private Background background2; // Segunda camada do fundo
	private long currentTime = System.currentTimeMillis(); // Tempo atual
	long delta; // Tempo entre frames
	boolean running = true; // Flag para indicar se o jogo está em execução

	// ------------------- ARRAYLISTS ------------------- //
	private ArrayList<Enemy> enemies;
	private ArrayList<Projectile> playerProjectiles;
	private ArrayList<Projectile> enemyProjectiles;
	private ArrayList<Powerup> powerups;

	// ------------ LÓGICA DE SPAWNS (temp) ------------ //
	// Tempo do próximo spawn de Enemies 1 e 2 e Boss 1 e 2
	private long nextE1, nextE2, nextB1, nextB2;
	private long nextPowerupTime;

	// Construtor da classe Main
	public Main() {
		// Inicializa as listas
		this.enemies = new ArrayList<>();
		this.playerProjectiles = new ArrayList<>(50);
		this.enemyProjectiles = new ArrayList<>(150);
		this.powerups = new ArrayList<>(5);

		for(int i = 0; i < 50; i++) {playerProjectiles.add(new Projectile(0.0, -20.0, 0.0, 0.0, Color.GREEN));}
		for(int i = 0; i < 150; i++) {enemyProjectiles.add(new Projectile(0.0, -20, 0.0, 0.0, Color.RED));}

		// Inicializa as entidades principais
		this.player = new Player(7, playerProjectiles);
		this.background1 = new Background(0, 0.070, 20, 2);
		this.background2 = new Background(0, 0.045, 50, 3);

		// Lógica de spawn
		this.nextE1 = currentTime + 1000;
		this.nextE2 = currentTime + 3000;
		this.nextB1 = currentTime + 20000;
		this.nextB2 = currentTime + 80000;
		this.nextPowerupTime = currentTime + (long)(Math.random() * 15000);
	}

	// Método para lançar novos inimigos
	private void launchNewEnemies(long currentTime) {
	    // Lançando Inimigos do tipo 1
	    if (currentTime > nextE1) {
	    	double spawnX = Math.random() * (GameLib.WIDTH - 20.0) + 10.0;
	    	double spawnY = -10.0;
	    	Enemy newEnemy = Enemy.createEnemy(Enemy.INIMIGO_1, spawnX, spawnY, 1);
	        enemies.add(newEnemy); // Adiciona na lista unificada
	        nextE1 = currentTime + 1200;
	    }

	    // Lançando Inimigos do tipo 2
	    if (currentTime > nextE2) {
	    	double spawnX = GameLib.WIDTH * 0.20;
	    	double spawnY = -10.0;
	    	Enemy newEnemy = Enemy.createEnemy(Enemy.INIMIGO_2, spawnX, spawnY, 1);
	        enemies.add(newEnemy); // Adiciona na lista unificada
	        nextE2 = currentTime + 7000;
	    }

	    /* !! para implementar a lógica de spawn dos bosses você
	     * pode usar de exemplo o spawn dos inimigos comuns !! */
	    // Lançando Boss1
	    if (currentTime > nextB1) {
	        // TODO
	    }

		// Lançando Boss2
		if (currentTime > nextB2) {
			// TODO
		}
	}

	// Método para manter a taxa de quadros constante
	public static void busyWait(long time) {
		// Loop que espera até que o tempo atual atinja o tempo especificado
		while (System.currentTimeMillis() < time) {
			// Libera a CPU para outros processos enquanto espera
			Thread.yield();
		}
	}

	public void gameLoop() {
		boolean running = true; // Flag para controlar a execução do loop do jogo
		long delta; // Tempo entre frames
		currentTime = System.currentTimeMillis(); // Tempo atual em milissegundos

		GameLib.initGraphics(); // Inicializa a biblioteca gráfica

		// Loop principal do jogo
		while (running) {
			// Calcula o tempo delta desde o último frame
			delta = System.currentTimeMillis() - currentTime;
			// Atualiza o tempo atual
			currentTime = System.currentTimeMillis();

			// Verificação de colisões
			CollisionManager.checkAllCollisions(player, enemies, 
                                   playerProjectiles, enemyProjectiles, 
                                   powerups, currentTime);

			player.update(currentTime, delta);

			// Atualizações de estados dos projéteis do jogador
			for (Projectile projectile : playerProjectiles) {
				projectile.update(currentTime, delta);
			}

			// Atualizações de estados dos projéteis dos inimigos
			for (Projectile projectile : enemyProjectiles) {
				projectile.update(currentTime, delta);
			}

			// Atualizações de estados dos inimigos
			for (Enemy enemy : enemies) {
				enemy.update(delta, currentTime, enemyProjectiles);
			}

			// Atualizações de estados dos powerups
			for (Powerup powerup : powerups) {
				powerup.update(currentTime, delta);
			}

			// Lançamento de novos inimigos
			launchNewEnemies(currentTime);

			// Verificando se o usuário fechou o jogo
			if (GameLib.iskeyPressed(GameLib.KEY_ESCAPE)) {
				System.exit(0);
			}

			// Desenha a cena
			render(delta, currentTime);

			// Espera para manter o loop constante
			busyWait(currentTime + 5);
		}
	}

	private void render(long delta, long currentTime) {
		// Desenha o fundo
		background1.render1(delta);
		background2.render2(delta);

		// Desenha o player
		player.render(currentTime);

		// Desenha projéteis do player
		for (Projectile projectile : playerProjectiles) {
			projectile.render();
		}

		// desenha projéteis dos inimigos
		for (Projectile projectile : enemyProjectiles) {
			projectile.render();
		}

		// Desenha inimigos
		for (Enemy enemy : enemies) {
			enemy.render(currentTime);
		}

		// TODO: Renderizar powerups
		// e fazer uma barrinha de HP para o player e bosses

		// Mostra a tela atualizada
		GameLib.display();
	}

	public static int findFreeIndex(ArrayList<? extends Entity> entidades) {
		int i;
		for (i = 0; i < entidades.size(); i++) {
			if (entidades.get(i).getState() == INACTIVE)
				break;
		}
		return i;
	}

	public static int[] findFreeIndex(ArrayList<? extends Entity> entidades, int amount) {
		int i, k;
		int[] freeArray = { entidades.size(), entidades.size(), entidades.size() };
		for (i = 0, k = 0; i < entidades.size() && k < amount; i++) {
			if (entidades.get(i).getState() == INACTIVE) {
				freeArray[k] = i;
				k++;
			}
		}
		return freeArray;
	}

	public static void main(String[] args) {
		Main main = new Main();

		main.gameLoop();
	}
}