package net.craftish37.commonboat;

import net.craftish37.commonboat.mixin.TropicalFishEntityAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.util.DyeColor;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CopyOnWriteArrayList;
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
    private static class SheetData {
        int color;
        Set<Integer> ids;
        boolean isWildcard;
        SheetData(int color, Set<Integer> ids, boolean isWildcard) {
            this.color = color;
            this.ids = ids;
            this.isWildcard = isWildcard;
        }
    }
    private static final List<SheetData> LOADED_SHEETS = new CopyOnWriteArrayList<>();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final Pattern SHEET_ID_PATTERN = Pattern.compile("spreadsheets/d/([a-zA-Z0-9_-]+)");
    private static final Pattern GID_PATTERN = Pattern.compile("[?#&]gid=(\\d+)");
    private static final Pattern CSV_SPLIT_REGEX = Pattern.compile(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
    private static final Pattern QUOTE_COUNT_REGEX = Pattern.compile("\"");
    public static Integer getVariantIdFromBucket(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() != Items.TROPICAL_FISH_BUCKET) return null;
        Object patternObj = stack.get(DataComponentTypes.TROPICAL_FISH_PATTERN);
        DyeColor baseColor = stack.get(DataComponentTypes.TROPICAL_FISH_BASE_COLOR);
        DyeColor patternColor = stack.get(DataComponentTypes.TROPICAL_FISH_PATTERN_COLOR);
        if (patternObj != null && baseColor != null && patternColor != null) {
            String patternName = patternObj.toString().toLowerCase();
            int shapeId = 0;
            int size = 0;
            boolean found = false;
            for (Map.Entry<String, Integer> entry : SMALL_SHAPE_MAP.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(patternName)) {
                    shapeId = entry.getValue();
                    found = true;
                    break;
                }
            }
            if (!found) {
                for (Map.Entry<String, Integer> entry : LARGE_SHAPE_MAP.entrySet()) {
                    if (entry.getKey().equalsIgnoreCase(patternName)) {
                        shapeId = entry.getValue();
                        size = 1;
                        found = true;
                        break;
                    }
                }
            }
            if (found) {
                int baseId = baseColor.ordinal();
                int patternId = patternColor.ordinal();
                return (patternId << 24) | (baseId << 16) | (shapeId << 8) | size;
            }
        }
        var customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData != null) {
            var nbt = customData.copyNbt();
            if (nbt.contains("BucketVariantTag")) {
                return nbt.getInt("BucketVariantTag").orElse(0);
            }
        }
        return null;
    }
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
    public static Integer getFishVariantColor(int variantId) {
        for (SheetData sheet : LOADED_SHEETS) {
            if ((sheet.isWildcard && !DEFAULT_IGNORED_FISH_IDS.contains(variantId)) || sheet.ids.contains(variantId)) {
                return sheet.color;
            }
        }
        return null;
    }
    private static int getColorIntFromHex(String hex) {
        if (hex == null) return 0xFFFFFFFF;
        hex = hex.trim();
        if (hex.startsWith("#")) hex = hex.substring(1);
        if (hex.length() == 8) {
            hex = hex.substring(2);
        }
        if (hex.length() != 6) {
            return 0xFFFFFFFF;
        }
        try {
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            return (0xFF << 24) | (r << 16) | (g << 8) | b;
        } catch (NumberFormatException e) {
            return 0xFFFFFFFF;
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
    private static class SheetFetchResult {
        Set<Integer> ids = ConcurrentHashMap.newKeySet();
        boolean hasWildcard = false;
    }
    private static SheetFetchResult fetchSheetData(String sheetUrl) {
        if (sheetUrl == null) return null;
        sheetUrl = sheetUrl.replace("\\u003d", "=").replace("\\u0026", "&").trim();
        if (sheetUrl.isEmpty()) return null;
        if (sheetUrl.equals("*")) {
            SheetFetchResult result = new SheetFetchResult();
            result.hasWildcard = true;
            return result;
        }
        Matcher idMatcher = SHEET_ID_PATTERN.matcher(sheetUrl);
        if (!idMatcher.find()) {
            System.out.println("[CommonBoat Debug] Failed to extract Sheet ID.");
            return null;
        }
        String sheetId = idMatcher.group(1);
        String gid = "0";
        Matcher gidMatcher = GID_PATTERN.matcher(sheetUrl);
        if (gidMatcher.find()) gid = gidMatcher.group(1);
        String exportUrl = "https://docs.google.com/spreadsheets/d/" + sheetId + "/export?format=csv&gid=" + gid;
        SheetFetchResult result = new SheetFetchResult();
        try {
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
                            if (name.equals("*")) {
                                result.hasWildcard = true;
                                continue;
                            }
                            Integer variantId = null;
                            if (name.equals("-")) {
                                variantId = getVariantIdFromTypeString(type);
                            } else {
                                variantId = SPECIAL_FISH_NAME_TO_ID_MAP.get(name);
                            }
                            if (variantId != null) {
                                result.ids.add(variantId);
                            }
                        }
                    }
                }
            }
            return result;
        } catch (Exception e) {
            System.err.println("[CommonBoat Debug] FAILED to fetch sheet data from: " + sheetUrl);
            e.printStackTrace();
            return null;
        }
    }
    private static void updateCapturedFishList() {
        CommonBoatConfig cfg = ConfigAccess.get();
        List<SheetData> newSheets = new ArrayList<>();
        for (String url : cfg.capturedFishSheetUrls) {
            if (url == null || url.trim().isEmpty()) continue;
            String cleanUrl = url.replace("\\u003d", "=").replace("\\u0026", "&").trim();
            String hexColor = cfg.capturedFishSheetColors.getOrDefault(cleanUrl, "#FFFFFF");
            int colorInt = getColorIntFromHex(hexColor);
            SheetFetchResult result = fetchSheetData(url);
            if (result != null && (result.hasWildcard || !result.ids.isEmpty())) {
                newSheets.add(new SheetData(colorInt, result.ids, result.hasWildcard));
            }
        }
        LOADED_SHEETS.clear();
        LOADED_SHEETS.addAll(newSheets);
    }
    private static float[] getRGBFromInt(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return new float[]{r / 255.0f, g / 255.0f, b / 255.0f};
    }
    public static void onWorldRender(@org.jetbrains.annotations.Nullable MatrixStack matrices) {
        var cfg = ConfigAccess.get();
        if (!cfg.enabled || !cfg.easterEggsEnabled || !cfg.leFischeAuChocolatEnabled) return;
        if (client.world == null || client.player == null || matrices == null) return;
        Vec3d cameraPos = client.gameRenderer.getCamera().getPos();

        Map<SheetData, List<TropicalFishEntity>> batches = new LinkedHashMap<>();
        for (SheetData sheet : LOADED_SHEETS) {
            batches.put(sheet, new ArrayList<>());
        }
        List<TropicalFishEntity> defaults = new ArrayList<>();
        for (Entity entity : client.world.getOtherEntities(client.player, client.player.getBoundingBox().expand(cfg.fishDetectionDistance))) {
            if (!(entity instanceof TropicalFishEntity fish)) continue;
            int variant = fish.getDataTracker().get(TropicalFishEntityAccessor.getVariantTrackedData());
            if (!LOADED_SHEETS.isEmpty()) {
                boolean matched = false;
                for (SheetData sheet : LOADED_SHEETS) {
                    if ((sheet.isWildcard && !DEFAULT_IGNORED_FISH_IDS.contains(variant)) || sheet.ids.contains(variant)) {
                        batches.get(sheet).add(fish);
                        matched = true;
                        break;
                    }
                }
            } else {
                if (!DEFAULT_IGNORED_FISH_IDS.contains(variant)) {
                    defaults.add(fish);
                }
            }
        }
        VertexConsumerProvider.Immediate provider = client.getBufferBuilders().getEntityVertexConsumers();
        provider.draw();

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glLineWidth(1.0f);

        VertexConsumer consumer = provider.getBuffer(RenderLayer.getLines());
        for (Map.Entry<SheetData, List<TropicalFishEntity>> entry : batches.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                float[] rgb = getRGBFromInt(entry.getKey().color);
                renderFishList(consumer, matrices, cameraPos, entry.getValue(), rgb[0], rgb[1], rgb[2]);
            }
        }
        if (!defaults.isEmpty()) {
            renderFishList(consumer, matrices, cameraPos, defaults, 1.0f, 1.0f, 1.0f);
        }

        provider.draw();
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }
    private static void renderFishList(VertexConsumer consumer, MatrixStack matrices, Vec3d cameraPos, List<TropicalFishEntity> fishList, float r, float g, float b) {
        for (TropicalFishEntity fish : fishList) {
            drawBoxOutline(matrices, consumer, cameraPos, fish.getBoundingBox(), r, g, b);
        }
        if (fishList.size() > 1) {
            Matrix4f matrix = matrices.peek().getPositionMatrix();
            for (int i = 0; i < fishList.size() - 1; i++) {
                TropicalFishEntity fish1 = fishList.get(i);
                TropicalFishEntity fish2 = fishList.get(i + 1);

                Vec3d center1 = fish1.getBoundingBox().getCenter();
                Vec3d center2 = fish2.getBoundingBox().getCenter();

                double x1 = center1.x - cameraPos.x;
                double y1 = center1.y - cameraPos.y;
                double z1 = center1.z - cameraPos.z;
                double x2 = center2.x - cameraPos.x;
                double y2 = center2.y - cameraPos.y;
                double z2 = center2.z - cameraPos.z;

                line(consumer, matrix, x1, y1, z1, x2, y2, z2, r, g, b);
            }
        }
    }
    private static void drawBoxOutline(MatrixStack matrices, VertexConsumer consumer, Vec3d cameraPos, Box box, float r, float g, float b) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        double minX = box.minX - cameraPos.x;
        double minY = box.minY - cameraPos.y;
        double minZ = box.minZ - cameraPos.z;
        double maxX = box.maxX - cameraPos.x;
        double maxY = box.maxY - cameraPos.y;
        double maxZ = box.maxZ - cameraPos.z;

        line(consumer, matrix, minX, minY, minZ, maxX, minY, minZ, r, g, b);
        line(consumer, matrix, maxX, minY, minZ, maxX, minY, maxZ, r, g, b);
        line(consumer, matrix, maxX, minY, maxZ, minX, minY, maxZ, r, g, b);
        line(consumer, matrix, minX, minY, maxZ, minX, minY, minZ, r, g, b);

        line(consumer, matrix, minX, maxY, minZ, maxX, maxY, minZ, r, g, b);
        line(consumer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b);
        line(consumer, matrix, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b);
        line(consumer, matrix, minX, maxY, maxZ, minX, maxY, minZ, r, g, b);

        line(consumer, matrix, minX, minY, minZ, minX, maxY, minZ, r, g, b);
        line(consumer, matrix, maxX, minY, minZ, maxX, maxY, minZ, r, g, b);
        line(consumer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b);
        line(consumer, matrix, minX, minY, maxZ, minX, maxY, maxZ, r, g, b);
    }

    private static void line(VertexConsumer consumer, Matrix4f matrix, double x1, double y1, double z1, double x2, double y2, double z2, float r, float g, float b) {
        consumer.vertex(matrix, (float) x1, (float) y1, (float) z1).color(r, g, b, 1.0F).normal(1.0F, 1.0F, 1.0F);
        consumer.vertex(matrix, (float) x2, (float) y2, (float) z2).color(r, g, b, 1.0F).normal(1.0F, 1.0F, 1.0F);
    }
}