package net.craftish37.commonboat;

import org.lwjgl.opengl.GL11;
import net.craftish37.commonboat.mixin.TropicalFishEntityAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EasterEggFishHighlighter {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final Map<String, Integer> SMALL_SHAPE_MAP = new HashMap<>() {{
        put("Kob", 0);
        put("Sunstreak", 1);
        put("Snooper", 2);
        put("Dasher", 3);
        put("Brinely", 4);
        put("Spotty", 5);
    }};
    private static final Map<String, Integer> LARGE_SHAPE_MAP = new HashMap<>() {{
        put("Flopper", 0);
        put("Stripey", 1);
        put("Glitter", 2);
        put("Blockfish", 3);
        put("Betty", 4);
        put("Clayfish", 5);
    }};
    private static final Map<String, Integer> COLOR_MAP = new HashMap<>() {{
        put("White", 0);
        put("Orange", 1);
        put("Magenta", 2);
        put("Light Blue", 3);
        put("Yellow", 4);
        put("Lime", 5);
        put("Pink", 6);
        put("Gray", 7);
        put("Light Gray", 8);
        put("Cyan", 9);
        put("Purple", 10);
        put("Blue", 11);
        put("Brown", 12);
        put("Green", 13);
        put("Red", 14);
        put("Black", 15);
    }};
    private static final Map<String, Integer> SPECIAL_FISH_NAME_TO_ID_MAP = new HashMap<>() {{
        put("Clownfish", 65536);
        put("Triggerfish", 459008);
        put("Tomato Clownfish", 917504);
        put("Red Snapper", 918273);
        put("Red Cichlid", 918529);
        put("Ornate Butterflyfish", 16778497);
        put("Queen Angelfish", 50660352);
        put("Cotton Candy Betta", 50726144);
        put("Threadfin", 67108865);
        put("Goatfish", 67110144);
        put("Yellow Tang", 67371009);
        put("Yellowtail Parrotfish", 67699456);
        put("Dottyback", 67764993);
        put("Parrotfish", 101253888);
        put("Moorish Idol", 117441025);
        put("Butterflyfish", 117441793);
        put("Anemone", 117506305);
        put("Black Tang", 117899265);
        put("Cichlid", 118161664);
        put("Blue Tang", 185008129);
        put("Emperor Red Snapper", 234882305);
        put("Red Lipped Blenny", 235340288);
    }};
    private static final Set<Integer> DEFAULT_IGNORED_FISH_IDS = Set.copyOf(SPECIAL_FISH_NAME_TO_ID_MAP.values());
    private static volatile Set<Integer> HIGHLIGHT_FISH_IDS = Set.of();
    private static volatile boolean usingSheetOverride = false;
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final Pattern SHEET_ID_PATTERN = Pattern.compile("spreadsheets/d/([a-zA-Z0-9_-]+)");
    private static final Pattern GID_PATTERN = Pattern.compile("[#&]gid=(\\d+)");
    private static final Pattern CSV_SPLIT_REGEX = Pattern.compile(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
    private static final Pattern QUOTE_COUNT_REGEX = Pattern.compile("\"");
    private static Integer getVariantIdFromTypeString(String type) {
        try {
            String shapeName;
            String baseColorName;
            String patternColorName;
            String[] parts = type.split("\\r?\\n");
            if (parts.length != 2) return null;
            shapeName = parts[0].trim();
            String colorString = parts[1].trim();
            String[] colorParts = colorString.split(",\\s*");
            if (colorParts.length == 1) {
                baseColorName = colorParts[0].trim();
                patternColorName = baseColorName;
            } else if (colorParts.length == 2) {
                baseColorName = colorParts[0].trim();
                patternColorName = colorParts[1].trim();
            } else {
                return null;
            }
            int size;
            Integer shapeId;
            if (SMALL_SHAPE_MAP.containsKey(shapeName)) {
                size = 0;
                shapeId = SMALL_SHAPE_MAP.get(shapeName);
            } else if (LARGE_SHAPE_MAP.containsKey(shapeName)) {
                size = 1;
                shapeId = LARGE_SHAPE_MAP.get(shapeName);
            } else {
                return null;
            }
            Integer baseColorId = COLOR_MAP.get(baseColorName);
            Integer patternColorId = COLOR_MAP.get(patternColorName);
            if (baseColorId == null || patternColorId == null) {
                return null;
            }
            return (patternColorId << 24) | (baseColorId << 16) | (shapeId << 8) | size;
        } catch (Exception e) {
            return null;
        }
    }
    public static void startUpdater() {
        scheduler.scheduleAtFixedRate(
                EasterEggFishHighlighter::updateCapturedFishList,
                0, 1, TimeUnit.MINUTES
        );
    }
    private static int countQuotes(String s) {
        int count = 0;
        Matcher m = QUOTE_COUNT_REGEX.matcher(s);
        while (m.find()) {
            count++;
        }
        return count;
    }
    private static void updateCapturedFishList() {
        try {
            CommonBoatConfig cfg = ConfigAccess.get();
            String sheetUrl = cfg.capturedFishSheetUrl;
            if (sheetUrl == null || sheetUrl.trim().isEmpty()) {
                usingSheetOverride = false;
                return;
            }
            Matcher idMatcher = SHEET_ID_PATTERN.matcher(sheetUrl);
            if (!idMatcher.find()) {
                System.err.println("[CommonBoat] Invalid Google Sheets URL format.");
                usingSheetOverride = false;
                return;
            }
            String sheetId = idMatcher.group(1);
            String gid = "0";
            Matcher gidMatcher = GID_PATTERN.matcher(sheetUrl);
            if (gidMatcher.find()) {
                gid = gidMatcher.group(1);
            }
            String exportUrl = "https://docs.google.com/spreadsheets/d/" + sheetId + "/export?format=csv&gid=" + gid;
            Set<Integer> newHighlightIdSet = ConcurrentHashMap.newKeySet();
            URL url = new URL(exportUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                in.readLine();
                String line;
                StringBuilder lineBuilder = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    lineBuilder.append(line);
                    if (countQuotes(lineBuilder.toString()) % 2 != 0) {
                        lineBuilder.append("\n");
                        continue;
                    }
                    String completeLine = lineBuilder.toString();
                    lineBuilder.setLength(0);
                    final int nameIndex = 0;
                    final int typeIndex = 1;
                    final int capturedIndex = 2;
                    String[] columns = CSV_SPLIT_REGEX.split(completeLine, -1);
                    if (columns.length > capturedIndex) {
                        String captured = columns[capturedIndex].trim().replace("\"", "");
                        if (captured.equalsIgnoreCase("FALSE")) {
                            String name = columns[nameIndex].trim().replace("\"", "");
                            String type = columns[typeIndex].trim().replace("\"", "");
                            Integer variantId = null;
                            if (name.equals("-")) {
                                variantId = getVariantIdFromTypeString(type);
                                if (variantId != null) {
                                    newHighlightIdSet.add(variantId);
                                }
                            } else {
                                variantId = SPECIAL_FISH_NAME_TO_ID_MAP.get(name);
                                if (variantId != null) {
                                    newHighlightIdSet.add(variantId);
                                }
                            }
                        }
                    }
                }
            }
            HIGHLIGHT_FISH_IDS = newHighlightIdSet;
            usingSheetOverride = true;
        } catch (Exception e) {
            System.err.println("[CommonBoat] Failed to update captured fish list:");
            e.printStackTrace();
            usingSheetOverride = false;
        }
    }
    public static void onWorldRender(@org.jetbrains.annotations.Nullable MatrixStack matrices) {
        var cfg = ConfigAccess.get();
        if (!cfg.enabled || !cfg.easterEggsEnabled || !cfg.leFischeAuChocolatEnabled) return;
        if (client.world == null || client.player == null || matrices == null) return;
        Vec3d cameraPos = client.gameRenderer.getCamera().getPos();
        VertexConsumerProvider.Immediate provider = client.getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer consumer = provider.getBuffer(RenderLayer.getLines());
        List<TropicalFishEntity> fishToHighlight = new ArrayList<>();
        for (Entity entity : client.world.getOtherEntities(client.player, client.player.getBoundingBox().expand(cfg.fishDetectionDistance))) {
            if (!(entity instanceof TropicalFishEntity fish)) continue;
            int variant = fish.getDataTracker().get(TropicalFishEntityAccessor.getVariantTrackedData());

            if (usingSheetOverride) {
                if (HIGHLIGHT_FISH_IDS.contains(variant)) {
                    fishToHighlight.add(fish);
                }
            } else {
                if (!DEFAULT_IGNORED_FISH_IDS.contains(variant)) {
                    fishToHighlight.add(fish);
                }
            }
        }
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        for (TropicalFishEntity fish : fishToHighlight) {
            drawBoxOutline(matrices, consumer, cameraPos, fish.getBoundingBox());
        }
        if (fishToHighlight.size() > 1) {
            Matrix4f matrix = matrices.peek().getPositionMatrix();
            for (int i = 0; i < fishToHighlight.size() - 1; i++) {
                TropicalFishEntity fish1 = fishToHighlight.get(i);
                TropicalFishEntity fish2 = fishToHighlight.get(i + 1);

                Vec3d center1 = fish1.getBoundingBox().getCenter();
                Vec3d center2 = fish2.getBoundingBox().getCenter();

                double x1 = center1.x - cameraPos.x;
                double y1 = center1.y - cameraPos.y;
                double z1 = center1.z - cameraPos.z;
                double x2 = center2.x - cameraPos.x;
                double y2 = center2.y - cameraPos.y;
                double z2 = center2.z - cameraPos.z;

                line(consumer, matrix, x1, y1, z1, x2, y2, z2);
            }
        }

        provider.draw();
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glLineWidth(1.0f);
    }
    private static void drawBoxOutline(MatrixStack matrices, VertexConsumer consumer, Vec3d cameraPos, Box box) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        double minX = box.minX - cameraPos.x;
        double minY = box.minY - cameraPos.y;
        double minZ = box.minZ - cameraPos.z;
        double maxX = box.maxX - cameraPos.x;
        double maxY = box.maxY - cameraPos.y;
        double maxZ = box.maxZ - cameraPos.z;

        line(consumer, matrix, minX, minY, minZ, maxX, minY, minZ);
        line(consumer, matrix, maxX, minY, minZ, maxX, minY, maxZ);
        line(consumer, matrix, maxX, minY, maxZ, minX, minY, maxZ);
        line(consumer, matrix, minX, minY, maxZ, minX, minY, minZ);

        line(consumer, matrix, minX, maxY, minZ, maxX, maxY, minZ);
        line(consumer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ);
        line(consumer, matrix, maxX, maxY, maxZ, minX, maxY, maxZ);
        line(consumer, matrix, minX, maxY, maxZ, minX, maxY, minZ);

        line(consumer, matrix, minX, minY, minZ, minX, maxY, minZ);
        line(consumer, matrix, maxX, minY, minZ, maxX, maxY, minZ);
        line(consumer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ);
        line(consumer, matrix, minX, minY, maxZ, minX, maxY, maxZ);
    }
    private static void line(VertexConsumer consumer, Matrix4f matrix, double x1, double y1, double z1, double x2, double y2, double z2) {
        consumer.vertex(matrix, (float) x1, (float) y1, (float) z1).color(1.0F, 1.0F, 1.0F, 1.0F).normal(1.0F, 1.0F, 1.0F);
        consumer.vertex(matrix, (float) x2, (float) y2, (float) z2).color(1.0F, 1.0F, 1.0F, 1.0F).normal(1.0F, 1.0F, 1.0F);
    }
}