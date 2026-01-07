package net.craftish37.commonboat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import java.net.InetAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import java.util.stream.StreamSupport;

public class CommonBoat implements ClientModInitializer {
    private static KeyBinding masterToggleKey;
    private static KeyBinding slipperinessToggleKey;
    private static KeyBinding velocityToggleKey;
    private static KeyBinding stepHeightToggleKey;
    private static KeyBinding airDragToggleKey;
    private static KeyBinding easterEggsToggleKey;
    private static KeyBinding handbrakeToggleKey;
    private static KeyBinding flappyBirdToggleKey;
    private static KeyBinding flappyBirdPitchToggleKey;
    private static KeyBinding leFischeAuChocolatToggleKey;
    private static KeyBinding elytraBoatToggleKey;
    private static KeyBinding blockBreakingPenaltyToggleKey;
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
    private static KeyBinding registerToggleKey(String key) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                key,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                new KeyBinding.Category(Identifier.of("commonboat", "general"))
        ));
    }
    private void performToggle(MinecraftClient client, CommonBoatConfig cfg, String configKey, boolean newState) {
        cfg.save();
        if (client.player != null) {
            String statusKey = newState ? "text.commonboat.status.enabled" : "text.commonboat.status.disabled";
            Text configName = Text.translatable("text.commonboat.config." + configKey);
            Text message = configName.copy().append(": ").append(Text.translatable(statusKey));

            client.player.sendMessage(message, true);
        }
    }
    private static String stripPort(String address) {
        if (address == null) return "";
        if (address.contains(":")) {
            return address.split(":")[0];
        }
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
    @Override
    public void onInitializeClient() {
        Sounds.registerSounds();
        ConfigAccess.get();
        EasterEggFishHighlighter.startUpdater();

        WorldRenderEvents.END_MAIN.register(context -> {
            EasterEggFishHighlighter.onWorldRender(context.matrices());
        });
        masterToggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.commonboat.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                new KeyBinding.Category(Identifier.of("commonboat", "general"))
        ));
        slipperinessToggleKey = registerToggleKey("text.commonboat.config.enable_slipperiness");
        velocityToggleKey = registerToggleKey("text.commonboat.config.enable_velocity");
        stepHeightToggleKey = registerToggleKey("text.commonboat.config.enable_step_height");
        airDragToggleKey = registerToggleKey("text.commonboat.config.remove_air_drag");
        easterEggsToggleKey = registerToggleKey("text.commonboat.config.enable_easter_eggs");
        handbrakeToggleKey = registerToggleKey("text.commonboat.config.enable_handbrake");
        flappyBirdToggleKey = registerToggleKey("text.commonboat.config.enable_flappybird");
        flappyBirdPitchToggleKey = registerToggleKey("text.commonboat.config.enable_flappybird_pitch_control");
        leFischeAuChocolatToggleKey = registerToggleKey("text.commonboat.config.enable_lefischeauchocolat");
        elytraBoatToggleKey = registerToggleKey("text.commonboat.config.enable_elytraboat");
        blockBreakingPenaltyToggleKey = registerToggleKey("text.commonboat.config.disable_block_breaking_penalty");
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
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
            CommonBoatConfig cfg = ConfigAccess.get();
            if (client.getNetworkHandler() != null && cfg.disableOnNameMatch && !cfg.nameMatchString.isEmpty()) {
                String[] prohibitedStrings = cfg.nameMatchString.split(";");
                Collection<String> onlinePlayerNames = client.getNetworkHandler().getCommandSource().getPlayerNames();
                Set<String> currentMatches = new HashSet<>();
                for (String name : onlinePlayerNames) {
                    boolean matchFound = false;
                    for (String part : prohibitedStrings) {
                        String trimmedPart = part.trim();
                        if (!trimmedPart.isEmpty() && name.contains(trimmedPart)) {
                            matchFound = true;
                            break;
                        }
                    }
                    if (matchFound) {
                        currentMatches.add(name);
                        if (!flaggedNames.contains(name)) {
                            flaggedNames.add(name);
                            if (cfg.enabled) {
                                cfg.enabled = false;
                                performToggle(client, cfg, "enable_mod", false);
                            }
                        }
                    }
                }
                flaggedNames.removeIf(n -> !currentMatches.contains(n));
            } else {
                flaggedNames.clear();
            }
            while (masterToggleKey.wasPressed()) {
                boolean wasEnabled = cfg.enabled;
                cfg.enabled = !cfg.enabled;
                performToggle(client, cfg, "enable_mod", cfg.enabled);
                if (wasEnabled && cfg.easterEggsEnabled && cfg.handbrakeEnabled) {
                    assert client.player != null;
                    client.player.playSound(Sounds.EASTER_EGG_DISABLE_SOUND);
                }
            }
            if (slipperinessToggleKey.wasPressed()) {
                cfg.slipperinessEnabled = !cfg.slipperinessEnabled;
                performToggle(client, cfg, "enable_slipperiness", cfg.slipperinessEnabled);
            }
            if (velocityToggleKey.wasPressed()) {
                cfg.velocityMultiplierEnabled = !cfg.velocityMultiplierEnabled;
                performToggle(client, cfg, "enable_velocity", cfg.velocityMultiplierEnabled);
            }
            if (stepHeightToggleKey.wasPressed()) {
                cfg.boatStepHeightEnabled = !cfg.boatStepHeightEnabled;
                performToggle(client, cfg, "enable_step_height", cfg.boatStepHeightEnabled);
            }
            if (airDragToggleKey.wasPressed()) {
                cfg.removeAirDrag = !cfg.removeAirDrag;
                performToggle(client, cfg, "remove_air_drag", cfg.removeAirDrag);
            }
            if (easterEggsToggleKey.wasPressed()) {
                cfg.easterEggsEnabled = !cfg.easterEggsEnabled;
                performToggle(client, cfg, "enable_easter_eggs", cfg.easterEggsEnabled);
            }
            if (cfg.easterEggsEnabled) {
                if (handbrakeToggleKey.wasPressed()) {
                    cfg.handbrakeEnabled = !cfg.handbrakeEnabled;
                    performToggle(client, cfg, "enable_handbrake", cfg.handbrakeEnabled);
                }
                if (flappyBirdToggleKey.wasPressed()) {
                    cfg.flappyBirdEnabled = !cfg.flappyBirdEnabled;
                    performToggle(client, cfg, "enable_flappybird", cfg.flappyBirdEnabled);
                }
                if (flappyBirdPitchToggleKey.wasPressed()) {
                    cfg.flappyBirdPitchControl = !cfg.flappyBirdPitchControl;
                    performToggle(client, cfg, "enable_flappybird_pitch_control", cfg.flappyBirdPitchControl);
                }
                if (leFischeAuChocolatToggleKey.wasPressed()) {
                    cfg.leFischeAuChocolatEnabled = !cfg.leFischeAuChocolatEnabled;
                    performToggle(client, cfg, "enable_lefischeauchocolat", cfg.leFischeAuChocolatEnabled);
                }
                if (elytraBoatToggleKey.wasPressed()) {
                    cfg.elytraBoatEnabled = !cfg.elytraBoatEnabled;
                    performToggle(client, cfg, "enable_elytraboat", cfg.elytraBoatEnabled);
                }
                if (blockBreakingPenaltyToggleKey.wasPressed()) {
                    cfg.disableBlockBreakingPenalty = !cfg.disableBlockBreakingPenalty;
                    performToggle(client, cfg, "disable_block_breaking_penalty", cfg.disableBlockBreakingPenalty);
                }
            }
        });
    }
}