package com.largesign;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class LargeSignBlockEntity extends BlockEntity {
	
	public volatile LargeSignCharacter character = LargeSignCharacter.SPACE;
    
	public LargeSignBlockEntity(BlockPos pos, BlockState state) {
        super(LargeSign.LARGE_SIGN_BLOCK_ENTITY, pos, state);
    }
	
	public static void syncUpdateToClient(LargeSignBlockEntity blockEntity, BlockPos pos, ServerPlayerEntity player) {
		PacketByteBuf sendBuf = PacketByteBufs.create();
		sendBuf.writeBlockPos(pos);
		sendBuf.writeEnumConstant(blockEntity.character);
		ServerPlayNetworking.send(player, LargeSignBlock.LARGE_SIGN_REFRESH_MODEL_PACKET_ID, sendBuf);
	}
	 
    @Override
    public void writeNbt(NbtCompound nbt) {
        nbt.putInt("character", character.ordinal());
 
        super.writeNbt(nbt);
    }
 
    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
 
        character = LargeSignCharacter.values()[nbt.getInt("character")];
    }
    
    @Override
    public Object getRenderData() {
		return this;
	}
    
    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
      return BlockEntityUpdateS2CPacket.create(this);
    }
   
    @Override
    public NbtCompound toInitialChunkDataNbt() {
      return createNbt();
    }
}