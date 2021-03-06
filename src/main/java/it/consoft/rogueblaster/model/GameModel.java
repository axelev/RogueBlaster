package it.consoft.rogueblaster.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.consoft.rogueblaster.model.enumeration.AttrEnum;

public class GameModel {

	private MainCharModel character = null;
	private MapModel map = new MapModel();

	private int[] charCoords;
	private List<int[]> enemiesCoords;
	private List<int[]> chestsCoords;

	public GameModel() {

	}

	public MainCharModel getMc() {
		return character;
	}

	public void setMc(MainCharModel mc) {
		this.character = mc;
	}

	public MapModel getMap() {
		return map;
	}

	public void setMap(MapModel map) {
		this.map = map;
	}

	public void gameOver() {

	}

	public void update() {

	}

	public void setup(MapModel m, MainCharModel mChar) {
		this.map = m;
		this.character = mChar;
		EnemyModel enemy;
		ChestModel chest;
		this.enemiesCoords = new ArrayList<int[]>();
		this.chestsCoords = new ArrayList<int[]>();
		int[] coords = this.generateCoords();
		while (!this.map.setTileContent(coords[0], coords[1], mChar)) {
			System.out
					.println("Inserimento mainCharacter non riuscito alle coordinate: " + coords[0] + "," + coords[1]);
		}
		this.charCoords = coords;
		this.map.setMainAlive(true);
		System.out.println(mChar.toJSON());
		System.out.println("Inserimento mainCharacter riuscito alle coordinate: " + coords[0] + "," + coords[1]);
		// blocco nemico
		for (int i = 0; i < this.map.getMaxEnemy(); i++) {
			enemy = new EnemyModel();
			coords = this.generateCoords();
			while (!this.map.setTileContent(coords[0], coords[1], enemy)) {
				System.out.println(
						"Inserimento Enemy " + i + " non riuscito alle coordinate: " + coords[0] + "," + coords[1]);
				coords = this.generateCoords();
			}
			this.enemiesCoords.add(coords);
			System.out.println("Inserimento Enemy " + i + " riuscito alle coordinate: " + coords[0] + "," + coords[1]);
		}
		System.out.println(this.enemiesCoords.toString());

		// blocco tesoro
		for (int i = 0; i < this.map.getMaxTeasure(); i++) {
			chest = new ChestModel((AttrEnum.getById((int) (Math.random() * 4) + 1)));
			coords = this.generateCoords();
			while (!this.map.setTileContent(coords[0], coords[1], chest)) {
				System.out.println(
						"Inserimento Teasure " + i + " non riuscito alle coordinate: " + coords[0] + "," + coords[1]);
				coords = this.generateCoords();
			}
			this.chestsCoords.add(coords);
			System.out
					.println("Inserimento Teasure " + i + " riuscito alle coordinate: " + coords[0] + "," + coords[1]);
		}
		System.out.println(this.map);
	}

	private int[] generateCoords() {
		int[] result = new int[2];
		result[0] = (int) (Math.random() * this.map.getWidth());
		result[1] = (int) (Math.random() * this.map.getHeight());
		return result;
	}

	@Scheduled(fixedRate = 20000, initialDelay = 10000)
	private void simulate() {
		int turn = -1;
		int newX = 0, newY = 0;
		try {
			while (turn == -1) {
				newX = this.charCoords[0] + ((int) (Math.random() * 3)) - 1;
				newY = this.charCoords[1] + ((int) (Math.random() * 3)) - 1;
				turn = this.map.moveMainChar(this.charCoords[0], this.charCoords[1], newX, newY);
			}
			System.out.println("Mossa Main Char: " + newX + " " + newY + "\n" + this.map);
			if (this.map.isEnemySlayed()) {
				this.map.setEnemySlayed(false);
				for (int[] a : this.enemiesCoords) {
					if (a[0] == newX && a[1] == newY) {
						System.out.println("Nemico rimosso dall'array");
						this.enemiesCoords.remove(a);
					}
				}
				System.out.println("Nemico ucciso. " + "\n" + this.map);
				if (this.enemiesCoords.size() == 0) {
					System.out.println(this.map);
					System.exit(0);
				}
			}
			if (turn == 0) {
				System.out.println("Mi muovo: " + newX + " " + newY);
				this.charCoords[0] = newX;
				this.charCoords[1] = newY;
			}
			if (turn == 2) {
				System.out.println("Attacco");
			}
			turn = -1;
			for (int i = 0; i < this.enemiesCoords.size(); i++) {
				System.out.println("Mossa del nemico " + i);
				while (turn == -1) {
					newX = this.enemiesCoords.get(i)[0] + ((int) (Math.random() * 3)) - 1;
					newY = this.enemiesCoords.get(i)[1] + ((int) (Math.random() * 3)) - 1;
					System.out.println(this.enemiesCoords.get(i)[0]);
					System.out.println(this.enemiesCoords.get(i)[1]);
					turn = this.map.moveEnemy(this.enemiesCoords.get(i)[0], this.enemiesCoords.get(i)[1], newX, newY);
				}
				if (!this.map.isMainAlive()) {
					System.out.println(this.map);
					System.exit(0);
				}
				if (turn == 0) {
					this.enemiesCoords.get(i)[0] = newX;
					this.enemiesCoords.get(i)[1] = newY;
					System.out.println(this.enemiesCoords.get(i)[0]);
					System.out.println(this.enemiesCoords.get(i)[1]);
					System.out.println("Nemico si muove: " + newX + " " + newY);
				}
				if (turn == 2) {
					System.out.println("Il nemico attacca: " + newX + " " + newY);
				}
				turn = -1;
			}
			System.out.println(this.character.toJSON());
			System.out.println("Main: " + this.charCoords[0] + " " + this.charCoords[1]);
			System.out.println("Main Life: " + this.character.getVit());
			System.out.println(this.map);
		} catch (IndexOutOfBoundsException e) {
			return;
		}
	}

	public String toJSON() {
		Gson gson = new GsonBuilder().create();
		return gson.toJson(this);
	}

}
