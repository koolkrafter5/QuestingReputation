package koolkrafter5.questrep;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.api2.registry.IRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import koolkrafter5.questrep.handlers.QREventHandlers;
import koolkrafter5.questrep.network.PacketHandler;
import koolkrafter5.questrep.reputation.FactionData;
import koolkrafter5.questrep.rewards.factory.FactoryRewardReputation;
import koolkrafter5.questrep.tasks.factory.FactoryTaskDeaths;
import koolkrafter5.questrep.tasks.factory.FactoryTaskReputation;

@Mod(
    modid = QuestingReputation.MODID,
    version = Tags.VERSION,
    name = "Questing Reputation",
    acceptedMinecraftVersions = "[1.7.10]")
public class QuestingReputation {

    public static final String MODID = "questrep";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @SidedProxy(clientSide = "koolkrafter5.questrep.ClientProxy", serverSide = "koolkrafter5.questrep.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
        PacketHandler.init();
    }

    @Mod.EventHandler
    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        proxy.init(event);

        IRegistry<IFactoryData<ITask, NBTTagCompound>, ITask> taskReg = QuestingAPI.getAPI(ApiReference.TASK_REG);
        taskReg.register(FactoryTaskDeaths.INSTANCE);
        taskReg.register(FactoryTaskReputation.INSTANCE);

        IRegistry<IFactoryData<IReward, NBTTagCompound>, IReward> rewardReg = QuestingAPI
            .getAPI(ApiReference.REWARD_REG);
        rewardReg.register(FactoryRewardReputation.INSTANCE);

        FactionData.loadFactions();

        QREventHandlers events = new QREventHandlers();
        MinecraftForge.EVENT_BUS.register(events);
        FMLCommonHandler.instance()
            .bus()
            .register(events);
    }

    @Mod.EventHandler
    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    public static void serverLoad(FMLServerStartingEvent event) {}

    @Mod.EventHandler
    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }
}
