package io.th0rgal.oraxen.pack.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.config.settings.Settings;
import io.th0rgal.oraxen.utils.AdventureUtils;
import io.th0rgal.oraxen.utils.VersionUtil;
import io.th0rgal.oraxen.utils.logs.Logs;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.UUID;

public class PolymathServer implements OraxenPackServer {

    private final String serverAddress;
    private String packUrl;
    private String minecraftPackURL;
    private String hash;
    private UUID packUUID;

    public PolymathServer() {
        String address = Settings.POLYMATH_SERVER.toString("atlas.oraxen.com");
        this.serverAddress = (address.startsWith("http://") || address.startsWith("https://") ? "" : "https://") + address + (address.endsWith("/") ? "" : "/");
    }

    @Override
    public void uploadPack() {
        try(CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(serverAddress + "upload");

            HttpEntity httpEntity = MultipartEntityBuilder
                    .create().addTextBody("id", Settings.POLYMATH_SECRET.toString())
                    .addBinaryBody("pack", OraxenPlugin.get().packPath().resolve("pack.zip").toFile())
                    .build();

            request.setEntity(httpEntity);

            CloseableHttpResponse response = httpClient.execute(request);
            HttpEntity responseEntity = response.getEntity();
            String responseString = EntityUtils.toString(responseEntity);
            JsonObject jsonOutput;
            try {
                jsonOutput = JsonParser.parseString(responseString).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                Logs.logError("The resource pack could not be uploaded due to a malformed response.");
                Logs.logWarning("This is usually due to the resourcepack server being down.");
                if (Settings.DEBUG.toBool()) e.printStackTrace();
                else Logs.logWarning(e.getMessage());
                return;
            }
            if (jsonOutput.has("url") && jsonOutput.has("sha1")) {
                packUrl = jsonOutput.get("url").getAsString();
                minecraftPackURL = packUrl.replace("https://", "http://");
                hash = jsonOutput.get("sha1").getAsString();
                packUUID = UUID.nameUUIDFromBytes(OraxenPackServer.hashArray(hash));
                return;
            }

            if (jsonOutput.has("error"))
                Logs.logError("Error: " + jsonOutput.get("error").getAsString());
            Logs.logError("Response: " + jsonOutput);
            Logs.logError("The resource pack has not been uploaded to the server. Usually this is due to an excessive size.");
        } catch(IllegalStateException | IOException ex) {
            Logs.logError("The resource pack has not been uploaded to the server. Usually this is due to an excessive size.");
            ex.printStackTrace();
        }
    }

    @Override
    public void sendPack(Player player) {
        String prompt = Settings.SEND_PACK_PROMPT.toString();
        boolean mandatory = Settings.SEND_PACK_MANDATORY.toBool();
        byte[] hashArray = OraxenPackServer.hashArray(hash);

        if (VersionUtil.atOrAbove("1.20.3")) {
            if (VersionUtil.isPaperServer()) player.setResourcePack(packUUID, minecraftPackURL, hash, AdventureUtils.MINI_MESSAGE.deserialize(prompt), mandatory);
            else player.setResourcePack(packUUID, minecraftPackURL, hashArray, AdventureUtils.parseLegacy(prompt), mandatory);
        }
        else if (VersionUtil.isPaperServer()) player.setResourcePack(minecraftPackURL, hashArray, AdventureUtils.MINI_MESSAGE.deserialize(prompt), mandatory);
        else player.setResourcePack(packUrl, hashArray, AdventureUtils.parseLegacy(prompt), mandatory);
    }
}
