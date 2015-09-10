package zan.wscard.card;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import zan.lib.util.Utility;

public class CardReader {

	public CardReader() {}

	public ArrayList<CardData> loadCardData(String fnm) {
		ArrayList<CardData> cardData = new ArrayList<CardData>();

		try {
			String id = "NONE";
			String name = "NONE";
			int type = -1;
			int color = -1;
			int level = -1;
			int cost = -1;
			int power = -1;
			int soul = -1;
			String trigger = "NONE";
			String trait = "NONE";
			String rarity = "NONE";
			int side = -1;
			String cardtext = "NONE";
			String flavortext = "NONE";
			String image = "NONE";

			BufferedReader br = new BufferedReader(new FileReader(fnm));
			String line;
			while((line = br.readLine()) != null) {
				if (line.length() == 0)	continue;
				String[] tkns = line.split(" ");
				if (tkns[0].isEmpty() || tkns[0].startsWith("//")) continue;

				if (tkns[0].startsWith("ID")) {
					if (!id.contentEquals("NONE")) {
						cardData.add(new CardData(id, name, type, color, level, cost, power, soul, trigger, trait, rarity, side, cardtext, flavortext, image));
					}
					id = tkns[1];
				} else if (tkns[0].startsWith("NAME")) {
					name = "";
					for (int i=1;i<tkns.length;i++) {
						name += tkns[i];
						if (i < tkns.length-1) name += " ";
					}
				} else if (tkns[0].startsWith("TYPE")) {
					if (tkns[1].contentEquals("CHARA")) type = 0;
					else if (tkns[1].contentEquals("CLIMAX")) type = 1;
					else if (tkns[1].contentEquals("EVENT")) type = 2;
					else type = -1;
				} else if (tkns[0].startsWith("COLOR")) {
					if (tkns[1].contentEquals("RED")) color = 0;
					else if (tkns[1].contentEquals("BLUE")) color = 1;
					else if (tkns[1].contentEquals("GREEN")) color = 2;
					else if (tkns[1].contentEquals("YELLOW")) color = 3;
					else color = -1;
				} else if (tkns[0].startsWith("LEVEL")) {
					level = Utility.parseInt(tkns[1]);
				} else if (tkns[0].startsWith("COST")) {
					cost = Utility.parseInt(tkns[1]);
				} else if (tkns[0].startsWith("POWER")) {
					power = Utility.parseInt(tkns[1]);
				} else if (tkns[0].startsWith("SOUL")) {
					soul = Utility.parseInt(tkns[1]);
				} else if (tkns[0].startsWith("TRIGGER")) {
					trigger = tkns[1];
				} else if (tkns[0].startsWith("TRAIT")) {
					trait = "";
					for (int i=1;i<tkns.length;i++) {
						trait += tkns[i];
						if (i < tkns.length-1) trait += " ";
					}
				} else if (tkns[0].startsWith("RARITY")) {
					rarity = tkns[1];
				} else if (tkns[0].startsWith("SIDE")) {
					if (tkns[1].contentEquals("W")) side = 0;
					else if (tkns[1].contentEquals("S")) side = 1;
					else side = -1;
				} else if (tkns[0].startsWith("CARDTEXT")) {
					cardtext = "";
					for (int i=1;i<tkns.length;i++) {
						cardtext += tkns[i];
						if (i < tkns.length-1) cardtext += " ";
					}
				} else if (tkns[0].startsWith("FLAVORTEXT")) {
					flavortext = "";
					for (int i=1;i<tkns.length;i++) {
						flavortext += tkns[i];
						if (i < tkns.length-1) flavortext += " ";
					}
				} else if (tkns[0].startsWith("IMAGE")) {
					image = tkns[1];
				}
			}
			if (!id.contentEquals("NONE")) {
				cardData.add(new CardData(id, name, type, color, level, cost, power, soul, trigger, trait, rarity, side, cardtext, flavortext, image));
			}

			br.close();
		} catch (IOException e) {
			System.err.println("Error reading file " + fnm + ":\n " + e);
		}

		return cardData;
	}

}
