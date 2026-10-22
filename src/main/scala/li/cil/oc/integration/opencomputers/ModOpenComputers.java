package li.cil.oc.integration.opencomputers;

import li.cil.oc.Constants;
import li.cil.oc.OpenComputers;
import li.cil.oc.Settings;
import li.cil.oc.api.Driver;
import li.cil.oc.api.IMC;
import li.cil.oc.api.Items;
import li.cil.oc.api.Manual;
import li.cil.oc.api.Nanomachines;
import li.cil.oc.api.detail.ItemInfo;
import li.cil.oc.api.driver.item.Chargeable;
import li.cil.oc.api.internal.Wrench;
import li.cil.oc.api.manual.PathProvider;
import li.cil.oc.api.prefab.ItemStackTabIconRenderer;
import li.cil.oc.api.prefab.ResourceContentProvider;
import li.cil.oc.api.prefab.SpriteTabIconRenderer;
import li.cil.oc.client.Textures;
import li.cil.oc.client.renderer.markdown.segment.render.BlockImageProvider$;
import li.cil.oc.client.renderer.markdown.segment.render.ItemImageProvider$;
import li.cil.oc.client.renderer.markdown.segment.render.TagImageProvider$;
import li.cil.oc.client.renderer.markdown.segment.render.TextureImageProvider$;
import li.cil.oc.common.EventHandler$;
import li.cil.oc.common.Loot$;
import li.cil.oc.common.SaveHandler$;
import li.cil.oc.common.block.SimpleBlock;
import li.cil.oc.common.event.*;
import li.cil.oc.common.item.Analyzer$;
import li.cil.oc.common.item.Tablet$;
import li.cil.oc.common.nanomachines.provider.*;
import li.cil.oc.common.openprinter.OpenPrinter;
import li.cil.oc.common.template.*;
import li.cil.oc.integration.ModProxy;
import li.cil.oc.integration.Mods;
import li.cil.oc.integration.SableTickBridge$;
import li.cil.oc.integration.util.BundledRedstone;
import li.cil.oc.server.network.Waypoints;
import li.cil.oc.server.network.WirelessNetwork;
import li.cil.oc.util.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Set;
import java.util.Objects;

public final class ModOpenComputers implements ModProxy {
    public static final ModOpenComputers INSTANCE = new ModOpenComputers();
    public static boolean hasRedstoneCardT2;

    private ModOpenComputers() {
    }

    @Override
    public li.cil.oc.integration.Mod getMod() {
        return Mods.OpenComputers();
    }

    @Override
    public void initialize() {
        DroneTemplate.register();
        MicrocontrollerTemplate.register();
        NavigationUpgradeTemplate.register();
        RobotTemplate.register();
        ServerTemplate.register();
        TabletTemplate.register();
        TemplateBlacklist.register();

        IMC.registerWrenchTool("li.cil.oc.integration.opencomputers.ModOpenComputers.useWrench");
        IMC.registerWrenchToolCheck("li.cil.oc.integration.opencomputers.ModOpenComputers.isWrench");
        IMC.registerItemCharge("OpenComputers",
            "li.cil.oc.integration.opencomputers.ModOpenComputers.canCharge",
            "li.cil.oc.integration.opencomputers.ModOpenComputers.charge");
        IMC.registerInkProvider("li.cil.oc.integration.opencomputers.ModOpenComputers.inkCartridgeInkProvider");
        IMC.registerInkProvider("li.cil.oc.integration.opencomputers.ModOpenComputers.dyeInkProvider");

        IMC.registerProgramDiskLabel("build", "builder", "Lua 5.2", "Lua 5.3", "LuaJ");
        IMC.registerProgramDiskLabel("dig", "dig", "Lua 5.2", "Lua 5.3", "LuaJ");
        IMC.registerProgramDiskLabel("base64", "data", "Lua 5.2", "Lua 5.3", "LuaJ");
        IMC.registerProgramDiskLabel("deflate", "data", "Lua 5.2", "Lua 5.3", "LuaJ");
        IMC.registerProgramDiskLabel("gpg", "data", "Lua 5.2", "Lua 5.3", "LuaJ");
        IMC.registerProgramDiskLabel("inflate", "data", "Lua 5.2", "Lua 5.3", "LuaJ");
        IMC.registerProgramDiskLabel("md5sum", "data", "Lua 5.2", "Lua 5.3", "LuaJ");
        IMC.registerProgramDiskLabel("sha256sum", "data", "Lua 5.2", "Lua 5.3", "LuaJ");
        IMC.registerProgramDiskLabel("refuel", "generator", "Lua 5.2", "Lua 5.3", "LuaJ");
        IMC.registerProgramDiskLabel("irc", "irc", "Lua 5.2", "Lua 5.3", "LuaJ");
        IMC.registerProgramDiskLabel("maze", "maze", "Lua 5.2", "Lua 5.3", "LuaJ");
        IMC.registerProgramDiskLabel("arp", "network", "Lua 5.2", "Lua 5.3", "LuaJ");
        IMC.registerProgramDiskLabel("ifconfig", "network", "Lua 5.2", "Lua 5.3", "LuaJ");
        IMC.registerProgramDiskLabel("ping", "network", "Lua 5.2", "Lua 5.3", "LuaJ");
        IMC.registerProgramDiskLabel("route", "network", "Lua 5.2", "Lua 5.3", "LuaJ");
        IMC.registerProgramDiskLabel("opl-flash", "openloader", "Lua 5.2", "Lua 5.3", "LuaJ");
        IMC.registerProgramDiskLabel("oppm", "oppm", "Lua 5.2", "Lua 5.3", "LuaJ");

        OpenComputers.proxy().modBus().addListener(EventHandler$.MODULE$::onRegisterCapabilities);
        OpenComputers.proxy().modBus().addListener(ChunkloaderUpgradeHandler.INSTANCE::onRegisterTicketControllers);
        OpenComputers.proxy().modBus().addListener(SableTickBridge$.MODULE$::onRegisterTicketControllers);

        NeoForge.EVENT_BUS.register(EventHandler$.MODULE$);
        NeoForge.EVENT_BUS.register(NanomachinesHandler.Common$.MODULE$);
        NeoForge.EVENT_BUS.register(Tablet$.MODULE$);
        NeoForge.EVENT_BUS.register(Analyzer$.MODULE$);
        NeoForge.EVENT_BUS.register(AngelUpgradeHandler.INSTANCE);
        NeoForge.EVENT_BUS.register(ChunkloaderUpgradeHandler.INSTANCE);
        NeoForge.EVENT_BUS.register(ExperienceUpgradeHandler.INSTANCE);
        NeoForge.EVENT_BUS.register(FileSystemAccessHandler.INSTANCE);
        NeoForge.EVENT_BUS.register(HoverBootsHandler.INSTANCE);
        NeoForge.EVENT_BUS.register(Loot$.MODULE$);
        NeoForge.EVENT_BUS.register(NetworkActivityHandler.INSTANCE);
        NeoForge.EVENT_BUS.register(RobotCommonHandler.INSTANCE);
        NeoForge.EVENT_BUS.register(SaveHandler$.MODULE$);
        NeoForge.EVENT_BUS.register(Waypoints.INSTANCE);
        NeoForge.EVENT_BUS.register(WirelessNetwork.INSTANCE);
        NeoForge.EVENT_BUS.register(WirelessNetworkCardHandler.INSTANCE);
        NeoForge.EVENT_BUS.register(li.cil.oc.client.ComponentTracker$.MODULE$);
        NeoForge.EVENT_BUS.register(li.cil.oc.server.ComponentTracker$.MODULE$);

        Driver.add(ConverterNanomachines.INSTANCE);
        Driver.add(ConverterLinkedCard.INSTANCE);
        Driver.add(DriverAPU$.MODULE$);
        Driver.add(DriverAudioCard$.MODULE$);
        Driver.add(DriverComponentBus$.MODULE$);
        Driver.add(DriverCPU$.MODULE$);
        Driver.add(DriverDataCard$.MODULE$);
        Driver.add(DriverDebugCard$.MODULE$);
        Driver.add(DriverEEPROM$.MODULE$);
        Driver.add(DriverFileSystem$.MODULE$);
        Driver.add(DriverGraphicsCard$.MODULE$);
        Driver.add(DriverQuadGraphicsCard$.MODULE$);
        Driver.add(DriverInternetCard$.MODULE$);
        Driver.add(DriverLinkedCard$.MODULE$);
        Driver.add(DriverNavigationCard$.MODULE$);
        Driver.add(DriverLootDisk$.MODULE$);
        Driver.add(DriverMemory$.MODULE$);
        Driver.add(DriverCreativeMemory$.MODULE$);
        Driver.add(DriverNetworkCard$.MODULE$);
        Driver.add(DriverKeyboard$.MODULE$);
        Driver.add(DriverRedstoneCard$.MODULE$);
        Driver.add(DriverTablet$.MODULE$);
        Driver.add(DriverWirelessNetworkCard$.MODULE$);
        Driver.add(DriverContainerCard$.MODULE$);
        Driver.add(DriverContainerFloppy$.MODULE$);
        Driver.add(DriverContainerUpgrade$.MODULE$);
        Driver.add(DriverGeolyzer$.MODULE$);
        Driver.add(DriverMotionSensor$.MODULE$);
        Driver.add(DriverScreen$.MODULE$);
        Driver.add(DriverTransposer$.MODULE$);
        Driver.add(DriverCapacitorMountable$.MODULE$);
        Driver.add(DriverDiskDriveMountable$.MODULE$);
        Driver.add(DriverServer$.MODULE$);
        Driver.add(DriverTerminalServer$.MODULE$);
        Driver.add(DriverUpgradeAngel$.MODULE$);
        Driver.add(DriverUpgradeBarcodeReader$.MODULE$);
        Driver.add(DriverUpgradeBattery$.MODULE$);
        Driver.add(DriverUpgradeChunkloader$.MODULE$);
        Driver.add(DriverUpgradeCrafting$.MODULE$);
        Driver.add(DriverUpgradeDatabase$.MODULE$);
        Driver.add(DriverUpgradeExperience$.MODULE$);
        Driver.add(DriverUpgradeGenerator$.MODULE$);
        Driver.add(DriverUpgradeHover$.MODULE$);
        Driver.add(DriverUpgradeInventory$.MODULE$);
        Driver.add(DriverUpgradeInventoryController$.MODULE$);
        Driver.add(DriverUpgradeLeash$.MODULE$);
        Driver.add(DriverUpgradeNavigation$.MODULE$);
        Driver.add(DriverUpgradePiston$.MODULE$);
        Driver.add(DriverUpgradeSign$.MODULE$);
        Driver.add(DriverUpgradeSolarGenerator$.MODULE$);
        Driver.add(DriverUpgradeStickyPiston$.MODULE$);
        Driver.add(DriverUpgradeTank$.MODULE$);
        Driver.add(DriverUpgradeTankController$.MODULE$);
        Driver.add(DriverUpgradeTractorBeam$.MODULE$);
        Driver.add(DriverUpgradeTrading$.MODULE$);
        Driver.add(DriverUpgradeMF$.MODULE$);
        Driver.add(DriverAPU.Provider$.MODULE$);
        Driver.add(DriverAudioCard.Provider$.MODULE$);
        Driver.add(DriverDataCard.Provider$.MODULE$);
        Driver.add(DriverDebugCard.Provider$.MODULE$);
        Driver.add(DriverEEPROM.Provider$.MODULE$);
        Driver.add(DriverGraphicsCard.Provider$.MODULE$);
        Driver.add(DriverQuadGraphicsCard.Provider$.MODULE$);
        Driver.add(DriverInternetCard.Provider$.MODULE$);
        Driver.add(DriverLinkedCard.Provider$.MODULE$);
        Driver.add(DriverNavigationCard.Provider$.MODULE$);
        Driver.add(DriverNetworkCard.Provider$.MODULE$);
        Driver.add(DriverRedstoneCard.Provider$.MODULE$);
        Driver.add(DriverWirelessNetworkCard.Provider$.MODULE$);
        Driver.add(DriverGeolyzer.Provider$.MODULE$);
        Driver.add(DriverMotionSensor.Provider$.MODULE$);
        Driver.add(DriverScreen.Provider$.MODULE$);
        Driver.add(DriverTransposer.Provider$.MODULE$);
        Driver.add(DriverUpgradeChunkloader.Provider$.MODULE$);
        Driver.add(DriverUpgradeCrafting.Provider$.MODULE$);
        Driver.add(DriverUpgradeDatabase.Provider$.MODULE$);
        Driver.add(DriverUpgradeExperience.Provider$.MODULE$);
        Driver.add(DriverUpgradeGenerator.Provider$.MODULE$);
        Driver.add(DriverUpgradeInventoryController.Provider$.MODULE$);
        Driver.add(DriverUpgradeLeash.Provider$.MODULE$);
        Driver.add(DriverUpgradeNavigation.Provider$.MODULE$);
        Driver.add(DriverUpgradePiston.Provider$.MODULE$);
        Driver.add(DriverUpgradeSign.Provider$.MODULE$);
        Driver.add(DriverUpgradeStickyPiston.Provider$.MODULE$);
        Driver.add(DriverUpgradeTankController.Provider$.MODULE$);
        Driver.add(DriverUpgradeTractorBeam.Provider$.MODULE$);
        Driver.add(DriverUpgradeMF.Provider$.MODULE$);
        Driver.add(EnvironmentProviderBlocks$.MODULE$);
        Driver.add(InventoryProviderDatabase$.MODULE$);
        Driver.add(InventoryProviderServer$.MODULE$);

        blacklistHost(li.cil.oc.api.internal.Adapter.class,
            Constants.BlockName$.MODULE$.Geolyzer(),
      Constants.BlockName$.MODULE$.MotionSensor(),
      Constants.BlockName$.MODULE$.Keyboard(),
      Constants.BlockName$.MODULE$.ScreenTier1(),
      Constants.BlockName$.MODULE$.Transposer(),
      Constants.BlockName$.MODULE$.CarpetedCapacitor(),
      Constants.ItemName$.MODULE$.Analyzer(),
      Constants.ItemName$.MODULE$.AngelUpgrade(),
      Constants.ItemName$.MODULE$.BatteryUpgradeTier1(),
      Constants.ItemName$.MODULE$.BatteryUpgradeTier2(),
      Constants.ItemName$.MODULE$.BatteryUpgradeTier3(),
      Constants.ItemName$.MODULE$.ChunkloaderUpgrade(),
      Constants.ItemName$.MODULE$.CraftingUpgrade(),
      Constants.ItemName$.MODULE$.ExperienceUpgrade(),
      Constants.ItemName$.MODULE$.GeneratorUpgrade(),
      Constants.ItemName$.MODULE$.HoverUpgradeTier1(),
      Constants.ItemName$.MODULE$.HoverUpgradeTier2(),
      Constants.ItemName$.MODULE$.InventoryUpgrade(),
      Constants.ItemName$.MODULE$.NavigationUpgrade(),
      Constants.ItemName$.MODULE$.PistonUpgrade(),
      Constants.ItemName$.MODULE$.StickyPistonUpgrade(),
      Constants.ItemName$.MODULE$.SolarGeneratorUpgrade(),
      Constants.ItemName$.MODULE$.TankUpgrade(),
      Constants.ItemName$.MODULE$.TractorBeamUpgrade(),
      Constants.ItemName$.MODULE$.LeashUpgrade(),
      Constants.ItemName$.MODULE$.TradingUpgrade());
        blacklistHost(li.cil.oc.api.internal.Drone.class,
            Constants.BlockName$.MODULE$.Keyboard(),
      Constants.BlockName$.MODULE$.ScreenTier1(),
      Constants.BlockName$.MODULE$.Transposer(),
      Constants.BlockName$.MODULE$.CarpetedCapacitor(),
      Constants.ItemName$.MODULE$.Analyzer(),
      Constants.ItemName$.MODULE$.APUTier1(),
      Constants.ItemName$.MODULE$.APUTier2(),
      Constants.ItemName$.MODULE$.GraphicsCardTier1(),
      Constants.ItemName$.MODULE$.GraphicsCardTier2(),
      Constants.ItemName$.MODULE$.GraphicsCardTier3(),
      Constants.ItemName$.MODULE$.NetworkCard(),
      Constants.ItemName$.MODULE$.RedstoneCardTier1(),
      Constants.ItemName$.MODULE$.CraftingUpgrade(),
      Constants.ItemName$.MODULE$.HoverUpgradeTier1(),
      Constants.ItemName$.MODULE$.HoverUpgradeTier2());
        blacklistHost(li.cil.oc.api.internal.Microcontroller.class,
            Constants.BlockName$.MODULE$.Keyboard(),
      Constants.BlockName$.MODULE$.ScreenTier1(),
      Constants.BlockName$.MODULE$.CarpetedCapacitor(),
      Constants.ItemName$.MODULE$.Analyzer(),
      Constants.ItemName$.MODULE$.APUTier1(),
      Constants.ItemName$.MODULE$.APUTier2(),
      Constants.ItemName$.MODULE$.GraphicsCardTier1(),
      Constants.ItemName$.MODULE$.GraphicsCardTier2(),
      Constants.ItemName$.MODULE$.GraphicsCardTier3(),
      Constants.ItemName$.MODULE$.AngelUpgrade(),
      Constants.ItemName$.MODULE$.CraftingUpgrade(),
      Constants.ItemName$.MODULE$.DatabaseUpgradeTier1(),
      Constants.ItemName$.MODULE$.DatabaseUpgradeTier2(),
      Constants.ItemName$.MODULE$.DatabaseUpgradeTier3(),
      Constants.ItemName$.MODULE$.ExperienceUpgrade(),
      Constants.ItemName$.MODULE$.GeneratorUpgrade(),
      Constants.ItemName$.MODULE$.HoverUpgradeTier1(),
      Constants.ItemName$.MODULE$.HoverUpgradeTier2(),
      Constants.ItemName$.MODULE$.InventoryUpgrade(),
      Constants.ItemName$.MODULE$.InventoryControllerUpgrade(),
      Constants.ItemName$.MODULE$.NavigationUpgrade(),
      Constants.ItemName$.MODULE$.TankUpgrade(),
      Constants.ItemName$.MODULE$.TankControllerUpgrade(),
      Constants.ItemName$.MODULE$.TractorBeamUpgrade(),
      Constants.ItemName$.MODULE$.LeashUpgrade(),
      Constants.ItemName$.MODULE$.TradingUpgrade());
        blacklistHost(li.cil.oc.api.internal.Robot.class,
            Constants.BlockName$.MODULE$.Transposer(),
      Constants.BlockName$.MODULE$.CarpetedCapacitor(),
      Constants.ItemName$.MODULE$.Analyzer(),
      Constants.ItemName$.MODULE$.LeashUpgrade());
        blacklistHost(li.cil.oc.api.internal.Tablet.class,
            Constants.BlockName$.MODULE$.ScreenTier1(),
      Constants.BlockName$.MODULE$.Transposer(),
      Constants.BlockName$.MODULE$.CarpetedCapacitor(),
      Constants.ItemName$.MODULE$.NetworkCard(),
      Constants.ItemName$.MODULE$.RedstoneCardTier1(),
      Constants.ItemName$.MODULE$.AngelUpgrade(),
      Constants.ItemName$.MODULE$.ChunkloaderUpgrade(),
      Constants.ItemName$.MODULE$.CraftingUpgrade(),
      Constants.ItemName$.MODULE$.DatabaseUpgradeTier1(),
      Constants.ItemName$.MODULE$.DatabaseUpgradeTier2(),
      Constants.ItemName$.MODULE$.DatabaseUpgradeTier3(),
      Constants.ItemName$.MODULE$.ExperienceUpgrade(),
      Constants.ItemName$.MODULE$.GeneratorUpgrade(),
      Constants.ItemName$.MODULE$.HoverUpgradeTier1(),
      Constants.ItemName$.MODULE$.HoverUpgradeTier2(),
      Constants.ItemName$.MODULE$.InventoryUpgrade(),
      Constants.ItemName$.MODULE$.InventoryControllerUpgrade(),
      Constants.ItemName$.MODULE$.TankUpgrade(),
      Constants.ItemName$.MODULE$.TankControllerUpgrade(),
      Constants.ItemName$.MODULE$.LeashUpgrade(),
      Constants.ItemName$.MODULE$.TradingUpgrade());

        if (BundledRedstone.isAvailable()) {
            OpenComputers.log().info("Found extended redstone mods, enabling tier two redstone card.");
            hasRedstoneCardT2 = true;
        }

        Nanomachines.addProvider(DisintegrationProvider$.MODULE$);
        Nanomachines.addProvider(HungryProvider$.MODULE$);
        Nanomachines.addProvider(ParticleProvider$.MODULE$);
        Nanomachines.addProvider(PotionProvider$.MODULE$);
        Nanomachines.addProvider(MagnetProvider$.MODULE$);

        if (FMLEnvironment.dist.isClient()) initializeClient();
    }

    @OnlyIn(Dist.CLIENT)
    private void initializeClient() {
        Manual.addProvider(DefinitionPathProvider.INSTANCE);
        Manual.addProvider(new ResourceContentProvider(Settings.resourceDomain(), "doc/"));
        Manual.addProvider("", TextureImageProvider$.MODULE$);
        Manual.addProvider("item", ItemImageProvider$.MODULE$);
        Manual.addProvider("block", BlockImageProvider$.MODULE$);
        Manual.addProvider("oredict", TagImageProvider$.MODULE$);
        OpenPrinter.registerManual();

        Manual.addTab(new SpriteTabIconRenderer(Textures.GUISprites$.MODULE$.ManualHome()),
            "oc:gui.Manual.Home", "%LANGUAGE%/index.md");
        Manual.addTab(new ItemStackTabIconRenderer(Items.get("case1").createItemStack(1)),
            "oc:gui.Manual.Blocks", "%LANGUAGE%/block/index.md");
        Manual.addTab(new ItemStackTabIconRenderer(Items.get("cpu1").createItemStack(1)),
            "oc:gui.Manual.Items", "%LANGUAGE%/item/index.md");
    }

    public static boolean useWrench(Player player, BlockPos pos, boolean changeDurability) {
        return player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof Wrench wrench &&
            wrench.useWrenchOnBlock(player, player.level(), pos, !changeDurability);
    }

    public static boolean isWrench(ItemStack stack) {
        return stack.getItem() instanceof Wrench;
    }

    public static boolean canCharge(ItemStack stack) {
        return stack.getItem() instanceof Chargeable chargeable && chargeable.canCharge(stack);
    }

    public static double charge(ItemStack stack, double amount, boolean simulate) {
        return stack.getItem() instanceof Chargeable chargeable ? chargeable.charge(stack, amount, simulate) : amount;
    }

    public static int inkCartridgeInkProvider(ItemStack stack) {
        return Objects.equals(Items.get(stack), Items.get(Constants.ItemName$.MODULE$.InkCartridge()))
            ? Settings.get().printInkValue() : 0;
    }

    public static int dyeInkProvider(ItemStack stack) {
        return Color.isDye(stack) ? Settings.get().printInkValue() / 10 : 0;
    }

    private static void blacklistHost(Class<?> host, String... itemNames) {
        for (String itemName : itemNames) {
            try {
                IMC.blacklistHost(itemName, host, Items.get(itemName).createItemStack(1));
            } catch (Throwable exception) {
                OpenComputers.log().warn("Error blacklisting '{}' for '{}'.", itemName, host.getSimpleName(), exception);
            }
        }
    }

    public static final class DefinitionPathProvider implements PathProvider {
        public static final DefinitionPathProvider INSTANCE = new DefinitionPathProvider();
        private static final Set<String> BLACKLIST = Set.of(
            Constants.ItemName$.MODULE$.Debugger(),
            Constants.ItemName$.MODULE$.DiamondChip(),
            Constants.BlockName$.MODULE$.Endstone());

        private DefinitionPathProvider() {
        }

        @Override
        public String pathFor(ItemStack stack) {
            return checkBlacklisted(Items.get(stack));
        }

        @Override
        public String pathFor(Level level, BlockPos pos) {
            return level.getBlockState(pos).getBlock() instanceof SimpleBlock block
                ? checkBlacklisted(Items.get(new ItemStack(block))) : null;
        }

        private String checkBlacklisted(ItemInfo info) {
            if (info == null || BLACKLIST.contains(info.name())) return null;
            return info.item() instanceof BlockItem
                ? "%LANGUAGE%/block/" + info.name() + ".md"
                : "%LANGUAGE%/item/" + info.name() + ".md";
        }
    }
}
