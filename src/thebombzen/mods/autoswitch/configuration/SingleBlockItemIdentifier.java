package thebombzen.mods.autoswitch.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thebombzen.mods.thebombzenapi.ThebombzenAPI;
import thebombzen.mods.thebombzenapi.configuration.BooleanTester;
import thebombzen.mods.thebombzenapi.configuration.ConfigFormatException;

/**
 * Represents a block or item with the given String namespace, name, and damage values.
 * @author thebombzen
 */
public class SingleBlockItemIdentifier implements BooleanTester<SingleValueIdentifier> {

	public static final int ALL = 0;
	public static final int NAME = 1;
	public static final int CLASS = 2;
	public static final int MATERIAL = 3;
	
	/**
	 * Parse an identifier from a string.
	 */
	public static SingleBlockItemIdentifier parseSingleBlockItemIdentifier(String info) throws ConfigFormatException {
		if (info.length() == 0){
			throw new ConfigFormatException("Empty block/item identifier");
		}
		char type = info.charAt(0);
		int superNum = 0;
		if (type == '@' || type == '$'){
			info = info.substring(1);
		} else if (type == '['){
			int lastIndex = info.indexOf(']');
			if (lastIndex < 0){
				throw new ConfigFormatException("No closing ]: " + info);
			}
			String superNumS = info.substring(1, lastIndex);
			try {
				superNum = ThebombzenAPI.parseInteger(superNumS);
			} catch (NumberFormatException e){
				throw new ConfigFormatException("Invalid broadening number: " + superNumS, e);
			}
			info = info.substring(lastIndex + 1);
		}
		
		boolean all = info.startsWith("+") || info.startsWith("-");
		
		if (all){
			info = "false_prefix" + info;
		}
		
		Scanner scanner = new Scanner(info);
		scanner.useDelimiter("(?<=[^\\+-])(?=[\\+-])");
		
		if (!scanner.hasNext()){
			scanner.close();
			return new SingleBlockItemIdentifier(SingleBlockItemIdentifier.ALL);
		}
		
		String fullname = scanner.next();
		String modid;
		String name;
		
		//System.out.println(fullname);
		
		if (all){
			modid = "";
			name = "";
		} else {
			int index = fullname.indexOf(':');
			if (index < 0){
				modid = "minecraft";
				name = fullname;
			} else {
				modid = fullname.substring(0, index);
				name = fullname.substring(index + 1);
			}
		}

		List<ValueSet> valueSets = new ArrayList<ValueSet>();
		
		while (scanner.hasNext()){
			String s = scanner.next();
			ValueSet valueSet = ValueSet.parseValueSet(s);
			valueSets.add(valueSet);
		}
		
		scanner.close();
		
		ValueSet[] sets = valueSets.toArray(new ValueSet[valueSets.size()]);
		
		if (all){
			return new SingleBlockItemIdentifier(SingleBlockItemIdentifier.ALL, sets);
		}
		
		switch(type){
		case '@':
		case '[':
			return new SingleBlockItemIdentifier(SingleBlockItemIdentifier.CLASS, modid, name, superNum, sets);
		case '$':
			return new SingleBlockItemIdentifier(SingleBlockItemIdentifier.MATERIAL, modid, name, 0, sets);
		default:
			return new SingleBlockItemIdentifier(SingleBlockItemIdentifier.NAME, modid, name, 0, sets);
		}
	}
	private ResourceLocation resourceLocation;
	private ValueSet[] valueSets;

	private int type;
	private int superNum;
	
	/**
	 * Construct an identifier with the given String namespace, name, and damage value.
	 * @param namespace The String namespace such as "minecraft"
	 * @param name The String name such as "log"
	 * @param damageValue The Damage value
	 */
	public SingleBlockItemIdentifier(int type, String namespace, String name, int damageValue){
		this(type, namespace, name, 0, new ValueSet(damageValue, Integer.MAX_VALUE, false));
	}
	
	/**
	 * Construct an identifier with the given String namespace, name, and damage values.
	 * @param namespace The String namespace such as "minecraft"
	 * @param name The String name such as "log"
	 * @param damageValues The Damage values
	 */
	public SingleBlockItemIdentifier(int type, String namespace, String name, int superNum, ValueSet... damageValues){
		
		if (type < 0 || type > 3){
			throw new IllegalArgumentException();
		}
		this.superNum = superNum;
		this.type = type;
		this.resourceLocation = new ResourceLocation(namespace + ":" + name);
		if (damageValues.length == 0 || damageValues.length > 0 && damageValues[0].doesSubtract()){
			ValueSet[] temp = new ValueSet[damageValues.length + 1];
			System.arraycopy(damageValues, 0, temp, 1, damageValues.length);
			temp[0] = new ValueSet();
			damageValues = temp;
		}
		this.valueSets = damageValues;
	}
	
	/**
	 * Construct a BlockItemIdentifier that matches the damages values on any item/block.
	 * @param damageValues
	 */
	public SingleBlockItemIdentifier(int type, ValueSet... damageValues){
		this(type, "", "", 0, damageValues);
	}

	/**
	 * Gets whether this identifier refers to the given name and damage value.
	 * @param name The String name of this block/item
	 * @param damageValue The damage value of this block/item
	 */
	@Override
	public boolean contains(SingleValueIdentifier identifier){
		String modid = identifier.getModId();
		String name = identifier.getName();
		switch (type){
		case ALL:
			break;
		case NAME:
			if (!getName().equals(name) || !getModId().equals(modid)){
				return false;
			}
			break;
		case CLASS:
			if (isBlock()){
				IBlockState state = identifier.getBlockState();
				if (state == null){
					break;
				}
				Class<?> clazz = getBlock().getClass();
				for (int i = 0; i < superNum; i++){
					if (clazz.getSuperclass() != null){
						clazz = clazz.getSuperclass();
					} else {
						break;
					}
				}
				if (!clazz.isAssignableFrom(state.getBlock().getClass())){
					return false;
				}
				break;
			}
			if (isItem()){
				ItemStack stack = identifier.getItemStack();
				if (stack == null){
					break;
				}
				Class<?> clazz = getItem().getClass();
				for (int i = 0; i < superNum; i++){
					if (clazz.getSuperclass() != null){
						clazz = clazz.getSuperclass();
					} else {
						break;
					}
				}
				if (!clazz.isAssignableFrom(stack.getItem().getClass())){
					return false;
				}
			} else {
				return false;
			}
			break;
		case MATERIAL:
			if (getBlock() == null){
				break;
			}
			IBlockState blockState = identifier.getBlockState();
			if (blockState == null || getBlock() == null){
				return false;
			}
			if (!getBlock().getDefaultState().getMaterial().equals(blockState.getMaterial())){
				return false;
			}
			break;
		}
		for (int i = valueSets.length - 1; i >= 0; i--){
			ValueSet set = valueSets[i];
			SingleValueIdentifier id2 = identifier;
			if (identifier.isItem()){
				if (set.getMask() < 0){
					if (identifier.getItemStack() == null || identifier.getItemStack().getMaxDamage() < identifier.getItemStack().getItemDamage()){
						continue;
					}
					ItemStack newStack = identifier.getItemStack().copy();
					newStack.setItemDamage(newStack.getMaxDamage() - newStack.getItemDamage());
					id2 = new SingleValueIdentifier(newStack);
				}
			}
			if (set.contains(id2)){
				return !set.doesSubtract();
			}
		}
		return false;
	}

	/**
	 * Gets the block associated with this identifier.
	 * null if this is not a block.
	 */
	public Block getBlock() {
		Block block = Block.getBlockFromName(resourceLocation.toString());
		if (Block.isEqualTo(block, Blocks.AIR)){
			return null;
		} else {
			return block;
		}
	}

	/**
	 * Return the array of ValueSets
	 */
	public ValueSet[] getDamageValues() {
		return valueSets;
	}

	/**
	 * Gets the item associated with this identifier. If it's a block it
	 * will return the corresponding ItemBlock.
	 */
	public Item getItem() {
		return Item.getByNameOrId(resourceLocation.toString());
	}
	
	/**
	 * Gets the String namespace of this item/block
	 * @return
	 */
	public String getModId(){
		return resourceLocation.getResourceDomain();
	}
	
	/**
	 * Gets the String name of this item/block
	 */
	public String getName(){
		return resourceLocation.getResourcePath();
	}

	/**
	 * Determines whether this IDMetadataPair is a valid (non-null) block. Air
	 * is not a valid block.
	 * 
	 * @return Whether this is a valid Block object.
	 */
	public boolean isBlock() {
		return getBlock() != null;
	}

	/**
	 * Determines whether this IDMetadataPair is a valid (non-null) item. All
	 * valid registered blocks are also valid items, see
	 * {net.minecraft.item.ItemBlock}
	 * 
	 * @return Whether this is a valid Item object.
	 */
	public boolean isItem() {
		return getItem() != null;
	}

}
