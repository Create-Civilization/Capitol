package com.createcivilization.capitol.common.data;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

// a team member as shown in the book: uuid + display name
public record CapitolMember(UUID uuid, String name) {

	public static final StreamCodec<ByteBuf, CapitolMember> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8,
		m -> m.uuid().toString(),
		ByteBufCodecs.STRING_UTF8,
		CapitolMember::name,
		(uuid, name) -> new CapitolMember(UUID.fromString(uuid), name)
	);
}