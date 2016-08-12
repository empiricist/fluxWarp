package com.empiricist.teleflux.item;

import com.empiricist.teleflux.utility.ChatHelper;
import com.empiricist.teleflux.utility.LogHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemDebugger extends ItemBase{

    public ItemDebugger(){
        super();
        name = "debugger";
        this.setUnlocalizedName(name);
        //this.setCreativeTab(CreativeTabTestProject.TEST_PROJECT_TAB);
    }

    //give data of block right clicked on
    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ){
        if( !world.isRemote ){
            ChatHelper.sendText(player, "-----Block-----");
            Block block = world.getBlockState(pos).getBlock();
            ChatHelper.sendText(player, pos.toString().substring(8) + "; Name: " + block.getLocalizedName() + ", ID: " + block.getUnlocalizedName() + ", Meta: " + world.getBlockState(pos));
            ChatHelper.sendText(player, "Hardness: " + block.getBlockHardness(world.getBlockState(pos),world,pos) + ", Resistance: " + block.getExplosionResistance(player)*5.0f + ", Mining Level: " + block.getHarvestLevel(world.getBlockState(pos)));
            TileEntity te = world.getTileEntity(pos);
            if( te != null ){
                NBTTagCompound tag = new NBTTagCompound();
                te.writeToNBT(tag);
                ChatHelper.sendText(player, "NBTData:" + tag.toString());

                ChatHelper.sendText(player, "---------------");
                for(TileEntity tile : world.loadedTileEntityList){
                    if(tile.getPos().equals(pos)){
                        ChatHelper.sendText(player, "*  There is a " + tile.getBlockType().getLocalizedName() + " here called " + tile);
                        tile.writeToNBT(tag);
                        ChatHelper.sendText(player, "-    NBTData:" + tag.toString());
                    }
                }
            }
        }
        return EnumActionResult.SUCCESS;
    }

    //If right clicked on no block, give data of item in slot 1
    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand){
        if( !world.isRemote ){
            ItemStack slot1 = player.inventory.getStackInSlot(0);
            if (slot1 != null){
                ChatHelper.sendText(player, "-----Item-----");
                ChatHelper.sendText(player, "Item in slot 1 has Display Name: " + slot1.getDisplayName() + ", Unlocalized Name: " + slot1.getItem().getUnlocalizedName());
                if( slot1.hasTagCompound() ){
                    ChatHelper.sendText(player, "NBT Data is :"  + slot1.getTagCompound().toString());
                }else{
                    ChatHelper.sendText(player, "No item found in slot 1 to analyze");
                }
            }
        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    //give data of entity left clicked on
    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity){
        if( !entity.worldObj.isRemote ){
            ChatHelper.sendText(player, "-----Entity-----");
            ChatHelper.sendText(player, "Name: " + entity.getClass()  + ", ID: " + entity.getEntityId() + ", Data: " + entity.toString());
        }
        return true;//do no damage
    }
}
