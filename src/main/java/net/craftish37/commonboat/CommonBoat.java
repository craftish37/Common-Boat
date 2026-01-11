package net.craftish37.commonboat;

import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.hotkeys.IKeybindManager;
import fi.dy.masa.malilib.hotkeys.IKeybindProvider;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.malilib.util.data.ModInfo;
import net.minecraft.client.MinecraftClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.text.Text;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import java.net.InetAddress;
import java.util.*;
import java.util.stream.StreamSupport;

public class CommonBoat implements ClientModInitializer {
    private static final Set<String> flaggedNames = new HashSet<>();
    private static final Set<UUID> WHITELISTED_UUIDS = Set.of(
            UUID.fromString("f919bd3e-5bc6-44fc-9372-34ccb15542e8"),
            UUID.fromString("43e007dc-faf7-4dde-87e1-8ed29dd40953"),
            UUID.fromString("4d19d92e-be72-469e-925d-8d9f416a7872"),
            UUID.fromString("28caf665-52d0-4782-839a-bc16a135c0cc"),
            UUID.fromString("e01adfb2-5254-4383-ae71-bffeb988b1b7"),
            UUID.fromString("b471e41b-77a6-4b8e-b063-79577e9f3372"),
            UUID.fromString("60806253-def5-4431-a5ad-a1018d7b6852"),
            UUID.fromString("e18341ad-f178-4567-9ab5-732c8774d1f8"),
            UUID.fromString("3f4dd260-f551-45a0-86c7-0b5d52823527"),
            UUID.fromString("1514ea38-8016-4db2-b7fc-7a3d93d233a6"),
            UUID.fromString("48e021d0-04b9-4014-ac4b-c737260aa2d9"),
            UUID.fromString("d76cfbff-09e7-49ad-95aa-744a8768716b"),
            UUID.fromString("2e9aa9bd-6726-4c6f-9e60-4402586e96e5"),
            UUID.fromString("6333a9c5-e831-4f0e-af13-51c8c569a612"),
            UUID.fromString("13e461a5-22bd-4217-99a1-9bbad9818dc6"),
            UUID.fromString("ab7b2efe-181d-45b0-a84f-5817b5fbd50a"),
            UUID.fromString("c510a4a5-cd44-44c3-b07f-53a4337241f1"),
            UUID.fromString("c48696eb-e870-4875-89b9-a4fcf46a65e0"),
            UUID.fromString("b86a7f60-aa09-43c4-8066-48a88eb95456")
    );
    private static final Set<String> RESTRICTED_SERVERS = Set.of("193.169.231.158");
    private static final int DISCONNECT_TICK_THRESHOLD = 100;
    private static int airTicks = 0;
    private static String cachedServerAddress = "";
    private static String cachedResolvedIp = "";
    private static final Map<ConfigHotkey, Boolean> keyStateMap = new HashMap<>();
    private static boolean commonboat$initialized = false;
    private static void performToggle(MinecraftClient client, CommonBoatConfig cfg, String configKey, boolean newState) {
        cfg.save();
        if (client.player != null) {
            String statusKey = newState ? "text.commonboat.status.enabled" : "text.commonboat.status.disabled";
            Text configName = Text.translatable(configKey);
            Text message = configName.copy().append(": ").append(Text.translatable(statusKey));
            client.player.sendMessage(message, true);
        }
    }
    private static String stripPort(String address) {
        if (address == null) return "";
        if (address.contains(":")) return address.split(":")[0];
        return address;
    }
    private static String resolveToIp(String address) {
        try {
            String host = stripPort(address);
            return InetAddress.getByName(host).getHostAddress();
        } catch (Exception e) {
            return stripPort(address);
        }
    }
    private static boolean isKeyJustPressed(ConfigHotkey hotkey) {
        boolean currentlyPressed = hotkey.getKeybind().isPressed();
        boolean wasPressed = keyStateMap.getOrDefault(hotkey, false);
        keyStateMap.put(hotkey, currentlyPressed);
        return currentlyPressed && !wasPressed;
    }
    public static class KeybindProvider implements IKeybindProvider {
        public void addKeysToMap(IKeybindManager manager) {
            for (ConfigHotkey hotkey : CommonBoatMalilibConfig.HOTKEYS) {
                manager.addKeybindToMap(hotkey.getKeybind());
            }
        }
        public void addHotkeys(IKeybindManager manager) { addKeysToMap(manager); }
    }
    public static class InitHandler implements IInitializationHandler {
        @Override
        public void registerModHandlers() {
            ConfigManager.getInstance().registerConfigHandler("commonboat", CommonBoatMalilibConfig.getInstance());
            InputEventHandler.getKeybindManager().registerKeybindProvider(new KeybindProvider());
        }
        public void registerLateHandlers() {
            Registry.CONFIG_SCREEN.registerConfigScreenFactory(
                    new ModInfo("commonboat", StringUtils.translate("config.name.commonboat"), () ->
                            new CommonBoatModMenuIntegration.CommonBoatConfigScreen(MinecraftClient.getInstance().currentScreen)
                    )
            );
            CommonBoatMalilibConfig.getInstance().load();
        }
    }
    @Override
    public void onInitializeClient() {
        new InitHandler().registerModHandlers();
        Sounds.registerSounds();
        EasterEggFishHighlighter.startUpdater();
        WorldRenderEvents.END_MAIN.register(context -> EasterEggFishHighlighter.onWorldRender(context.matrices()));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!commonboat$initialized) {
                commonboat$initialized = true;
                new InitHandler().registerLateHandlers();
            }
            CommonBoatConfig cfg = ConfigAccess.get();
            handleRestrictedServerLogic(client);
            handleNameMatchLogic(client, cfg);
            handleKeybinds(client, cfg);
        });
    }
    private static void handleRestrictedServerLogic(MinecraftClient client) {
        if (client.player != null && client.getNetworkHandler() != null && client.getCurrentServerEntry() != null) {
            String currentAddress = client.getCurrentServerEntry().address;
            UUID playerUuid = client.player.getUuid();
            if (!currentAddress.equals(cachedServerAddress)) {
                cachedServerAddress = currentAddress;
                cachedResolvedIp = resolveToIp(currentAddress);
            }
            boolean isRestrictedServer = RESTRICTED_SERVERS.contains(cachedResolvedIp) ||
                    RESTRICTED_SERVERS.contains(currentAddress);
            if (isRestrictedServer) {
                if (!WHITELISTED_UUIDS.contains(playerUuid)) {
                    if (client.player.getVehicle() instanceof AbstractBoatEntity boat) {
                        boolean inWater = boat.isSubmergedInWater();
                        if (inWater) {
                            airTicks = 0;
                        } else {
                            Box checkZone = boat.getBoundingBox().stretch(0, -1.0, 0);
                            assert client.world != null;
                            Iterable<VoxelShape> collisions = client.world.getBlockCollisions(boat, checkZone);
                            boolean hasBlockUnder = StreamSupport.stream(collisions.spliterator(), false)
                                    .anyMatch(shape -> !shape.isEmpty());
                            if (!hasBlockUnder) {
                                airTicks++;
                                if (airTicks > DISCONNECT_TICK_THRESHOLD) {
                                    client.getNetworkHandler().getConnection().disconnect(Text.translatable("multiplayer.disconnect.flying"));
                                    airTicks = 0;
                                }
                            } else {
                                airTicks = 0;
                            }
                        }
                    } else {
                        airTicks = 0;
                    }
                }
            }
        }
    }
    private static void handleNameMatchLogic(MinecraftClient client, CommonBoatConfig cfg) {
        if (client.getNetworkHandler() != null && cfg.disableOnNameMatch && !cfg.nameMatchList.isEmpty()) {
            Collection<String> onlinePlayerNames = client.getNetworkHandler().getCommandSource().getPlayerNames();
            Set<String> currentMatches = new HashSet<>();
            for (String name : onlinePlayerNames) {
                boolean matchFound = false;
                for (String part : cfg.nameMatchList) {
                    if (part != null && !part.isEmpty() && name.contains(part)) {
                        matchFound = true;
                        break;
                    }
                }
                boolean shouldFlag = (cfg.nameMatchMode == CommonBoatConfig.BlackWhiteList.WHITELIST) != matchFound;
                if (shouldFlag) {
                    currentMatches.add(name);
                    if (!flaggedNames.contains(name)) {
                        flaggedNames.add(name);
                        if (cfg.enabled) {
                            cfg.enabled = false;
                            performToggle(client, cfg, "text.commonboat.config.enable_mod", false);
                        }
                    }
                }
            }
            flaggedNames.removeIf(n -> !currentMatches.contains(n));
        } else {
            flaggedNames.clear();
        }
    }
    private static void handleKeybinds(MinecraftClient client, CommonBoatConfig cfg) {
        if (isKeyJustPressed(CommonBoatMalilibConfig.masterToggleKey)) {
            boolean wasEnabled = cfg.enabled;
            cfg.enabled = !cfg.enabled;
            performToggle(client, cfg, "text.commonboat.config.enable_mod", cfg.enabled);
            if (wasEnabled && cfg.easterEggsEnabled && cfg.handbrakeEnabled) {
                if (client.player != null) client.player.playSound(Sounds.EASTER_EGG_DISABLE_SOUND);
            }
        }
        handlePhysicsKeybinds(client, cfg);
        handleEasterEggKeybinds(client, cfg);
    }
    private static void handlePhysicsKeybinds(MinecraftClient client, CommonBoatConfig cfg) {
        if (isKeyJustPressed(CommonBoatMalilibConfig.slipperinessToggleKey)) {
            cfg.slipperinessEnabled = !cfg.slipperinessEnabled;
            performToggle(client, cfg, "text.commonboat.config.enable_slipperiness", cfg.slipperinessEnabled);
        }
        if (isKeyJustPressed(CommonBoatMalilibConfig.velocityToggleKey)) {
            cfg.velocityMultiplierEnabled = !cfg.velocityMultiplierEnabled;
            performToggle(client, cfg, "text.commonboat.config.enable_velocity", cfg.velocityMultiplierEnabled);
        }
        if (isKeyJustPressed(CommonBoatMalilibConfig.stepHeightToggleKey)) {
            cfg.boatStepHeightEnabled = !cfg.boatStepHeightEnabled;
            performToggle(client, cfg, "text.commonboat.config.enable_step_height", cfg.boatStepHeightEnabled);
        }
        if (isKeyJustPressed(CommonBoatMalilibConfig.airDragToggleKey)) {
            cfg.removeAirDrag = !cfg.removeAirDrag;
            performToggle(client, cfg, "text.commonboat.config.remove_air_drag", cfg.removeAirDrag);
        }
    }
    private static void handleEasterEggKeybinds(MinecraftClient client, CommonBoatConfig cfg) {
        if (isKeyJustPressed(CommonBoatMalilibConfig.easterEggsToggleKey)) {
            cfg.easterEggsEnabled = !cfg.easterEggsEnabled;
            performToggle(client, cfg, "text.commonboat.config.enable_easter_eggs", cfg.easterEggsEnabled);
        }
        if (cfg.easterEggsEnabled) {
            if (isKeyJustPressed(CommonBoatMalilibConfig.handbrakeToggleKey)) {
                cfg.handbrakeEnabled = !cfg.handbrakeEnabled;
                performToggle(client, cfg, "text.commonboat.config.enable_handbrake", cfg.handbrakeEnabled);
            }
            if (isKeyJustPressed(CommonBoatMalilibConfig.flappyBirdToggleKey)) {
                cfg.flappyBirdEnabled = !cfg.flappyBirdEnabled;
                performToggle(client, cfg, "text.commonboat.config.enable_flappybird", cfg.flappyBirdEnabled);
            }
            if (isKeyJustPressed(CommonBoatMalilibConfig.flappyBirdPitchToggleKey)) {
                cfg.flappyBirdPitchControl = !cfg.flappyBirdPitchControl;
                performToggle(client, cfg, "text.commonboat.config.enable_flappybird_pitch_control", cfg.flappyBirdPitchControl);
            }
            if (isKeyJustPressed(CommonBoatMalilibConfig.leFischeAuChocolatToggleKey)) {
                cfg.leFischeAuChocolatEnabled = !cfg.leFischeAuChocolatEnabled;
                performToggle(client, cfg, "text.commonboat.config.enable_lefischeauchocolat", cfg.leFischeAuChocolatEnabled);
            }
            if (isKeyJustPressed(CommonBoatMalilibConfig.elytraBoatToggleKey)) {
                cfg.elytraBoatEnabled = !cfg.elytraBoatEnabled;
                performToggle(client, cfg, "text.commonboat.config.enable_elytraboat", cfg.elytraBoatEnabled);
            }
            if (isKeyJustPressed(CommonBoatMalilibConfig.blockBreakingPenaltyToggleKey)) {
                cfg.disableBlockBreakingPenalty = !cfg.disableBlockBreakingPenalty;
                performToggle(client, cfg, "text.commonboat.config.disable_block_breaking_penalty", cfg.disableBlockBreakingPenalty);
            }
        }
    }
}