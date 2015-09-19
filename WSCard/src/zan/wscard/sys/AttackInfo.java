package zan.wscard.sys;

import static zan.wscard.sys.GameSystem.*;
import zan.wscard.card.CardData;

public class AttackInfo {

	private Player attacker = null;
	private Player defender = null;

	private int attackType = ATK_NONE;
	private int attackStage = STAGE_NONE;
	private int attackTrigger = CARD_NONE;

	private CardData attackerCard = null;
	private CardData defenderCard = null;
	private CardData triggerCard = null;

	public void clear() {
		attackType = ATK_NONE;
		attackStage = STAGE_NONE;
		attackTrigger = CARD_NONE;
		attackerCard = null;
		defenderCard = null;
		triggerCard = null;
	}

	public void setAttack(Player attacker, Player defender) {
		this.attacker = attacker;
		this.defender = defender;
	}

	public void setType(int type) {attackType = type;}
	public void setStage(int stage) {
		attackStage = stage;
		attackerCard = attacker.getStageCardData(getAttackerStage());
		if (attackType == ATK_FRONTAL || attackType == ATK_SIDE) {
			defenderCard = defender.getStageCardData(getDefenderStage());
		}
	}
	public void setTrigger(int trigger) {
		attackTrigger = trigger;
		triggerCard = attacker.getCardData(attackTrigger);
	}

	public int getDamage() {
		int attackersoul = attackerCard.soul;
		int triggersoul = 0;	// TODO triggerCard.trigger;
		int damage = attackersoul + triggersoul;
		if (attackType == ATK_DIRECT) {
			damage += 1;
		} else if (attackType == ATK_SIDE) {
			damage -= defenderCard.level;
		}
		return damage;
	}

	public int getBattleResult() {
		if (attackType == ATK_FRONTAL) {
			if (attackerCard.power > defenderCard.power) return BTL_ATTACKER;
			else if (attackerCard.power < defenderCard.power) return BTL_DEFENDER;
			else return BTL_TIE;
		}
		return BTL_NONE;
	}

	public int getAttackerStage() {return attackStage;}
	public int getDefenderStage() {return 2-attackStage;}

}
