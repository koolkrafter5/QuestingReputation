package koolkrafter5.questrep;

import net.minecraft.nbt.NBTTagCompound;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.api2.registry.IRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import koolkrafter5.questrep.command.AddRep;
import koolkrafter5.questrep.command.SeeJson;
import koolkrafter5.questrep.command.SeePartyID;
import koolkrafter5.questrep.reputation.FactionData;
import koolkrafter5.questrep.tasks.factory.FactoryTaskDeaths;
import koolkrafter5.questrep.tasks.factory.FactoryTaskReputation;

@Mod(
    modid = QuestingReputation.MODID,
    version = Tags.VERSION,
    name = "Questing Reputation",
    acceptedMinecraftVersions = "[1.7.10]")
public class QuestingReputation {

    public static Logger log = null;
    public static final String MODID = "questrep";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @SidedProxy(clientSide = "koolkrafter5.questrep.ClientProxy", serverSide = "koolkrafter5.questrep.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        log = event.getModLog();
        proxy.preInit(event);
    }

    @Mod.EventHandler
    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        proxy.init(event);

        IRegistry<IFactoryData<ITask, NBTTagCompound>, ITask> taskReg = QuestingAPI.getAPI(ApiReference.TASK_REG);
        taskReg.register(FactoryTaskDeaths.INSTANCE);
        taskReg.register(FactoryTaskReputation.INSTANCE);

        FactionData.loadFactions();

    }

    @Mod.EventHandler
    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    public static void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new SeeJson());
        event.registerServerCommand(new SeePartyID());
        event.registerServerCommand(new AddRep());
    }

    @Mod.EventHandler
    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }
}
