package zan.wscard.card;

public class CardData {
	
	public final String id;
	public final String name;
	public final int type;
	public final int color;
	public final int level;
	public final int cost;
	public final int power;
	public final int soul;
	public final String trigger;
	public final String trait;
	public final String rarity;
	public final int side;
	public final String cardtext;
	public final String flavortext;
	public final String image;
	
	public CardData(String id, String name, int type, int color, int level, int cost, int power, int soul, String trigger, String trait, String rarity, int side, String cardtext, String flavortext, String image) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.color = color;
		this.level = level;
		this.cost = cost;
		this.power = power;
		this.soul = soul;
		this.trigger = trigger;
		this.trait = trait;
		this.rarity = rarity;
		this.side = side;
		this.cardtext = cardtext;
		this.flavortext = flavortext;
		this.image = image;
	}
	
}
