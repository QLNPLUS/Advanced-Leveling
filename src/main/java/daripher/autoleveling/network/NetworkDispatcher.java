package daripher.autoleveling.network;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.network.message.SyncLevelingData;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.NetworkDirection;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.simple.SimpleChannel;

@EventBusSubscriber(modid = AutoLevelingMod.MOD_ID)
public class NetworkDispatcher {
  public static SimpleChannel network_channel;

  @SubscribeEvent
  public static void onCommonSetupEvent(FMLCommonSetupEvent event) {
    Identifier networkChannelId = Identifier.fromNamespaceAndPath(AutoLevelingMod.MOD_ID, "channel");
    network_channel =
        NetworkRegistry.newSimpleChannel(networkChannelId, () -> "1.0", s -> true, s -> true);
    network_channel.registerMessage(
        1,
        SyncLevelingData.class,
        SyncLevelingData::encode,
        SyncLevelingData::decode,
        SyncLevelingData::receive,
        Optional.of(NetworkDirection.PLAY_TO_CLIENT));
  }
}
