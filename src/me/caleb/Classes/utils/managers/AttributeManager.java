package me.caleb.Classes.utils.managers;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import me.caleb.Classes.Main;
import me.caleb.Classes.utils.Utils;

public class AttributeManager extends Utils{

	protected Player p;
	protected Main plugin;
	protected ConfigManager cma;
	protected HashMap<String, Double> attr;

	/*
	 * Attributes are the following:
	 *
	 * Strength
	 * Quickness
	 * SpellPower
	 * Toughness
	 * Resistance
	 * RangedDamage
	 * MaxHealth
	 *
	 */

	public AttributeManager(Main plugin, Player p, String cl) {
		this.p = p;
		this.plugin = plugin;
		cma = new ConfigManager(plugin, "attributes.yml");
		attr = getPlayerAttributes();
	}

	public AttributeManager(Main plugin, Entity e) {
		this.plugin = plugin;
		this.p = Bukkit.getPlayer(e.getUniqueId());
		cma = new ConfigManager(plugin, "attributes.yml");
		attr = getPlayerAttributes();
	}

	public double getMaxDifference() {
		FileConfiguration config = plugin.getConfig();
		return config.getDouble("MaxDifferenceAmount");
	}

	public void applyAttributes() {

		HashMap<String, Double> playerAttributes = getPlayerAttributes();

		double hpAdditive = Double.parseDouble(cma.getValue("Attributes.MaxHealth." + playerAttributes.get("MaxHealth").intValue()));
		double strengthMult = Double.parseDouble(cma.getValue("Attributes.Strength." + playerAttributes.get("Strength").intValue()));
		double quicknessMult = Double.parseDouble(cma.getValue("Attributes.Quickness." + playerAttributes.get("Quickness").intValue()));

		p.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).addModifier(new AttributeModifier("Strength", strengthMult, Operation.MULTIPLY_SCALAR_1));
		p.getAttribute(Attribute.GENERIC_MAX_HEALTH).addModifier(new AttributeModifier("Max_HP", hpAdditive, Operation.ADD_NUMBER));
		p.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).addModifier(new AttributeModifier("Movement_Speed", quicknessMult, Operation.MULTIPLY_SCALAR_1));
	}

	public void removeAttributes() {

		for(AttributeModifier a : p.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getModifiers()) {
			p.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).removeModifier(a);
		}

		for(AttributeModifier a : p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getModifiers()) {
			p.getAttribute(Attribute.GENERIC_MAX_HEALTH).removeModifier(a);
		}

		for(AttributeModifier a : p.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getModifiers()) {
			p.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).removeModifier(a);
		}

		p.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.1);
	}

	/*
	Used for the CustomEnchants plugin (Enchant - Muscle Sap)
	 */
	public void setNewAttributes(HashMap<String, Double> newAttr, Player p){

		ConfigManager cm = new ConfigManager(plugin, "players.yml");
		List<String> attrList = cm.getList("Players." + p.getName() + ".Attributes");

		attrList.set(2, "RangedDamage: " + newAttr.get("RangedDamage"));
		attrList.set(4, "SpellPower: " + newAttr.get("SpellPower"));
		attrList.set(5, "Strength: " + newAttr.get("Strength"));
		attrList.set(6, "Toughness: " + newAttr.get("Toughness"));

		cm.setValue("Players." + p.getName() + ".Attributes", attrList);

		cm.saveCustomConfig();
	}

	/*
	Used for the CustomEnchants plugin (Enchant - Muscle Sap)
	 */
	public void restoreAttributes(HashMap<String, Double> oldAttr, Player p){

		ConfigManager cm = new ConfigManager(plugin, "players.yml");
		List<String> attrList = cm.getList("Players." + p.getName() + ".Attributes");

		attrList.set(2, "RangedDamage: " + oldAttr.get("RangedDamage"));
		attrList.set(4, "SpellPower: " + oldAttr.get("SpellPower"));
		attrList.set(5, "Strength: " + oldAttr.get("Strength"));
		attrList.set(6, "Toughness: " + oldAttr.get("Toughness"));

		cm.setValue("Players." + p.getName() + ".Attributes", attrList);

		cm.saveCustomConfig();
	}

	public FileConfiguration getCustomEnchantsConfig(){
		return plugin.getServer().getPluginManager().getPlugin("CustomEnchants").getConfig();
	}

	/*
	Going back to classes normal speed without enchants
	 */
	public void reApplyClassSpeedModifier() {

		double quicknessMult;

		for (AttributeModifier a : p.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getModifiers()) {
			p.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).removeModifier(a);
		}

		p.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.1);

		if (attr.get("Quickness") == null || cma.getValue("Attributes.Quickness.") == null) {
			quicknessMult = 1;
		} else {
			quicknessMult = Double.parseDouble(cma.getValue("Attributes.Quickness." + attr.get("Quickness").intValue()));
		}

		p.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).addModifier(new AttributeModifier("Movement_Speed", quicknessMult, Operation.MULTIPLY_SCALAR_1));

	}
	public void reApplyClassStrengthModifier() {

		double StrengthMult;

		for (AttributeModifier a : p.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getModifiers()) {
			p.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).removeModifier(a);
		}

		p.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(0.1);

		if (attr.get("Strength") == null || cma.getValue("Attributes.Strength.") == null) {
			StrengthMult = 1;
		} else {
			StrengthMult = Double.parseDouble(cma.getValue("Attributes.Strength." + attr.get("Strength").intValue()));
		}

		p.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).addModifier(new AttributeModifier("Strength", StrengthMult, Operation.MULTIPLY_SCALAR_1));

	}

	/*
	Was originally for Frozen Touch Enchant
	 */
	public void restoreSpeedModifiers(Collection<AttributeModifier> sm){
		removeSpeedModifiers();
		for(AttributeModifier a : sm){
			p.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).addModifier(a);
		}
	}

	/*
	Going back to default Minecraft Attribute movement
	 */
	public void removeSpeedModifiers(){
		for(AttributeModifier a : p.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getModifiers()) {
			p.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).removeModifier(a);
		}
		p.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.1);
	}

	public void removeStrengthModifiers(){
		for(AttributeModifier a : p.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getModifiers()){
			p.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).removeModifier(a);
		}
	}


	/*
	For the swift foot enchant and Frozen Touch Enchant
	 */
	public void setSpeedModifier(int lvl, String enchantInitials){

		double currentSpeed = p.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue();
		FileConfiguration config = getCustomEnchantsConfig();

		if(enchantInitials.equalsIgnoreCase("SF")){

			double mult = config.getDouble("Enchants.Swift_Foot.PercantageIncreasePerLevel." + lvl);

			AttributeModifier speedModifier = null;
			double newMult;
			for(AttributeModifier am : p.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getModifiers()){
				if(am.getName().equalsIgnoreCase("Movement_Speed")){
					speedModifier = am;
				}
			}
			//Just in-case this is null, return. This should rarely be null.
			if(speedModifier == null){
				return;
			}
			newMult = speedModifier.getAmount() + mult;

			removeSpeedModifiers();
			p.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).addModifier(new AttributeModifier("Movement_Speed", newMult, Operation.MULTIPLY_SCALAR_1));

		}else if(enchantInitials.equalsIgnoreCase("FT")){

			double mult = config.getDouble("Enchants.Frozen_Touch.DecreasedPercentagePerLevel." + lvl);

			AttributeModifier speedModifier = null;
			double newMult;
			for(AttributeModifier am : p.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getModifiers()){
				if(am.getName().equalsIgnoreCase("Movement_Speed")){
					speedModifier = am;
				}
			}
			//Just in-case this is null, return. This should rarely be null.
			if(speedModifier == null){
				return;
			}

			removeSpeedModifiers();
			newMult = speedModifier.getAmount() - mult;
			p.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).addModifier(new AttributeModifier("Movement_Speed", newMult, Operation.MULTIPLY_SCALAR_1));

		}

	}

	/*
	For the Berserker Enchant
	 */
	public void setStrengthModifier(int lvl){
		Bukkit.broadcastMessage("Modifiers: " + p.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE));
		FileConfiguration config = getCustomEnchantsConfig();
		double mult = config.getDouble("Enchants.Berserker.StrengthIncreasePerLevel" + lvl);

		AttributeModifier speedModifier = null;
		double newMult;
		for(AttributeModifier am : p.getAttribute(Attribute.GENERIC_ATTACK_SPEED).getModifiers()){
			if(am.getName().equalsIgnoreCase("Strength")){
				speedModifier = am;
			}
		}
		//Just in-case this is null, return. This should rarely be null.
		if(speedModifier == null){
			return;
		}

		removeStrengthModifiers();
		newMult = speedModifier.getAmount() + mult;
		Bukkit.broadcastMessage("Old mult: " + speedModifier.getAmount());
		Bukkit.broadcastMessage("New mult: " + newMult);
		p.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).addModifier(new AttributeModifier("Strength", newMult, Operation.MULTIPLY_SCALAR_1));


	}

	/*
	 * Factors in RangedDmg attribute
	 */
	public double getNewRangedDmg(double initDmg) {

		double rangedDmgMult;

		if(attr.get("RangedDamage").intValue() != 1) {
			rangedDmgMult = Double.parseDouble(cma.getValue("Attributes.RangedDamage." + attr.get("RangedDamage").intValue()));
		}else {
			rangedDmgMult = 1.0;
		}

		double temp = initDmg * rangedDmgMult;
		double newDmg = initDmg + temp;
		return ThreadLocalRandom.current().nextDouble(newDmg, (newDmg + getMaxDifference()));
	}

	/*
	 * Factors in Toughness attribute
	 */
	public double factorInToughness(double initDmg) {

		try {
			double toughnessMult = Double.parseDouble(cma.getValue("Attributes.Toughness." + attr.get("Toughness").intValue()));
			double temp = initDmg * toughnessMult;
			return (initDmg - temp);
		}catch(NumberFormatException e) {
			//If the value is normal
			return initDmg;
		}

	}

	/*
	 * Factors in Resistance attribute
	 */
	public double factorInResistance(double initDmg, potionTypes potion) {

		try {
			double resistanceMult = Double.parseDouble(cma.getValue("Attributes.Resistance." + attr.get("Resistance").intValue()));
			double temp = initDmg * resistanceMult;
			return (initDmg - temp);
		}catch(NumberFormatException e) {
			return initDmg;
		}
			/*
		}else if(potion.equals(potionTypes.SLOWNESS)) {
			*/
	}

	public enum potionTypes{
		POISON,
		SLOWNESS
	}

	/*
	 * Melee hits
	 */
	public double getNewHitDmg(double initDmg) {
		double strengthMult = Double.parseDouble(cma.getValue("Attributes.Strength." + attr.get("Strength").intValue()));
		double temp = initDmg * strengthMult;
		double newDmg = initDmg + temp;
		return ThreadLocalRandom.current().nextDouble(newDmg, (newDmg + getMaxDifference()));
	}

	/*
	 * Spell damaged
	 */
	public double getNewSpellDmg(double initDmg) {
		double spellPowerMult = 1;
		try {
			spellPowerMult = Double.parseDouble(cma.getValue("Attributes.SpellPower." + attr.get("SpellPower").intValue()));
		}catch(NumberFormatException e) {
			return initDmg;
		}

		double temp = initDmg * spellPowerMult;
		double newDmg = initDmg + temp;
		return ThreadLocalRandom.current().nextDouble(newDmg, (newDmg + getMaxDifference()));
	}

	public HashMap<String, Double> getPlayerAttributes() {

		ConfigManager cm = new ConfigManager(plugin, "players.yml");

		HashMap<String, Double> attributes = new HashMap();
		List<String> attr = cm.getList("Players." + p.getName() + ".Attributes");

		for(String line : attr) {
			attributes.put(getKey(line), getValue(line));
		}

		return attributes;

	}

	public String getKey(String line) {
		String key;
		String arrLine[] = line.split(" ");
		key = arrLine[0];
		return key.substring(0,key.length()-1);
	}

	public double getValue(String line) {
		double value;
		String arrLine[] = line.split(" ");
		return Double.parseDouble(arrLine[1]);
	}

}